package mcc_2018_g15.chatapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.cache.common.WriterCallback;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;

class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ImageViewHolder> {

    public static int currentPosition,currentViewPosition;
    private int counter;
    private  int viewPosition;
    private View download_view;
    public static boolean isDialogShown;
    private CircleImageView img_btn;


    class ImageViewHolder extends RecyclerView.ViewHolder {
        String mCurrentPhotoPath;

        private SimpleDraweeView drawee;
        public ImageViewHolder(View itemView) {
            super(itemView);
            drawee = (SimpleDraweeView) itemView.findViewById(R.id.grid_image);
        }
    }

    private Context context;
    private ArrayList<String> arrayList;

    public ImageRecyclerViewAdapter(Context context, ArrayList<String> arrayList, int viewPosition) {
        this.context = context;
        this.arrayList = arrayList;
        this.viewPosition = viewPosition;
        this.counter = 0;
    }



    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_gallery, parent, false);
        download_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_view,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        counter +=1;

        holder.drawee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 img_btn = download_view.findViewById(R.id.circle_btn);
                if (download_view.getParent()!=null){
                    ((ViewGroup) download_view.getParent()).removeView(download_view);

                }
                isDialogShown = true;
                currentPosition = position;

                new ImageViewer.Builder<>(context, arrayList).setStartPosition(position)
                        .setOnDismissListener(getDismissListener())
                        .setOverlayView(download_view).setImageChangeListener(getImageChangeListener()).show();

            }
        });


        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        Uri uri = Uri.parse(arrayList.get(position));
        DataSource<Boolean> inMemoryCache = imagePipeline.isInDiskCache(uri);
        Log.d("inMemoryCache", String.valueOf(inMemoryCache.getResult()));

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(arrayList.get(position)))
                .setResizeOptions(new ResizeOptions(90, 90))
                .build();
        holder.drawee.setController(
                Fresco.newDraweeControllerBuilder()
                        .setOldController(holder.drawee.getController())
                        .setImageRequest(request)
                        .build());
        Log.d("wasDialogShown", String.valueOf(GalleryActivity.wasDialogShown));
        Log.d("itemCount", getItemCount() + "," + position);
        int itemcount = getItemCount();
        Log.d("counter", String.valueOf(viewPosition));

        if (viewPosition == GalleryActivity.currentViewPosition && (getItemCount() == (position+1))){
            Log.d("soiminhere", "true");

            if (GalleryActivity.wasDialogShown){
                Log.d("soiminhere", "false");
                new ImageViewer.Builder<>(context, arrayList)
                        .setStartPosition(GalleryActivity.currentPosition)
                        .setImageChangeListener(getImageChangeListener())
                        .setOnDismissListener(getDismissListener())
                        .setOverlayView(download_view)
                        .show();
                GalleryActivity.wasDialogShown= false;
            }

        }


    }

    private ImageViewer.OnDismissListener getDismissListener() {
        return new ImageViewer.OnDismissListener() {
            @Override
            public void onDismiss() {
                isDialogShown = false;
                GalleryActivity.wasDialogShown= false;
            }
        };
    }

    private ImageViewer.OnImageChangeListener getImageChangeListener() {
        return new ImageViewer.OnImageChangeListener() {
            @Override
            public void onImageChange(int position) {
                currentPosition = position;
                currentViewPosition = viewPosition;
                Log.d("insideImage",String.valueOf(currentViewPosition));

                final int new_position = position;
                img_btn = download_view.findViewById(R.id.circle_btn);

                img_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setItems(R.array.download_types_dialog, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ArrayList<String> message_id = GalleryActivity.image_message_id.get(arrayList.get(new_position));
                                        String select_img;
                                        String file_name = message_id.get(3);
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                                            return;
                                        }

                                        if (i == 0) {
                                            select_img = message_id.get(0);
                                            file_name+="_full";

                                        } else if (i == 1) {
                                            select_img = message_id.get(1);
                                            file_name+="_high";


                                        } else {
                                            select_img = message_id.get(2);
                                            file_name+="_low";

                                        }

                                        File storageDir = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Whisper");
                                        Log.d("storagedir", storageDir.toString());
                                        try {
                                            storageDir.mkdirs();

                                            final File image = new File(storageDir , file_name + ".jpg");
                                            //image.createNewFile();
                                            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(select_img);
                                            imageRef.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Toast.makeText(context, "Download successful", Toast.LENGTH_SHORT).show();
                                                }


                                            });

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    }).show();
                    }
                });
            }
        };
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp ;
        Log.d("timestamp", timeStamp);
        Log.d("dateee", new Date().toString());
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("storagedir", storageDir.toString() );
        try {
            File image = new File(storageDir.toString() + "/" + imageFileName + ".jpg");
            image.createNewFile();
            return image;
        }catch (Exception e ){
            Log.e("fileException", e.toString());
            return null;
        }
    }
}
