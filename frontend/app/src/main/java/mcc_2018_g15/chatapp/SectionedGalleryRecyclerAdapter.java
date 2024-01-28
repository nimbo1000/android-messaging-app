package mcc_2018_g15.chatapp;

import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;


import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;

import java.util.ArrayList;

/**
 * Created by sonu on 24/07/17.
 */

public class SectionedGalleryRecyclerAdapter extends RecyclerView.Adapter<SectionedGalleryRecyclerAdapter.SectionViewHolder> {


    private int totalImagesCount =0;

    class SectionViewHolder extends RecyclerView.ViewHolder {
        private TextView sectionLabel;
        private RecyclerView imageRecyclerView;

        public SectionViewHolder(View itemView) {
            super(itemView);
            sectionLabel = (TextView) itemView.findViewById(R.id.date_label);
            imageRecyclerView = (RecyclerView) itemView.findViewById(R.id.image_recycler_view);
        }
    }

    private Context context;
    private ArrayList<SectionModel> sectionModelArrayList;

    public SectionedGalleryRecyclerAdapter(Context context,  ArrayList<SectionModel> sectionModelArrayList) {
        this.context = context;
        this.sectionModelArrayList = sectionModelArrayList;
        this.totalImagesCount = 0;

    }



    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_gallery, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        final SectionModel sectionModel = sectionModelArrayList.get(position);
        holder.sectionLabel.setText(sectionModel.getSectionLabel());

        //recycler view for items
        holder.imageRecyclerView.setHasFixedSize(true);
        holder.imageRecyclerView.setNestedScrollingEnabled(false);
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            holder.imageRecyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        }
        else{
            holder.imageRecyclerView.setLayoutManager(new GridLayoutManager(context, 8));
        }
        

            ImageRecyclerViewAdapter adapter = new ImageRecyclerViewAdapter(context, sectionModel.getItemArrayList(),holder.getAdapterPosition());
            holder.imageRecyclerView.setAdapter(adapter);



        //show toast on click of show all button


    }

    @Override
    public int getItemCount() {
        return sectionModelArrayList.size();
    }


}