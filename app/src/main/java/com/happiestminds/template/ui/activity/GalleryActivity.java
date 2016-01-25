package com.happiestminds.template.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;



import com.bumptech.glide.Glide;
import com.happiestminds.template.R;
import com.happiestminds.template.service.genericservices.ImageCaptureType;


import java.util.ArrayList;
import java.util.List;



/**
 * Created by Venkatesh.Guddanti on 1/11/2016.
 */
public class GalleryActivity extends AppCompatActivity {

    ArrayList<String> catogoriesNames = new ArrayList<String>();
    ArrayList<String> catogoriesThumbnails = new ArrayList<String>();
    public static String MODE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerView gallery = (RecyclerView)findViewById(R.id.gallery_list);
        gallery.setLayoutManager(new GridLayoutManager(this,3,LinearLayoutManager.VERTICAL,false));
        MODE = getIntent().getExtras().getString("TYPE");

        String[] projection;
        if(MODE.equalsIgnoreCase(ImageCaptureType.CAPTURE_BY_GALLERY.toString())) {
             projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA
            };
            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Cursor cur = getContentResolver().query(images,projection,null,null,null);

            if (cur.moveToFirst()) {
                String bucket;
                String data;
                int bucketColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                int dataColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATA);

                do {
                    // Get the field values
                    bucket = cur.getString(bucketColumn);
                    data = cur.getString(dataColumn);

                    catogoriesNames.add(bucket);

                    catogoriesThumbnails.add(cur.getString(dataColumn));

                    // Do something with the values.
                    Log.d("debug", " bucket=" + catogoriesNames.size()+","+bucket);
                } while (cur.moveToNext());

            }

            cur.close();
        }
        else
        {
            projection = new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media.DATA
            };
            Uri images = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            Cursor cur = getContentResolver().query(images,projection,null,null,null);

            if (cur.moveToFirst()) {
                String bucket;
                String data;
                int bucketColumn = cur.getColumnIndex(
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

                int dataColumn = cur.getColumnIndex(
                        MediaStore.Video.Media.DATA);

                do {
                    // Get the field values
                    bucket = cur.getString(bucketColumn);
                    data = cur.getString(dataColumn);

                    catogoriesNames.add(bucket);

                    catogoriesThumbnails.add(cur.getString(dataColumn));

                    // Do something with the values.
                    Log.d("debug", " bucket=" + catogoriesNames.size()+","+bucket);
                } while (cur.moveToNext());

            }

            cur.close();
        }



        gallery.setAdapter(new SimpleStringRecyclerViewAdapter(this,catogoriesNames,catogoriesThumbnails));



    }


    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {


        private List<String> mValues;
        private List<String> mBitmaps;
        private List<String> mBitmapPaths;
        Context context;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mBoundString;

            public final View mView;
            public final ImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.gallery_avatar);
            }

        }

        public String getValueAt(int position) {
            return mValues.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context, List<String> items,List<String> bitmaps) {

            mValues = items;
            mBitmaps = bitmaps;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gallery_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            Glide.with(holder.mImageView.getContext())
                    .load(mBitmaps.get(position))
                    .fitCenter()
                    .into(holder.mImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent();
                    if(MODE.equalsIgnoreCase(ImageCaptureType.CAPTURE_BY_GALLERY.toString())) {
                        intent.putExtra(ImageCaptureType.CAPTURE_BY_GALLERY.toString(), mBitmaps.get(position));
                    }else
                    {
                        intent.putExtra("VIDEO", mBitmaps.get(position));
                    }

                    ((Activity) context).setResult(ImageCaptureType.CAPTURE_BY_GALLERY.getImageCaptureType(), intent);
                    ((Activity) context).finish();

                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }


}
