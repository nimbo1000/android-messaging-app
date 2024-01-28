package mcc_2018_g15.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DialogsActivity extends AppCompatActivity{

    private static final String TAG = "MessageActivity";
    private static String USER_ID = "user_id";
    DatabaseReference myRef;
    Query myQuery;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;

    private Boolean isFabOpen = false;
    private FloatingActionButton fab,fab1,fab2;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        firebaseAuth = FirebaseAuth.getInstance();
        USER_ID = firebaseAuth.getUid();

        final DialogsList dialogsListView = (DialogsList)findViewById(R.id.dialogsList);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("users").child(USER_ID).child("user_chats");
        myQuery = database.getReference().child("users").child(USER_ID).child("user_chats").orderByChild("lastMessage/createdAt/time");

        final DialogsListAdapter dialogsListAdapter = new DialogsListAdapter<>(R.layout.custom_dialog_layout, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                try {
                    Picasso.get().load(url).into(imageView);
                }catch(Exception e){

                }
            }
        });

        dialogsListView.setAdapter(dialogsListAdapter);

        myQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String prevChildKey) {

                final Calendar calendar = Calendar.getInstance();
                DatabaseReference chatsRef = database.getReference().child("chats").child(dataSnapshot.getKey());
//                Dialog new_dialog = new Dialog(dataSnapshot.getKey(), "","",
//                        new ArrayList<Author>(), new Message("id", new Author("","",""), "", calendar.getTime()), 0);
//                dialogsListAdapter.addItem(0,new_dialog);
//                dialogsListView.scrollToPosition(0);
                chatsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot chatsDataSnapshot) {
                        final Dialog new_dialog = new Dialog();

                        new_dialog.setId(chatsDataSnapshot.getKey());

//                        new_dialog.setUsers(new ArrayList<Author>());

                        ArrayList<String> keyList = new ArrayList<>();
                        final ArrayList<String> usernames = new ArrayList<String>();
                        final ArrayList<Author> authorsList = new ArrayList<Author>();
//                        final int[] index = new int[] {0};
                        final long[] index = new long[1];
                        index[0] = 0;
                        for (final DataSnapshot child : chatsDataSnapshot.child("users").getChildren()) {
                            Log.e("!_@@_Key::>", child.getKey());
                            keyList.add( child.getValue().toString());
                            DatabaseReference usersRef = database.getReference().child("users").child(child.getKey());
                            index[0]++;
                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot usersDataSnapshot) {
                                    if(!usersDataSnapshot.getKey().equals(USER_ID)||chatsDataSnapshot.child("users").getChildrenCount()==1)
                                        usernames.add(usersDataSnapshot.child("name").getValue().toString());
                                    authorsList.add(new Author(usersDataSnapshot.getKey(), usersDataSnapshot.child("name").getValue().toString(), usersDataSnapshot.child("avatar").getValue().toString()));
                                    if(!chatsDataSnapshot.child("isGroup").getValue(Boolean.class)){
                                        if(chatsDataSnapshot.child("users").getChildrenCount()==1)
                                            new_dialog.setDialogPhoto(usersDataSnapshot.child("avatar").getValue().toString());
                                        else if(!usersDataSnapshot.getKey().equals(USER_ID))
                                            new_dialog.setDialogPhoto(usersDataSnapshot.child("avatar").getValue().toString());
                                    }

                                    if(index[0]==chatsDataSnapshot.child("users").getChildrenCount()){

                                        new_dialog.setUsers(authorsList);
                                        if(chatsDataSnapshot.child("isGroup").getValue(Boolean.class)){
                                            new_dialog.setDialogPhoto(chatsDataSnapshot.child("dialogPhoto").getValue(String.class));
                                            new_dialog.setDialogName(chatsDataSnapshot.child("dialogName").getValue(String.class));

                                            if(chatsDataSnapshot.child("dialogName").getValue(String.class).equals(""))
                                                new_dialog.setDialogName(TextUtils.join(", ", usernames));
                                        }
                                        else{
                                            new_dialog.setDialogName(TextUtils.join(", ", usernames));
                                        }
//                        new_dialog.setDialogName(TextUtils.join(", ", keyList));
                                        String messageId = "";
                                        String last_message="";
                                        String avatar="";
                                        String name="";
                                        String id="";
                                        try {   //last_message migh be added later than needed here
                                            if (chatsDataSnapshot.child("last_message").child("createdAt").child("time").getValue(Long.class) > chatsDataSnapshot.child("users").child(USER_ID).getValue(Long.class)) {
                                                messageId = (chatsDataSnapshot.child("last_message").child("id").getValue(String.class));
                                                last_message = (chatsDataSnapshot.child("last_message").child("text").getValue(String.class));
                                                avatar = (chatsDataSnapshot.child("last_message").child("avatar").getValue(String.class));
                                                name = (chatsDataSnapshot.child("last_message").child("user").child("name").getValue(String.class));
                                                id = (chatsDataSnapshot.child("last_message").child("id").getValue(String.class));
                                                calendar.setTimeInMillis(chatsDataSnapshot.child("last_message").child("createdAt").child("time").getValue(Long.class));

                                                if(last_message.equals(id + "has left the chat")){
                                                    Log.d("chatLeaving", "is being done");
                                                    last_message = name.trim() + " has left the chat";
                                                }
                                            }
                                        }catch(Exception e){}

                                        final String finalLastMessage = last_message;

                                        // TODO: 11/27/2018 update data below with data from last message object
                                        Message msg = new Message(messageId, new Author(id,name,avatar), finalLastMessage, calendar.getTime());



                                        new_dialog.setLastMessage(msg);
                                        dialogsListAdapter.updateItemById(new_dialog);
                                        if(dialogsListAdapter.getItemById(new_dialog.getId())==null)
                                            dialogsListAdapter.addItem(new_dialog);
                                        dialogsListAdapter.updateDialogWithMessage(dataSnapshot.getKey(), msg);
                                        dialogsListView.scrollToPosition(0);
                                    }
//                                    IDialog dialog = dialogsListAdapter.getItemById(chatsDataSnapshot.getKey());
//                                    if(dialog!=null) {
//                                        Message lastMessage = (Message) dialog.getLastMessage();
//                                        if(lastMessage.getUser().getId()==usersDataSnapshot.getKey()){
//                                            lastMessage.setImage(new Message.Image(usersDataSnapshot.child("avatar").getValue().toString()));
//                                        }
//                                    }

//                                    String chatImage = "";

//                                    String dialogName = TextUtils.join(", ", usernames);
//                                    Log.e(TAG, "onDataChange: " + chatsDataSnapshot.child("isGroup").getValue());
//                                    Log.e(TAG, "onDataChange: " + (Boolean)chatsDataSnapshot.child("isGroup").getValue());
//                                    if((Boolean) chatsDataSnapshot.child("isGroup").getValue()) {
//                                        try {
//                                            if (!chatsDataSnapshot.child("dialogName").getValue().toString().equals("")){
//                                                String dialogNameValue = chatsDataSnapshot.child("dialogName").getValue().toString();
//                                                if(!dialogNameValue.isEmpty()){
//                                                    dialogName=dialogNameValue;
//                                                }
//                                            }
//                                        } catch (Exception e) { }
//                                        try {
//                                            if (!chatsDataSnapshot.child("dialogPhoto").getValue().toString().equals(""))
//                                                chatImage = chatsDataSnapshot.child("dialogPhoto").getValue().toString();
//                                        } catch (Exception e) { }
//                                    }
//                                    if (chatsDataSnapshot.child("users").getChildrenCount()<3) {
//                                        chatImage = usersDataSnapshot.child("avatar").getValue().toString();
//                                    }
//                                    Dialog dlg = new Dialog(dialog.getId(), chatImage, dialogName, authorsList, lastMessage, dialog.getUnreadCount());
//                                    dialogsListAdapter.updateItemById(dlg);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                dialogsListAdapter.deleteById(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<Dialog>() {
            @Override
            public void onDialogClick(Dialog dialog) {
                Intent messageIntent = new Intent(DialogsActivity.this, MessageActivity.class);
                messageIntent.putExtra("chatId", dialog.getId()); //Optional parameters
                startActivity(messageIntent);
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFAB();
            }
        });

        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backwards);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFAB();
                Intent searchIntent = new Intent(DialogsActivity.this, SearchUsersActivity.class);
                searchIntent.putExtra("isGroup", false);
                startActivity(searchIntent);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFAB();
                Intent searchIntent = new Intent(DialogsActivity.this, SearchUsersActivity.class);
                searchIntent.putExtra("isGroup", true);
                startActivity(searchIntent);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dialog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            startActivity(profileIntent);
            return true;
        } else if (id == R.id.menu_logout){
            firebaseAuth.signOut();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
