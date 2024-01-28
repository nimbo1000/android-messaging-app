package mcc_2018_g15.chatapp;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import mcc_2018_g15.chatapp.holders.CustomIncomingImageMessageViewHolder;
import mcc_2018_g15.chatapp.holders.CustomIncomingTextMessageViewHolder;
import mcc_2018_g15.chatapp.holders.CustomOutcomingImageMessageViewHolder;
import mcc_2018_g15.chatapp.holders.CustomOutcomingTextMessageViewHolder;

//TODO Leave chat and user data load from the users table
public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private static int REQUEST_IMAGE = 1;
    private static int REQUEST_TAKE_PHOTO = 2;
    DatabaseReference myRef;
    FirebaseDatabase database;
    private static final String LOADING_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2018-g15.appspot.com/o/loading_gif.gif?alt=media&token=1edb26c7-6251-413b-884c-45fad9e09a53";
    String mCurrentPhotoPath;
    Uri photoURI;
    private String CHAT_ID;
    private Author author;
    private static  String USER_ID;
    private static String USERNAME;
    private static String AVATAR;
    private static ImageLoader imageLoader;
    private static MessageInput inputView;
    private MessagesListAdapter<Message> adapter;
    private static TextView userTitleTextView;
    private static CircleImageView userImageView;
    public static boolean isLeavingChat = false;
    private boolean isGroup;
    private boolean isAdmin = false;
    private HashMap<String,Long> memberJoiningDates = new HashMap<String, Long>();
    private  long USER_JOIN_TIME ;




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK ) {
            if (data != null) {
                final Uri uri = data.getData();
                Log.d(TAG, "Uri: " + uri.getLastPathSegment().toString());
                storeTemporaryImage(myRef,uri);

            }
        } else  if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            final Uri uri = photoURI;
            Log.d(TAG, "Uri: " + uri.getLastPathSegment());
            storeTemporaryImage(myRef,uri);

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isLeavingChat = false;

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.action_bar, null);
        userTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        userImageView = (CircleImageView) mCustomView.findViewById(R.id.actionBarImageView);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        Intent intent = getIntent();
        CHAT_ID = intent.getStringExtra("chatId");
        Log.d("chat_id", CHAT_ID);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            USER_ID = user.getUid();
            Log.d("UserID", USER_ID);
        }
        final DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference("chats").child(CHAT_ID);
        final DatabaseReference userref = FirebaseDatabase.getInstance().getReference("users");


        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Count ", "" + dataSnapshot.child("users").getChildrenCount());
                isGroup  = dataSnapshot.child("isGroup").getValue(Boolean.class);

                if (isGroup){
                    String group_name = dataSnapshot.child("dialogName").getValue(String.class);
                    String group_image = dataSnapshot.child("dialogPhoto").getValue(String.class);
                    userTitleTextView.setText(group_name);
                    Glide.with(getApplicationContext()).load(group_image).into(userImageView);
                    try {
                        USER_JOIN_TIME = dataSnapshot.child("users").child(USER_ID).getValue(Long.class);
                        String adminCheck= dataSnapshot.child("admin").getValue(String.class);
                        if(adminCheck.equals(USER_ID)){
                            isAdmin = true;
                        }
                    } catch (Exception e){
                        Log.e(TAG, "onDataChange: "+e.getMessage());
                    }



                }
                else{

                    for (DataSnapshot postSnapshot : dataSnapshot.child("users").getChildren()) {
                        String member = postSnapshot.getKey();
                        if(member.equals(USER_ID)){
                            USER_JOIN_TIME = postSnapshot.getValue(Long.class);
                        }
                        else {
                            Log.d("chosenMember", member);
                            userref.child(member).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String chatter = dataSnapshot.child("name").getValue(String.class);
                                    userTitleTextView.setText(chatter);

                                    String chatter_avatar = dataSnapshot.child("avatar").getValue(String.class);
                                    //TODO Replcae with default avatar
                                    if (!chatter_avatar.equals(SignUpActivity.DEFAULT_PROFILE)) {
                                        Log.d("chatter_avatar", "is set");
                                        Glide.with(getApplicationContext()).load(chatter_avatar).into(userImageView);
                                    }
                                    //Glide.with(MessageActivity.this).load(chatter_avatar).into(getSupportActionBar().set)
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                }

                startMessagesListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        userref.child(USER_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                USERNAME = dataSnapshot.child("name").getValue(String.class);
                AVATAR = dataSnapshot.child("avatar").getValue(String.class);
                author = new Author(USER_ID, USERNAME, AVATAR);
                MessagesList messagesList = findViewById(R.id.messagesList);

                CustomIncomingTextMessageViewHolder.Payload payload = new CustomIncomingTextMessageViewHolder.Payload();
                payload.avatarClickListener = new CustomIncomingTextMessageViewHolder.OnAvatarClickListener() {
                    @Override
                    public void onAvatarClick() {
                        Toast.makeText(MessageActivity.this,
                                "Text message avatar clicked", Toast.LENGTH_SHORT).show();
                    }
                };

                MessageHolders holdersConfig = new MessageHolders()
                        .setIncomingTextConfig(
                                CustomIncomingTextMessageViewHolder.class,
                                R.layout.custom_incoming_text_view_holder,
                                payload).setOutcomingTextConfig(
                                CustomOutcomingTextMessageViewHolder.class,
                                R.layout.item_custom_outcoming_text_message)
                        .setIncomingImageConfig(
                                CustomIncomingImageMessageViewHolder.class,
                                R.layout.item_custom_incoming_image_message)
                        .setOutcomingImageConfig(
                                CustomOutcomingImageMessageViewHolder.class,
                                R.layout.item_custom_outcoming_image_message);

                inputView = (MessageInput) findViewById(R.id.input);
                adapter = new MessagesListAdapter<>(author.getId(), holdersConfig, imageLoader);

                messagesList.setAdapter(adapter);
                Log.d("username", USERNAME + ", " + AVATAR);
                inputView.setInputListener(new MessageInput.InputListener() {
                    @Override
                    public boolean onSubmit(CharSequence input) {
                        //validate and send message

                        Message message = new Message(USER_ID, author, input.toString());
                        //Message rep_message = new Message("user_replier", zee, input.toString() + "'s reply");
                        myRef.push().setValue(message);
                        // myRef.push().setValue(rep_message);

                        return true;
                    }
                });

                inputView.setAttachmentsListener(new MessageInput.AttachmentsListener() {
                    @Override
                    public void onAddAttachments() {

                        new AlertDialog.Builder(MessageActivity.this)
                                .setItems(R.array.view_types_dialog, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0) {
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                                            intent.setType("image/*");
                                            startActivityForResult(intent, REQUEST_IMAGE);
                                        } else {
                                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                            // Ensure that there's a camera activity to handle the intent
                                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                                // Create the File where the photo should go
                                                File photoFile = null;
                                                try {
                                                    photoFile = createImageFile();
                                                } catch (IOException ex) {
                                                    // Error occurred while creating the File

                                                }
                                                // Continue only if the File was successfully created
                                                if (photoFile != null) {
                                                    photoURI = FileProvider.getUriForFile(MessageActivity.this,
                                                            "mcc_2018_g15.chatapp.fileprovider",
                                                            photoFile);
                                                    Log.d("photouri", photoURI.toString());
                                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                                                }
                                            }

                                        }
                                        Log.i("interface", String.valueOf(i));
                                    }
                                })
                                .show();
                        //select attachments

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("chat_msgs").child(CHAT_ID);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                final Message new_message = dataSnapshot.getValue(Message.class);
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                userref.addListenerForSingleValueEvent(new ValueEventListener() {

                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String username = dataSnapshot.child(new_message.getId()).child("name").getValue(String.class);
                        String avatar = dataSnapshot.child(new_message.getId()).child("avatar").getValue(String.class);
                        Author new_author = new Author(new_message.getId(),username,avatar);
                        new_message.setUser(new_author);
                        Log.d("everything: " , new_message.print());
                        String isLoading = new_message.getImageUrl();
                        Date messageTime = new_message.getCreatedAt();
                        Log.d("message_time_comare", messageTime.getTime() + ", " + USER_JOIN_TIME);

                        if(messageTime.getTime() > USER_JOIN_TIME) {
                            if (isLeavingChat) {
                                return;
                            }
                            adapter.addToStart(new_message,true);

                    }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//                if (isLoading != null) {
//                    if (isLoading.equals(LOADING_IMAGE_URL) && new_author.getId() != USER_ID) {
//                    } else {
//                        adapter.addToStart(new_message, true);
//
//                    }
//                } else {
//                    adapter.addToStart(new_message, true);
//                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot.hasChildren()) {
                    Log.d("onchildchanged", "hasbeencalled");
                    Message new_message = dataSnapshot.getValue(Message.class);

                    Author new_author = dataSnapshot.child("user").getValue(Author.class);
                    new_message.setUser(new_author);
                    if(dataSnapshot.child("imageurl").exists()){
                        Message.Image new_image = dataSnapshot.child("imageurl").getValue(Message.Image.class);
                        new_message.setImage(new_image);
                    }

                    Log.d("boutotloadimage: ", "right here + " + prevChildKey);
                    adapter.update(new_message);
//                    if(new_author.getId().equals(USER_ID)) {
//                        adapter.update(new_message);
//                    }
//                    else{
//                        adapter.addToStart(new_message,true);
//                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });


        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                if (url == LOADING_IMAGE_URL) {
                    Picasso.get().load(LOADING_IMAGE_URL).into(imageView);

                }
                Glide.with(MessageActivity.this).load(url).apply(new RequestOptions().fitCenter()).into(imageView);
                //Picasso.get().load(url).into(imageView);
            }

        };

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem addMember = menu.findItem(R.id.menu_addMember);
        MenuItem renameGroup = menu.findItem(R.id.menu_editGroup);
        if(isGroup){
            addMember.setVisible(true);
            renameGroup.setVisible(true);
        }
        else{
            addMember.setVisible(false);
            renameGroup.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_gallery) {
            //  logoutUser();
            Log.d("Menu_Item", "Gallery");

            Intent galleryIntent = new Intent(this, GalleryActivity.class);
            galleryIntent.putExtra("chatId", CHAT_ID);
            galleryIntent.putExtra("userId",USER_ID);
            galleryIntent.putExtra("userJoinTime", USER_JOIN_TIME);

            startActivity(galleryIntent);
            return true;
        } else if (id == R.id.menu_addMember) {
            //  logoutUser();
            Log.d("menu Item", "Add Member");
            Intent addMember = new Intent(MessageActivity.this, SearchUsersActivity.class);
            addMember.putExtra("addMember", true);
            addMember.putExtra("isAdmin", isAdmin);

            addMember.putExtra("chatId", CHAT_ID);
            startActivity(addMember);
            return true;
        } else if (id == R.id.menu_leaveChat){
            Task<Void> removeUserfromChat = FirebaseDatabase.getInstance().getReference("chats").child(CHAT_ID).child("users").child(USER_ID).removeValue();

            removeUserfromChat.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Task<Void> removeFromUsersChat = FirebaseDatabase.getInstance().getReference("users").child(USER_ID).child("user_chats").child(CHAT_ID).removeValue();
                    removeFromUsersChat.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            isLeavingChat = true;
                            Message leaving_message = new Message(USER_ID,author,USER_ID + "has left the chat");
                            Log.d("LeavingMessage","sent");
                            Task<Void> final_message = FirebaseDatabase.getInstance().getReference("chat_msgs").child(CHAT_ID).push().setValue(leaving_message);
                            final_message.addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent backtodialog = new Intent(MessageActivity.this, DialogsActivity.class);
                                    backtodialog.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(backtodialog);
                                    finish();
                                }
                            });

                        }
                    });
                }
            });



            return true;

        }else if (id == R.id.menu_editGroup) {
            //  logoutUser();
            Log.d("menu Item", "Add Member");
            Intent editGroup = new Intent(MessageActivity.this, ProfileActivity.class);
            editGroup.putExtra("editGroup", true);
            editGroup.putExtra("chatId", CHAT_ID);
            startActivity(editGroup);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void storeTemporaryImage(final DatabaseReference dbReference,final Uri uri){
        Message tempMessage = new Message(USER_ID, author, null);
        Message.Image new_img = new Message.Image(LOADING_IMAGE_URL);
        tempMessage.setImage(new_img);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName = "IMG_" + timeStamp + ".jpg" ;


        dbReference.push()
                .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            String key = databaseReference.getKey();
                            StorageReference storageReference =
                                    FirebaseStorage.getInstance()
                                            .getReference("chats")
                                            .child(CHAT_ID)
                                            .child(imageFileName);
                            Log.d("putImage", "about to be called: " + key );
                            putImageInStorage(storageReference, uri, key);
                        } else {
                            Log.w(TAG, "Unable to write message to database.",
                                    databaseError.toException());
                        }
                    }
                });
    }

    private void putImageInStorage(final StorageReference storageReference, Uri uri, final String key) {

        UploadTask uploadTask = storageReference.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL

                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    final Uri downloadUri = task.getResult();
                    Message.Image up_img = new Message.Image(downloadUri.toString());
                    Message message_update =
                            new Message(USER_ID, author, null);
                    message_update.setImage(up_img);
                    myRef.child(key)
                            .setValue(message_update);

                    Log.d("downloadURI?", downloadUri.toString());
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp ;
        Log.d("timestamp", timeStamp);
        Log.d("dateee", new Date().toString());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("storagedir", storageDir.toString() );
        try {
            File image = new File(storageDir.toString() + "/" + imageFileName + ".jpg");
            image.createNewFile();
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }catch (Exception e ){
            Log.e("fileException", e.toString());
            return null;
        }
    }
    @Override
    public void onBackPressed() {
        Intent dialogsListIntent = new Intent(MessageActivity.this, DialogsActivity.class);
        dialogsListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(dialogsListIntent);
    }

    public void startMessagesListener(){

    }
}
