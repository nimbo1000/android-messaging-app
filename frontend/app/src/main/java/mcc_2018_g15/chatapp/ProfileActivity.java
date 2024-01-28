package mcc_2018_g15.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.angmarch.views.NiceSpinner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private DatabaseReference usersRef;
    private DatabaseReference databaseRef;
    private EditText editTextUsername;
    private ImageView imageViewAvatar;
    private ProgressDialog progressDialog;
    private Uri updatedAvatarUri;
    private String orgAvatarUri;
    private String orgUsername;
    private String userId;
    private String callingActivity = "";
    private String chatId = "";
    boolean editGroup = false;
    NiceSpinner niceSpinner;
    private int originalImgRes;
    private int FULL_RESOLUTION = 0;
    private int HIGH_RESOLUTION = 1;
    private int LOW_RESOLUTION = 2;
    private TextView userNameLabel,imgResLabel;
    private static final String DEFAULT_GROUP_IMAGE= "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2018-g15.appspot.com/o/group_deault.jpg?alt=media&token=74147f81-2acd-4166-aa2e-434fe1f34be6";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        if (intent.hasExtra("callingActivity"))
            callingActivity = intent.getStringExtra("callingActivity");
        if(intent.hasExtra("editGroup")){
            editGroup = true;
        }

        userNameLabel = findViewById(R.id.username_txtview);
        imgResLabel = findViewById(R.id.image_resolution);
        niceSpinner = findViewById(R.id.spinner1);
        List<String> dataset = new LinkedList<>(Arrays.asList("Full", "High", "Low"));
        niceSpinner.attachDataSource(dataset);
        userId = FirebaseAuth.getInstance().getUid();


        editTextUsername = findViewById(R.id.editTextUsername);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        final Button buttonSave = findViewById(R.id.buttonSave);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        progressDialog = new ProgressDialog(this);

        if (callingActivity.equals("SearchUsersActivity")) {
            editTextUsername.setHint("Group name");
            userNameLabel.setText("Group Name");
            Glide.with(this).load(DEFAULT_GROUP_IMAGE).apply(new RequestOptions().fitCenter()).into(imageViewAvatar);
            niceSpinner.setVisibility(View.GONE);
            imgResLabel.setVisibility(View.GONE);

            if (intent.hasExtra("chatId"))
                chatId = intent.getStringExtra("chatId");

        } else if(editGroup){
            editTextUsername.setHint("Group name");
            userNameLabel.setText("Group Name");
            Glide.with(this).load(DEFAULT_GROUP_IMAGE).apply(new RequestOptions().fitCenter()).into(imageViewAvatar);
            niceSpinner.setVisibility(View.GONE);
            imgResLabel.setVisibility(View.GONE);

            if (intent.hasExtra("chatId"))
                chatId = intent.getStringExtra("chatId");
            displayCurrentInfo();
        } else {
            getProfileData();
        }

        imageViewAvatar.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                chooseImage();
            }
        });
        buttonSave.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (callingActivity.equals("SearchUsersActivity") || editGroup)
                    saveGroupInfo();
                else
                    updateUser();
            }
        });


    }

    private void displayCurrentInfo() {
        databaseRef.child("chats").child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editTextUsername.setText(dataSnapshot.child("dialogName").getValue().toString());
                editTextUsername.setSelection(editTextUsername.getText().length());
                userNameLabel.setText("Group Name");
                orgAvatarUri = dataSnapshot.child("dialogPhoto").getValue().toString();
                Glide.with(ProfileActivity.this).load(orgAvatarUri).apply(new RequestOptions().fitCenter()).into(imageViewAvatar);
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveGroupInfo() {
        final String newUsername = editTextUsername.getText().toString().trim();
        if (newUsername.isEmpty()) {
            Toast.makeText(this,"Please enter group name",Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Saving");
        progressDialog.show();
        if (updatedAvatarUri != null) {
            final StorageReference storageReference =
                    FirebaseStorage.getInstance()
                            .getReference("chats")
                            .child(chatId)
                            .child("dialogPhoto");

            UploadTask uploadTask = storageReference.putFile(updatedAvatarUri);

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
//                        User newUser = new User(username, orgAvatarUri, "full", "dark");
//                        usersRef.child(userId).setValue(newUser);
                        if(!newUsername.isEmpty())
                            databaseRef.child("chats").child(chatId).child("dialogName").setValue(newUsername);
                        databaseRef.child("chats").child(chatId).child("dialogPhoto").setValue(task.getResult().toString());
                        progressDialog.dismiss();
                        Intent chatIntent = new Intent(ProfileActivity.this, MessageActivity.class);
                        chatIntent.putExtra("chatId", chatId);
                        startActivity(chatIntent);

                        finish();

                    }
                    progressDialog.dismiss();
                }
            });
        } else {
            if(!newUsername.isEmpty())
                databaseRef.child("chats").child(chatId).child("dialogName").setValue(newUsername);
//            databaseRef.child("chats").child(chatId).child("dialogPhoto").setValue("https://firebasestorage.googleapis.com/v0/b/mcc-fall-2018-g15.appspot.com/o/group_deault.jpg?alt=media&token=74147f81-2acd-4166-aa2e-434fe1f34be6");
            progressDialog.dismiss();
            Intent chatIntent = new Intent(ProfileActivity.this, MessageActivity.class);
            chatIntent.putExtra("chatId", chatId);
            startActivity(chatIntent);

            finish();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream inputStream;
        if (requestCode == PICK_IMAGE) {
            if(data!=null) {
                updatedAvatarUri = data.getData();
                if (updatedAvatarUri != null) {
                    try {
                        inputStream = getContentResolver().openInputStream(updatedAvatarUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        Bitmap compressedBitmap = resizeBitmap(bitmap, 320);
                        imageViewAvatar.setImageBitmap(compressedBitmap);
                    } catch (IOException e) {

                    }
                }
            }
        }

    }

    private void getProfileData() {
        if (userId == null) {
            return;
        }
        progressDialog.setMessage("Loading");
        progressDialog.show();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                orgUsername = snapshot.child("name").getValue().toString();
                editTextUsername.setText(orgUsername);
                editTextUsername.setSelection(editTextUsername.getText().length());


                String imageResolution = snapshot.child("imgRes").getValue(String.class);
                if (imageResolution.equals("low")){
                    niceSpinner.setSelectedIndex(LOW_RESOLUTION);
                }
                else if (imageResolution.equals("high")) {
                    niceSpinner.setSelectedIndex(HIGH_RESOLUTION);
                } else {
                    niceSpinner.setSelectedIndex(FULL_RESOLUTION);
                }


                originalImgRes = niceSpinner.getSelectedIndex();
                orgAvatarUri = snapshot.child("avatar").getValue(String.class);
                Glide.with(ProfileActivity.this).load(orgAvatarUri).apply(new RequestOptions().fitCenter()).into(imageViewAvatar);
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }


    private Bitmap resizeBitmap(Bitmap mBitMap, int maxSize) {
        int width = mBitMap.getWidth();
        int height = mBitMap.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(mBitMap, width, height, true);
    }

    private void updatedUserNameAndPhoto(final String username, final Uri imageUri, final int imgRes) {

        final String newRes;
        if (imgRes == LOW_RESOLUTION){
            newRes = "low";
        } else if (imgRes == HIGH_RESOLUTION){
            newRes = "high";
        }else{
            newRes="full";
        }


        if (updatedAvatarUri != null) {
            final StorageReference storageReference =
                    FirebaseStorage.getInstance()
                            .getReference("users")
                            .child(userId)
                            .child("avatar");

            UploadTask uploadTask = storageReference.putFile(imageUri);

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

                        usersRef.child(userId).child("name").setValue(username);
                        usersRef.child(userId).child("avatar").setValue(task.getResult().toString());
                        usersRef.child(userId).child("imgRes").setValue(newRes);
                        startActivity(new Intent(ProfileActivity.this, DialogsActivity.class));
                        finish();

                    }
                    progressDialog.dismiss();
                }
            });
        } else {
            usersRef.child(userId).child("name").setValue(username);
            usersRef.child(userId).child("avatar").setValue(orgAvatarUri);
            usersRef.child(userId).child("imgRes").setValue(newRes);
            progressDialog.dismiss();
            startActivity(new Intent(ProfileActivity.this, DialogsActivity.class));
            finish();
        }
    }

    private void updateUser() {
        final String newUsername = editTextUsername.getText().toString().trim();

        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Updating");
        progressDialog.show();
        niceSpinner.getSelectedIndex();


        if (updatedAvatarUri == null && newUsername.equals(orgUsername) && (originalImgRes == niceSpinner.getSelectedIndex())) {
            progressDialog.dismiss();
            startActivity(new Intent(ProfileActivity.this, DialogsActivity.class));
            finish();
            return;
        }

        usersRef.orderByChild("name").equalTo(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !newUsername.equals(orgUsername)) {
                    Toast.makeText(ProfileActivity.this, "Username already exist"
                            , Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                } else {
                    updatedUserNameAndPhoto(newUsername, updatedAvatarUri, niceSpinner.getSelectedIndex());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
}
