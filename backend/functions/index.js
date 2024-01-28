// https://us-central1-friendlychat-21ea9.cloudfunctions.net/addMessage?text=helloDolly

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

const mkdirp = require('mkdirp-promise');
const vision = require('@google-cloud/vision');
const spawn = require('child-process-promise').spawn;
const path = require('path');
const os = require('os');
const fs = require('fs');

const runtimeOpts = {
  timeoutSeconds: 300,
  memory: '1GB'
}


// Take the text parameter passed to this HTTP endpoint and insert it into the
// Realtime Database under the path /messages/:pushId/original
exports.addMessage = functions.https.onRequest((req, res) => {
  // Grab the text parameter.
  const original = req.query.text;
  // Push the new message into the Realtime Database using the Firebase Admin SDK.
  return admin.database().ref('/chat_msgs/chat_id').push({original: original}).then((snapshot) => {
    // Redirect with 303 SEE OTHER to the URL of the pushed object in the Firebase console.
    return res.redirect(303, snapshot.ref.toString());
  });
});

// Listens for new messages added to /messages/:pushId/original and creates an
// uppercase version of the message to /messages/:pushId/uppercase
// exports.makeUppercase = functions.database.ref('/messages/{pushId}/original')
//     .onCreate((snapshot, context) => {
//       // Grab the current value of what was written to the Realtime Database.
//       const original = snapshot.val();
//       console.log('Uppercasing', context.params.pushId, original);
//       const uppercase = original.toUpperCase();
//       // You must return a Promise when performing asynchronous tasks inside a Functions such as
//       // writing to the Firebase Realtime Database.
//       // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
//       return snapshot.ref.parent.child('uppercase').set(uppercase);
//     });

        // Cleans up the tokens that are no longer valid.
        function cleanupTokens(response, tokens) {
         // For each notification we check if there was an error.
         const tokensToRemove = {};
         response.results.forEach((result, index) => {
           const error = result.error;
           if (error) {
             console.error('Failure sending notification to', tokens[index], error);
             // Cleanup the tokens who are not registered anymore.
             if (error.code === 'messaging/invalid-registration-token' ||
                 error.code === 'messaging/registration-token-not-registered') {
               tokensToRemove[`/fcmTokens/${tokens[index]}`] = null;
             }
           }
         });
         // return admin.database().ref().update(tokensToRemove);
        }

        // Sends a notifications to all users when a new message is posted.
        exports.sendNotifications = functions.database.ref('/chat_msgs/{chat_id}/{chat_msg_id}')
        .onCreate(async(snapshot, context) => {
              // Notification details.
              const title = snapshot.val().user.name;
              var text = snapshot.val().text;

              const chatDetails = await admin.database().ref(`/chats/${context.params.chat_id}`).once('value');

              var full_title = "";

              if(chatDetails.val().isGroup) {
                full_title = `${title} to ${chatDetails.val().dialogName}`;
              } else {
                full_title = `${title} sent you a message`;
              }

              if(!text) {
                // its a image
                return null;
              }
console.log("text before" + text);
              text = text.replace(snapshot.val().id, "");
console.log("text after" + text);
console.log("user id : " + snapshot.val().id );
              const small_text = text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : '' ;
              const payload = {
                data: {
                  title: full_title,
                  body: `${title} : ${small_text}`,
                  icon: snapshot.val().user.avatar || '/images/profile_placeholder.png',
                  click_action: `https://${process.env.GCLOUD_PROJECT}.firebaseapp.com`,
                // },
                // data : {
                  "picture_url" : snapshot.val().imageurl ? snapshot.val().imageurl.url : "",
                  "avatar" : snapshot.val().user.avatar,
                  "big_text" : text ? text : "",
                  "chatId" : context.params.chat_id,
                }

              };
              // var token = 'dXgE1dM9WZo:APA91bFbABB5e9D9UAvLRnN_RoDqnFofjk78TV_0Us8bB7CZTwDIvDYC2k1vhALphUpz08FoFeR2Ixj8yLxd5FKBmeeOTbwOaBiA85ViOCB_mObrPWzD_fxok3WbofOcZqKnXyVazRE3';
              // const response = await admin.messaging().sendToDevice(token, payload);
              // Get the list of device tokens.
              // const allTokens = await admin.database().ref('').once('value');
              const allMembers = await admin.database().ref(`/chats/${context.params.chat_id}/users`).once('value');
              console.log("allMembers : " + allMembers.val());
              if (allMembers.exists()) {
                // Listing all device tokens to send a notification to.
                const allMembersIds = Object.keys(allMembers.val());
                // const allMembersIds = [allMembers.val()];
                console.log("allMembersIds : " + allMembersIds);

                var selfIndex = allMembersIds.indexOf(snapshot.val().id);
                if(selfIndex > -1) {
                  allMembersIds.splice(selfIndex,1);
                }
                console.log("allMembersIds without self : " + allMembersIds);

                var tokens = [];
                for (const memId of allMembersIds)  {
                  console.log(" memId : " + memId);
                  var tokenObject = await admin.database().ref(`/users/${memId}/fcm_token`).once('value');
                  var fcm_token = tokenObject.val();
                  console.log("tok : " + fcm_token);
                  if (fcm_token && fcm_token != "") {
                    tokens.push(fcm_token);
                  }
                }
                console.log("Tokens : " + tokens);
                // Send notifications to all tokens.
                //const response = await admin.messaging().sendToCondition(true, payload);
                if(tokens.length < 1 || tokens[0] == "") {
                  return null;
                }
                const response = await admin.messaging().sendToDevice(tokens, payload);

                await cleanupTokens(response, tokens);
                console.log('Notifications have been sent and tokens cleaned up.');
              }
            });


            // Sends a notifications to all users when a new group is created.
            exports.sendNotificationsForNewGroup = functions.database.ref('/chats/{chat_id}/users/{user_id}')
            .onCreate(async(userObj, context) => {
                  // Notification details.

                  const snapshot = await admin.database().ref(`/chats/${context.params.chat_id}`).once('value');

                  if(!snapshot.val().isGroup) {
                    return null;
                  }

                  if(snapshot.val().admin == context.params.user_id) {
                    return null;
                  }

console.log("new group : " + snapshot.val().dialogName);
                  // const adminKey = snapshot.val().admin;
                  // const groupAdmin = await admin.database().ref(`/users/${adminKey}`).once('value');

                  const dialogName = snapshot.val().dialogName || "";
                  // const text = snapshot.val().text;
                  const payload = {
                    data: {
                      title: `You were added to chat ${dialogName}`,
                      // body: text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : '',
                      icon: snapshot.val().dialogPhoto || '/images/profile_placeholder.png',
                      click_action: `https://${process.env.GCLOUD_PROJECT}.firebaseapp.com`,
                    // },
                    // data : {
                      "chatId" : context.params.chat_id,
                    }
                  };

                    const allMembersIds = [context.params.user_id];
                    console.log("allMembersIds : " + allMembersIds);

                    var tokens = [];
                    for (const memId of allMembersIds)  {
                      console.log(" memId : " + memId);
                      var tokenObject = await admin.database().ref(`/users/${memId}/fcm_token`).once('value');
                      var fcm_token = tokenObject.val();
                      console.log("tok : " + fcm_token);
                      if (fcm_token && fcm_token != "") {
                        tokens.push(fcm_token);
                      }
                    }
                    console.log("Tokens : " + tokens);
                    // Send notifications to all tokens.
                    //const response = await admin.messaging().sendToCondition(true, payload);
                    if(tokens.length < 1 || tokens[0] == "") {
                      return null;
                    }
                    const response = await admin.messaging().sendToDevice(tokens, payload);

                    await cleanupTokens(response, tokens);
                    console.log('Notifications have been sent and tokens cleaned up.');
                });


/*                // Sends a notifications to new users on Group.
                exports.sendNotificationsForNewMember = functions.database.ref('/chats/{chat_id}')
                .onUpdate(async(change, context) => {
                      // Notification details.
                      if(!change.after.val().isGroup) {
                        return null;
                      }

                      console.log("new member : " + change.before.val().dialogName + " new: "+ change.after.val().dialogName);

                      // const adminKey = snapshot.val().admin;
                      // const groupAdmin = await admin.database().ref(`/users/${adminKey}`).once('value');

                      const dialogName = change.after.val().dialogName || "";
                      // const text = snapshot.val().text;
                      const payload = {
                        notification: {
                          title: `You were added to chat ${dialogName}`,
                          // body: text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : '',
                          icon: change.after.val().dialogPhoto || '/images/profile_placeholder.png',
                          click_action: `https://${process.env.GCLOUD_PROJECT}.firebaseapp.com`,
                        }
                      };

                        const allMembersIds = Object.keys(change.after.val().users);
                        console.log("allMembersIds : " + allMembersIds);

                        const oldMembersIds = Object.keys(change.before.val().users);
                        console.log("oldMembersIds : " + allMembersIds);

                        if(oldMembersIds.length < 1) {
                            return null;
                        }

                        for(const oldie in oldMembersIds) {
                          var selfIndex = allMembersIds.indexOf(oldie);
                          if(selfIndex > -1) {
                            allMembersIds.splice(selfIndex,1);
                          }
                        }

                        console.log("allMembersIds without self : " + allMembersIds);

                        var tokens = [];
                        for (const memId of allMembersIds)  {
                          console.log(" memId : " + memId);
                          var tokenObject = await admin.database().ref(`/users/${memId}/fcm_token`).once('value');
                          var fcm_token = tokenObject.val();
                          console.log("tok : " + fcm_token);
                          tokens.push(fcm_token);
                        }
                        console.log("Tokens : " + tokens);
                        // Send notifications to all tokens.
                        //const response = await admin.messaging().sendToCondition(true, payload);
                        const response = await admin.messaging().sendToDevice(tokens, payload);

                        await cleanupTokens(response, tokens);
                        console.log('Notifications have been sent and tokens cleaned up.');
                    });
*/

async function sendImageNotification(snapshot, context) {
  const title = snapshot.val().user.name;
  const text = snapshot.val().text;

  const small_text = text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : '' ;
  const payload = {
    data: {
      title: `${title} posted ${text ? 'a message' : 'an image'}`,
      body: "", //`${title} : ${small_text}`,
      icon: snapshot.val().user.avatar || '/images/profile_placeholder.png',
      click_action: `https://${process.env.GCLOUD_PROJECT}.firebaseapp.com`,
    // },
    // data : {
      "picture_url" : snapshot.val().imageurl ? snapshot.val().imageurl.url : "",
      "avatar" : snapshot.val().user.avatar,
      "big_text" : text ? text : "",
      "chatId" : context.params.chat_id,
    }

  };
  // var token = 'dXgE1dM9WZo:APA91bFbABB5e9D9UAvLRnN_RoDqnFofjk78TV_0Us8bB7CZTwDIvDYC2k1vhALphUpz08FoFeR2Ixj8yLxd5FKBmeeOTbwOaBiA85ViOCB_mObrPWzD_fxok3WbofOcZqKnXyVazRE3';
  // const response = await admin.messaging().sendToDevice(token, payload);
  // Get the list of device tokens.
  // const allTokens = await admin.database().ref('').once('value');
  const allMembers = await admin.database().ref(`/chats/${context.params.chat_id}/users`).once('value');
  console.log("allMembers : " + allMembers.val());
  if (allMembers.exists()) {
    // Listing all device tokens to send a notification to.
    const allMembersIds = Object.keys(allMembers.val());
    // const allMembersIds = [allMembers.val()];
    console.log("allMembersIds : " + allMembersIds);

    var selfIndex = allMembersIds.indexOf(snapshot.val().id);
    if(selfIndex > -1) {
      allMembersIds.splice(selfIndex,1);
    }
    console.log("allMembersIds without self : " + allMembersIds);

    var tokens = [];
    for (const memId of allMembersIds)  {
      console.log(" memId : " + memId);
      var tokenObject = await admin.database().ref(`/users/${memId}/fcm_token`).once('value');
      var fcm_token = tokenObject.val();
      console.log("tok : " + fcm_token);
      if (fcm_token && fcm_token != "") {
        tokens.push(fcm_token);
      }
    }
    console.log("Tokens : " + tokens);
    // Send notifications to all tokens.
    //const response = await admin.messaging().sendToCondition(true, payload);
    if(tokens.length < 1 || tokens[0] == "") {
      return null;
    }
    const response = await admin.messaging().sendToDevice(tokens, payload);

    await cleanupTokens(response, tokens);
    console.log('Notifications have been sent and tokens cleaned up.');
  }
}
            /**
             * Function
             * When an image is uploaded we find label by the Cloud Vision
             * API and resize it using ImageMagick.
             */
            exports.labelImagesNew = functions.runWith(runtimeOpts).database.ref('/chat_msgs/{chat_id}/{chat_msg_id}')
            .onUpdate(async(change, context) => {

              console.log("Before : " + change.before.val().imageurl.url)
              console.log("After : " + change.after.val().imageurl.url)

              if( change.after.val().imageurl === undefined || change.before.val().imageurl.url == change.after.val().imageurl.url) {
                return null;
              }

              await sendImageNotification(change.after,context);

              var photoUrl = change.after.val().imageurl.url;
              var bucket = "mcc-fall-2018-g15.appspot.com";
              var name = "chats/" + photoUrl.split("chats%2F")[1].split("%2F")[0] + "/" + photoUrl.split("chats%2F")[1].split("%2F")[1].split("?")[0];

              const client = new vision.ImageAnnotatorClient();
              const inStorage = `gs://${bucket}/${name}`

              console.log("Storage url :" + inStorage)

              // Check the image content using the Cloud Vision API.
              const [ results ] = await client.labelDetection(inStorage)
              const labels = results.labelAnnotations

              var bestLabel = 'others'
              for(var i =0; i < labels.length; i++) {
                if(labels[i].description === 'technology') {
                  bestLabel = 'technology';
                  break;
                }
                if(labels[i].description === 'food') {
                  bestLabel = 'food';
                  break;
                }
                if(labels[i].description === 'screenshot') {
                  bestLabel = 'screenshot';
                  break;
                }
              }
              console.log("labelled as: " + bestLabel);
              console.log('Labels: ' + labels.map(label =>label.description).join(", "));

              await admin.database().ref(`/image_urls/${context.params.chat_id}/${context.params.chat_msg_id}`).update({"label" : bestLabel, "url" : photoUrl, "createdAt": change.after.val().createdAt, "user" : change.after.val().user, "id" : change.after.val().id });
              await resizeImage(name, bucket, null, photoUrl, context.params.chat_id);

              var chatId = context.params.chat_id;
              var downloadUrl = photoUrl.replace(chatId, chatId + "%2Fimage_low_res");
              console.log("download " + downloadUrl);
              var downloadUrlH = photoUrl.replace(chatId, chatId + "%2Fimage_high_res");
              console.log("download " + downloadUrlH);

              await admin.database().ref(`/image_urls/${context.params.chat_id}/${context.params.chat_msg_id}`).update({"url" : photoUrl, "low_res_url" : downloadUrl, "high_res_url" : downloadUrlH});
              return null;
            });

        /**
         * Resizes the given image located in the given bucket using ImageMagick.
         */
        async function resizeImage(filePath, bucketName, metadata) {
          const tempLocalFile = path.join(os.tmpdir(), filePath);
          const tempLocalDir = path.dirname(tempLocalFile);
          const bucket = admin.storage().bucket(bucketName);
          const low = "low";
          const high = "high";
          const CONFIG = {
            action: 'read',
            expires: '03-01-2500',
          };

          // Create the temp directory where the storage file will be downloaded.
          await mkdirp(tempLocalDir);
          console.log('Temporary directory has been created', tempLocalDir);
          // Download file from bucket.
          console.log("file: " + filePath + " dest : " + tempLocalFile + low);

          await bucket.file(filePath).download({destination: tempLocalFile + low});
          await bucket.file(filePath).download({destination: tempLocalFile + high});
//           metadata = await bucket.file(filePath).getMetadata();
//
//           await bucket.file(filePath).getMetadata().then(md => {
//               console.log('Metadata from Cloud Storage');
//               console.log(JSON.stringify(md[0], null, 2));  // Our custom metadata is buried in here!
//             }).catch(e => console.error(e));
//
// console.log("Metadata : " );
// console.log(metadata);
//           console.log('The file has been downloaded to', tempLocalFile);

          //For low res
          // Resize the image using ImageMagick.
          await spawn('convert', [tempLocalFile + low, '-resize', '640x480', tempLocalFile]);
          console.log('Resized image created at', tempLocalFile);
          // Uploading the Blurred image.
          var dest = path.join(path.dirname(filePath), 'image_low_res', path.basename(filePath));
          await bucket.upload(tempLocalFile, {
            destination: dest//,
            // metadata: {"contentType": "image/jpeg"}, // Keeping custom metadata.
          });
          console.log('Resized image uploaded to Storage at', path.join('image_low_res', path.basename(filePath)));
          fs.unlinkSync(tempLocalFile);
          console.log('Deleted local file', filePath);

          // var downloadUrl = await bucket.file(dest).getSignedUrl(CONFIG);

          //For high res
          // Resize the image using ImageMagick.
          await spawn('convert', [tempLocalFile + high, '-resize', '1280x960', tempLocalFile]);
          console.log('Resized image created at', tempLocalFile);
          // Uploading the Blurred image.
          var destH = path.join(path.dirname(filePath), 'image_high_res', path.basename(filePath));
          await bucket.upload(tempLocalFile, {
            destination: destH//,
            // metadata: {metadata: metadata[0]}, // Keeping custom metadata.
          });
          console.log('Resized image uploaded to Storage at', path.join('image_high_res', path.basename(filePath)));
          fs.unlinkSync(tempLocalFile);
          console.log('Deleted local file', filePath);

          // var downloadUrl = await bucket.file(destH).getSignedUrl(CONFIG);
        }

/**
* Function
* Updating last message for easy access by frontend
**/
        exports.updateLastMessage = functions.database.ref('/chat_msgs/{chat_id}/{chat_msg_id}')
        .onCreate(async(snapshot, context) => {
              // Notification details.
              const text = snapshot.val();
              // Push the new message into the Realtime Database using the Firebase Admin SDK.
              return admin.database().ref('/chats/' +  context.params.chat_id + '/last_message').update(snapshot.val());
          });
