package com.happiestminds.template.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;


import com.bumptech.glide.Glide;
import com.happiestminds.template.R;

import com.happiestminds.template.service.genericservices.ImageCaptureType;
import com.happiestminds.template.service.genericservices.VideoCaptureType;
import com.happiestminds.template.service.genericservices.VideoServices;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;


public class VideoServicesActivity extends AppCompatActivity {

    VideoView mVideoView;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_services);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         mVideoView = (VideoView)findViewById(R.id.videoView);
         mImageView= (ImageView)findViewById(R.id.imageView);

        // declare the dialog as a member field of your activity
        final ProgressDialog mProgressDialog;

     // instantiate it within the onCreate method



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();



            }
        });
    }


    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.button:

                final CharSequence[] items = {
                        VideoCaptureType.CAPTURE_BY_CAMERA.toString(),
                        VideoCaptureType.CAPTURE_BY_GALLERY.toString(),
                        VideoCaptureType.CAPTURE_WITH_COMPRESS.toString(),
                        VideoCaptureType.CAPTURE_BY_DOWNLOAD.toString()

                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Make your selection");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:
                                VideoServices.captureVideo(VideoServicesActivity.this, VideoCaptureType.CAPTURE_BY_CAMERA);
                                break;
                            case 1:
                                VideoServices.captureVideo(VideoServicesActivity.this, VideoCaptureType.CAPTURE_BY_GALLERY);
                                break;
                            case 2:
                                VideoServices.captureVideo(VideoServicesActivity.this, VideoCaptureType.CAPTURE_WITH_COMPRESS);
                                break;
                            case 3:
                                VideoServices.downloadVideoFromUrl(VideoServicesActivity.this,
                                        "http://techslides.com/demos/sample-videos/small.mp4",
                                        new ProgressDialog(VideoServicesActivity.this), mImageView);
                                break;

                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.imageView:
                break;
            case R.id.videoView:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( data != null) {
            if (requestCode == VideoCaptureType.CAPTURE_WITH_COMPRESS.getVideoCaptureType())
            {

            }
            else if (requestCode == VideoCaptureType.CAPTURE_BY_CAMERA.getVideoCaptureType())
            {
                Uri videoUri = data.getData();
                Bitmap thumbNail;
                thumbNail = ThumbnailUtils.createVideoThumbnail(VideoServices.getRealPathFromURI(this, videoUri), MediaStore.Video.Thumbnails.MINI_KIND);
                mImageView.setImageBitmap(thumbNail);
            }
            else if (requestCode == VideoCaptureType.CAPTURE_BY_GALLERY.getVideoCaptureType())
            {
                Glide.with(this)
                        .load(data.getExtras().getString("VIDEO"))
                        .fitCenter()
                        .into(mImageView);
            }

        }

    }


}
