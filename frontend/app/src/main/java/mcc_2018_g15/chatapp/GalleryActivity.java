package mcc_2018_g15.chatapp;

    import android.content.Intent;
    import android.content.res.Configuration;
    import android.os.Environment;
    import android.provider.ContactsContract;
    import android.support.annotation.NonNull;
    import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
    import android.support.v7.widget.LinearLayoutManager;
    import android.support.v7.widget.RecyclerView;
    import android.support.v7.widget.Toolbar;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.GridView;
    import android.widget.Toast;

    import com.facebook.cache.disk.DiskCacheConfig;
    import com.facebook.drawee.backends.pipeline.Fresco;
    import com.facebook.drawee.view.SimpleDraweeView;
    import com.facebook.imagepipeline.core.ImagePipelineConfig;
    import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import com.stfalcon.frescoimageviewer.ImageViewer;

    import java.io.File;
    import java.io.IOException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.function.Supplier;

//TODO: Orientation change closes the viewer. Back Button for view. Implement Download of images. (Maybe) Ability to navigate
// through all images when opening one.
public class GalleryActivity extends AppCompatActivity {
    RecyclerView galleryRecycler;
    private static String CHAT_ID, USER_ID ;
    private static int SORTING_OPTION = 1;
    private String resolution;
    private long USER_JOIN_TIME;
    public static boolean wasDialogShown = false;
    public static int currentPosition = 0,currentViewPosition =0 ;


    private static final String KEY_IS_DIALOG_SHOWN = "IS_DIALOG_SHOWN";
    private static final String KEY_CURRENT_POSITION = "CURRENT_POSITION";
    private static final String KEY_CURRENT_VIEW_POSITION ="CURRENT_VIEW_POSITION" ;
    public static Map<String,ArrayList<String>> image_message_id = new LinkedHashMap<>();



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            wasDialogShown = savedInstanceState.getBoolean(KEY_IS_DIALOG_SHOWN);
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
            currentViewPosition = savedInstanceState.getInt(KEY_CURRENT_VIEW_POSITION);

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_IS_DIALOG_SHOWN, ImageRecyclerViewAdapter.isDialogShown);
        outState.putInt(KEY_CURRENT_POSITION, ImageRecyclerViewAdapter.currentPosition);
        outState.putInt(KEY_CURRENT_VIEW_POSITION,ImageRecyclerViewAdapter.currentViewPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_recycler);



        Toolbar toolbar = (Toolbar) findViewById(R.id.gallery_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Shared Images");
        }
        toolbar.inflateMenu(R.menu.menu_gallery);


        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this).setBaseDirectoryName("cache")
                .setBaseDirectoryPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                .build();

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this).setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                //.setDownsampleEnabled(true)
                .setDiskCacheEnabled(true)
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();

        Fresco.initialize(this,config);


        Log.d("fresco", "initialized");

        CHAT_ID = getIntent().getStringExtra("chatId");
        USER_ID = getIntent().getStringExtra("userId");
        USER_JOIN_TIME = getIntent().getLongExtra("userJoinTime",0);

        DatabaseReference resolution_check = FirebaseDatabase.getInstance().getReference("users").child(USER_ID);
        resolution_check.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               String imgRes = dataSnapshot.child("imgRes").getValue(String.class);

               if(imgRes.equals("high")){
                   resolution = "high_res_url";
               }
               else if(imgRes.equals("low")){
                   resolution = "low_res_url";
               }
               else {
                   resolution = "url";
               }
                if (imgRes.equals("full")){
                    resolution = "url";
                }
                Log.i("galleryResolution", resolution);
                setUpRecyclerView();
                if (SORTING_OPTION == 3){
                    getAllImages_label();
                }
                else if (SORTING_OPTION ==2){
                    getAllImages_user();
                } else{
                    getAllImages_date();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_date) {
            if (SORTING_OPTION == 1){
                Toast.makeText(GalleryActivity.this, "Already sorted by date", Toast.LENGTH_SHORT).show();
            }
            else{
                getAllImages_date();
                SORTING_OPTION = 1;
            }
            return true;
        } else if (id == R.id.menu_user) {
            if (SORTING_OPTION == 2){
                Toast.makeText(GalleryActivity.this, "Already sorted by user", Toast.LENGTH_SHORT).show();
            }
            else{
                getAllImages_user();
                SORTING_OPTION = 2;
            }
            return true;
        } else if (id == R.id.menu_label){
            if (SORTING_OPTION == 3){
                Toast.makeText(GalleryActivity.this, "Already sorted by label", Toast.LENGTH_SHORT).show();
            }
            else{
                getAllImages_label();
                SORTING_OPTION=3;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void setUpRecyclerView() {
        galleryRecycler = (RecyclerView) findViewById(R.id.recycler);
        galleryRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        galleryRecycler.setLayoutManager(linearLayoutManager);
    }

    //populate recycler view
    private void populateRecyclerView(Map<String, ArrayList<String>> sorted_data) {
        Log.d("populaterecycler", "Inside populate");
        ArrayList<SectionModel> sections = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : sorted_data.entrySet()) {
            Log.d("populaterecycler", entry.getKey());
            sections.add(new SectionModel(entry.getKey(),entry.getValue()));
        }
        Log.d("populaterecycler", "Sections:- " + sections.toString());
        SectionedGalleryRecyclerAdapter adapter = new SectionedGalleryRecyclerAdapter(GalleryActivity.this, sections);
        galleryRecycler.setAdapter(adapter);

    }


    private void getAllImages_date(){
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("image_urls").child(CHAT_ID);
        final Map<String,ArrayList<String>> image_sorter = new LinkedHashMap<>();
        image_message_id.clear();


        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.e("Count " ,""+snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        if(postSnapshot.hasChild("createdAt")) {
                            Date image_time = postSnapshot.child("createdAt").getValue(Date.class);
                            String full_res = postSnapshot.child("url").getValue(String.class);
                            String high_res = postSnapshot.child("high_res_url").getValue(String.class);
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(image_time);

                            String low_res = postSnapshot.child("low_res_url").getValue(String.class);

                            ArrayList<String> message_id = new ArrayList<String>();
                            message_id.add(full_res);
                            message_id.add(high_res);
                            message_id.add(low_res);
                            message_id.add(timeStamp);


                            String image_date = new SimpleDateFormat("MMMM dd,yyyy").format(image_time);
                            String image_path = postSnapshot.child(resolution).getValue(String.class);

                            if(image_time.getTime() > USER_JOIN_TIME ) {
                                if (image_path == null) {
                                    Log.d("image_path_null", "true");
                                    image_path = postSnapshot.child("url").getValue(String.class);
                                }
                                if (image_sorter.get(image_date) == null) {
                                    ArrayList<String> new_element = new ArrayList<>();
                                    new_element.add(image_path);
                                    image_sorter.put(image_date, new_element);

                                } else {
                                    image_sorter.get(image_date).add(image_path);
                                }
                                image_message_id.put(image_path,message_id);
                            }
                            Log.d("haschild", image_sorter.toString());
                        }
                }

                Map<String,ArrayList<String>> ordered_data = new LinkedHashMap<>();
                List<String> keyList = new ArrayList<String>(image_sorter.keySet());
                List<ArrayList<String>> images_list = new ArrayList<>(image_sorter.values());
                for(int i=image_sorter.size()-1; i>=0; i--){
                    Collections.reverse(images_list.get(i));
                    ordered_data.put(keyList.get(i),images_list.get(i));
                }
                Log.d("ordered data", ordered_data.toString());
                populateRecyclerView(ordered_data);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());

            }


        });
    }

    private void getAllImages_user() {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("image_urls").child(CHAT_ID);
        final Map<String,ArrayList<String>> image_sorter = new HashMap<>();
        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        image_message_id.clear();


        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.e("Count " ,""+snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    if(postSnapshot.hasChild("id")) {
                        Date image_time = postSnapshot.child("createdAt").getValue(Date.class);
                        String full_res = postSnapshot.child("url").getValue(String.class);
                        String high_res = postSnapshot.child("high_res_url").getValue(String.class);
                        String low_res = postSnapshot.child("low_res_url").getValue(String.class);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(image_time);


                        ArrayList<String> message_id = new ArrayList<String>();
                        message_id.add(full_res);
                        message_id.add(high_res);
                        message_id.add(low_res);
                        message_id.add(timeStamp);


                        String user_id = postSnapshot.child("id").getValue(String.class);
                        String image_path = postSnapshot.child(resolution).getValue(String.class);
                        if(image_time.getTime() > USER_JOIN_TIME ) {

                            if (image_path == null) {
                                Log.d("image_path_null", "true");
                                image_path = postSnapshot.child("url").getValue(String.class);
                            }
                            if (image_sorter.get(user_id) == null) {
                                ArrayList<String> new_element = new ArrayList<>();
                                new_element.add(image_path);
                                image_sorter.put(user_id, new_element);
                            } else {
                                image_sorter.get(user_id).add(image_path);
                            }
                            image_message_id.put(image_path,message_id);
                            Log.d("haschild", image_sorter.toString());
                        }
                    }
                }

                final List<String> keyList = new ArrayList<String>(image_sorter.keySet());
                final List<ArrayList<String>> images_list = new ArrayList<>(image_sorter.values());
                final Map<String,ArrayList<String>> resorted = new HashMap<>();

                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("insideUserref", "now");
                        for(int i=0; i<=image_sorter.size()-1; i++){
                            String username = dataSnapshot.child(keyList.get(i)).child("name").getValue(String.class);
                            resorted.put(username,images_list.get(i));
                            Log.d("insideUserref", resorted.toString());

                        }
                        populateRecyclerView(resorted);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());

            }
        });

        final List<String> keyList = new ArrayList<String>(image_sorter.keySet());
        final List<ArrayList<String>> images_list = new ArrayList<>(image_sorter.values());
        final Map<String,ArrayList<String>> resorted = new HashMap<>();

    }

    private void getAllImages_label() {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance().getReference("image_urls").child(CHAT_ID);
        final Map<String,ArrayList<String>> image_sorter = new HashMap<>();
        image_message_id.clear();


        imagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.e("Count " ,"" + snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        Date image_time = postSnapshot.child("createdAt").getValue(Date.class);
                    String full_res = postSnapshot.child("url").getValue(String.class);
                    String high_res = postSnapshot.child("high_res_url").getValue(String.class);
                    String low_res = postSnapshot.child("low_res_url").getValue(String.class);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(image_time);



                    ArrayList<String> message_id = new ArrayList<String>();
                    message_id.add(full_res);
                    message_id.add(high_res);
                    message_id.add(low_res);
                    message_id.add(timeStamp);

                        String label = postSnapshot.child("label").getValue(String.class);
                        String image_path = postSnapshot.child(resolution).getValue(String.class);

                    if(image_time.getTime() > USER_JOIN_TIME ) {

                        if (image_path == null) {
                            Log.d("image_path_null", "true");
                            image_path = postSnapshot.child("url").getValue(String.class);
                        }
                        if (image_sorter.get(label) == null) {
                            ArrayList<String> new_element = new ArrayList<>();
                            new_element.add(image_path);
                            image_sorter.put(label, new_element);
                        } else {
                            image_sorter.get(label).add(image_path);
                        }
                        image_message_id.put(image_path,message_id);
                        Log.d("haschild", image_sorter.toString());
                    }
                }

                if(image_sorter.get("others")!=null) {
                    ArrayList<String> others = image_sorter.get("others");
                    image_sorter.remove("others");
                    image_sorter.put("others",others);
                }

                populateRecyclerView(image_sorter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());

            }
        });
    }

}
