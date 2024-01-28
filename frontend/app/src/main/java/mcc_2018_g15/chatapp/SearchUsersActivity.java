package mcc_2018_g15.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchUsersActivity extends AppCompatActivity {

    private Button btnCreateGroup;
    private RecyclerView chatsList;
    private DatabaseReference databaseRef;
    private Query query;
    private boolean isGroup = false;
    private String USER_ID = "user_id";
    private ArrayList<String> usersChats = new ArrayList<>();
    private ArrayList<String> connectedPeople = new ArrayList<>();
    private ArrayList<String> groupMembers = new ArrayList<>();
    private ArrayList<String> previousMembers = new ArrayList<>();
    FirebaseAuth firebaseAuth;
    boolean isAddingMember = false;
    boolean isAdmin = true;
    String chatId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        USER_ID = firebaseAuth.getUid();

        btnCreateGroup = (Button) findViewById(R.id.btnCreateGroup);

        Intent intent = getIntent();
        if (intent.hasExtra("isGroup"))
            isGroup = intent.getBooleanExtra("isGroup", false);
        if (intent.hasExtra("addMember")) {
            isAdmin = intent.getBooleanExtra("isAdmin", false);
            isAddingMember = intent.getBooleanExtra("addMember", false);
            chatId = intent.getStringExtra("chatId");

            if (isAddingMember) {
                isGroup = true;
                btnCreateGroup.setText("Add members");
            }
        }

        chatsList = (RecyclerView) findViewById(R.id.chats_list);
        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(new LinearLayoutManager(this));


        databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = databaseRef.child("users");
        DatabaseReference usersChatsRef = usersRef.child(USER_ID).child("user_chats");
        final DatabaseReference chatsRef = databaseRef.child("chats");



        SearchView sv = (SearchView) findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                if(queryText.length()<3)
                    return true;
                query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("name").startAt(queryText).endAt(queryText + "\uf8ff").limitToLast(50);
                FirebaseRecyclerOptions<User> options =
                        new FirebaseRecyclerOptions.Builder<User>()
                                .setQuery(query, User.class)
                                .setLifecycleOwner(SearchUsersActivity.this)
                                .build();
                FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<User, chatViewHolder>(options) {
                    @Override
                    public chatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        return new chatViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chats_list_item, parent, false));
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final chatViewHolder holder, int position, @NonNull User model) {
                        holder.setName(model.getName());
                        holder.setAvatar(model.getAvatar());

                        final String userId = getRef(position).getKey();
                        if (isGroup) {
                            holder.setCheckboxVisibility(View.VISIBLE);


                            if (groupMembers.contains(userId)) {
//                            holder.view.setBackgroundColor(getResources().getColor(R.color.blue));
                                if(holder.checkboxEnabled())
                                    holder.setCheckbox(true);
                            } else {
                                if(holder.checkboxEnabled())
                                holder.setCheckbox(false);
                            }
                            if(!isAdmin&&previousMembers.contains(userId)){
                                holder.setEnabled(false);
                            }
                        } else {
                            holder.setCheckboxVisibility(View.GONE);
                        }
                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isGroup) {
                                    if (groupMembers.contains(userId)) {
                                        if(holder.checkboxEnabled())
                                            holder.setCheckbox(false);
//                                        groupMembers.remove(userId);
                                    } else {
                                        if(holder.checkboxEnabled())
                                            holder.setCheckbox(true);
//                                        groupMembers.add(userId);
                                    }
//                                    chatsRef.child(chatID).child("isGroup").setValue(false);
                                    //databaseRef.child("chats").child(databaseReference.getKey()).child("dialogName").setValue("test");
                                    //databaseRef.child("chats").child(databaseReference.getKey()).child("dialogPhoto").setValue("test");
                                } else {
//                                    chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (connectedPeople.contains(userId)) {
                                        Intent chatIntent = new Intent(getBaseContext(), MessageActivity.class);
                                        chatIntent.putExtra("chatId", usersChats.get(connectedPeople.indexOf(userId)));
                                        startActivity(chatIntent);
                                    } else {
                                        chatsRef.push()
                                                .setValue("", new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError,
                                                                           DatabaseReference databaseReference) {
                                                        String chatID = databaseReference.getKey();
                                                        //chatsRef.child(chatID).child("lastMessage").setValue("");
                                                        chatsRef.child(chatID).child("isGroup").setValue(false);
                                                        chatsRef.child(chatID).child("users").child(USER_ID).setValue(System.currentTimeMillis());
                                                        chatsRef.child(chatID).child("users").child(userId).setValue(System.currentTimeMillis());
                                                        chatsRef.child(chatID).child("admin").setValue(USER_ID);
                                                        databaseRef.child("users").child(userId).child("user_chats").child(chatID).setValue("user");
                                                        databaseRef.child("users").child(USER_ID).child("user_chats").child(chatID).setValue("admin");

                                                        Intent chatIntent = new Intent(getBaseContext(), MessageActivity.class);
                                                        chatIntent.putExtra("chatId", chatID);
                                                        startActivity(chatIntent);
                                                    }
                                                });
                                    }
                                }
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                        }
//                                    });
//                                }
                            }
                        });
                        CheckBox cb = (CheckBox) holder.view.findViewById(R.id.cbGroupMember);
                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if(b){
                                    if (!groupMembers.contains(userId)) {
//                                    holder.setCheckbox(b);
                                        groupMembers.add(userId);
                                    }
                                }else{
                                    if (groupMembers.contains(userId)) {
//                                    holder.setCheckbox(b);
                                        groupMembers.remove(userId);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onDataChanged() {
                        // If there are no chat messages, show a view that invites the user to add a message.
                        // mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                };
                chatsList.setAdapter(adapter);
                return true;
            }
        });

        usersChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    chatsRef.child(child.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean isGroupChat = (boolean) dataSnapshot.child("isGroup").getValue();

                            if (!isGroupChat) {
                                for (DataSnapshot user : dataSnapshot.child("users").getChildren()) {
                                    if (!user.getKey().equals(USER_ID)) {
                                        usersChats.add(dataSnapshot.getKey());
                                        connectedPeople.add(user.getKey());
                                    }
                                    if (dataSnapshot.child("users").getChildrenCount() == 1) {
                                        usersChats.add(dataSnapshot.getKey());
                                        connectedPeople.add(user.getKey());
                                    }
                                }
                            }
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


        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                btnCreateGroup.setClickable(false);
                btnCreateGroup.setEnabled(false);
                if (isAddingMember) {
                    for (int i = 0; i < groupMembers.size(); i++) {
                        if(!previousMembers.contains(groupMembers.get(i))) {
                            chatsRef.child(chatId).child("users").child(groupMembers.get(i)).setValue(System.currentTimeMillis());
                            databaseRef.child("users").child(groupMembers.get(i)).child("user_chats").child(chatId).setValue("admin");
                        }
                    }
                    for(int i=0; i<previousMembers.size(); i++){
                        if(!groupMembers.contains(previousMembers.get(i))){
                            chatsRef.child(chatId).child("users").child(previousMembers.get(i)).removeValue();
                            databaseRef.child("users").child(previousMembers.get(i)).child("user_chats").child(chatId).removeValue();
                        }
                    }
                    finish();
                } else {
                    chatsRef.push()
                            .setValue("", new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    int numberOfMembers = 3;
                                    if(!groupMembers.contains(USER_ID))
                                        numberOfMembers = 2;
                                    if (groupMembers.size() < numberOfMembers) {
                                        Toast.makeText(SearchUsersActivity.this, "Please select at least three people to create a group!", Toast.LENGTH_LONG).show();

                                        //Snackbar.make(view, "Please select at least three people!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        return;
                                    }
                                    String chatID = databaseReference.getKey();
                                    //chatsRef.child(chatID).child("lastMessage").setValue("");
                                    chatsRef.child(chatID).child("isGroup").setValue(true);
                                    chatsRef.child(chatID).child("dialogPhoto").setValue("https://firebasestorage.googleapis.com/v0/b/mcc-fall-2018-g15.appspot.com/o/group_deault.jpg?alt=media&token=74147f81-2acd-4166-aa2e-434fe1f34be6");
                                    chatsRef.child(chatID).child("dialogName").setValue("");
                                    chatsRef.child(chatID).child("users").child(USER_ID).setValue(System.currentTimeMillis());
                                    chatsRef.child(chatID).child("admin").setValue(USER_ID);
                                    for (int i = 0; i < groupMembers.size(); i++) {
                                        chatsRef.child(chatID).child("users").child(groupMembers.get(i)).setValue(System.currentTimeMillis());
                                        databaseRef.child("users").child(groupMembers.get(i)).child("user_chats").child(chatID).setValue("user");
                                    }
                                    databaseRef.child("users").child(USER_ID).child("user_chats").child(chatID).setValue("admin");

                                    Intent chatIntent = new Intent(getBaseContext(), ProfileActivity.class);
                                    chatIntent.putExtra("chatId", chatID);
                                    chatIntent.putExtra("callingActivity", "SearchUsersActivity");
                                    startActivity(chatIntent);
                                }
                            });
                }
                finish();
            }
        });

        if (isGroup) {
            btnCreateGroup.setVisibility(View.VISIBLE);
        } else {
            btnCreateGroup.setVisibility(View.GONE);
        }

        if(isAddingMember){
            chatsRef.child(chatId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot user : dataSnapshot.getChildren()){
                        previousMembers.add(user.getKey());
                        groupMembers.add(user.getKey());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

//        Databaza.child("new").setValue("value");
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder {
        View view;
        String userId;

        public chatViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setAvatar(String url) {
            ImageView avatarIV = (ImageView) view.findViewById(R.id.avatar_iv);
            Glide.with(avatarIV.getContext()).load(url).into(avatarIV);
        }

        public void setName(String lastMessage) {
            TextView name_tv = (TextView) view.findViewById(R.id.name_tv);
            name_tv.setText(lastMessage);
        }

        public void setCheckbox(boolean selected) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbGroupMember);
            checkBox.setChecked(selected);
        }

        public void setCheckboxVisibility(int visibility) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbGroupMember);
            checkBox.setVisibility(visibility);

        }

        public void setEnabled(boolean b) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbGroupMember);
            checkBox.setEnabled(b);
        }
        public boolean checkboxEnabled() {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbGroupMember);
            return checkBox.isEnabled();
        }
    }
}
