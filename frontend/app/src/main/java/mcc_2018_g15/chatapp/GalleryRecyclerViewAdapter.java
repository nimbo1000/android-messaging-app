package mcc_2018_g15.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;

import java.util.HashMap;
import java.util.List;

public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<GalleryRecyclerViewAdapter.ViewHolder>  {


    String[] images;
    Context viewContext;
    String samp_img_url= "https://firebasestorage.googleapis.com/v0/b/mccchattest.appspot.com/o/chats%2F-LSBmE7sqAbavCu0-D87%2Fimage%3A156257?alt=media&token=cde92e6b-d6e9-49d9-bb46-6ca98253c21d";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    public GalleryRecyclerViewAdapter(String[] images){
        this.images = images;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView textGroupView;
        public GridView myGridView;




        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            textGroupView = (TextView) itemView.findViewById(R.id.text_group);
            myGridView = (GridView) itemView.findViewById(R.id.gridView);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        viewContext = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(viewContext);
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(viewContext)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(viewContext,config);

        // Inflate the custom layout
        View galleryView = inflater.inflate(R.layout.activity_gallery, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(galleryView);
        return viewHolder;
    }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            String[] imageurl = new String[]{samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url,samp_img_url,samp_img_url,LOADING_IMAGE_URL,samp_img_url};
            viewHolder.textGroupView.setText("Today");
            viewHolder.myGridView.setAdapter(new GalleryGridAdapter(viewContext,imageurl));
            Log.d("this is going on " , "now");


    }

    @Override
    public int getItemCount() {
        return images.length;
    }





}
