package mcc_2018_g15.chatapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

public class GalleryGridAdapter extends BaseAdapter {

    private Context context;
    private final String[] imageurl;

    public GalleryGridAdapter(Context context, String[] imageurl) {
        this.context = context;
        this.imageurl = imageurl;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {


            gridView = new View(context);

            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.content_gallery, null);



            // set image based on selected text
//            ImageView imageView = (ImageView) gridView
//                    .findViewById(R.id.grid_item_image);
//            Picasso.get().load(imageurl[position]).into(imageView);
//
//            String mobile = imageurl[position];

            SimpleDraweeView drawee = (SimpleDraweeView) gridView.findViewById(R.id.grid_image);

            final int new_pos = position;
            Log.d("new_pos", String.valueOf(new_pos));
            drawee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ImageViewer.Builder<>(context,imageurl).setStartPosition(new_pos).show();
                }
            });
            drawee.setImageURI(imageurl[position]);





        } else {
            gridView = (View) convertView;
        }
        Log.d("grid view", "has returned");
        return gridView;
    }

    @Override
    public int getCount() {
        return imageurl.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
