package com.happiestminds.template.core;

import android.app.Application;
import android.graphics.Bitmap;

import com.happiestminds.template.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by ravindra.kambale on 12/16/2015.
 */
public class MyApplication extends Application {

    private static MyApplication mInstance;

    //SingleTon Instances for ImageDownload

    ImageLoader imageLoader;
    DisplayImageOptions imageOptions;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        //set up imageloader configurations for one time

        imageOptions = new DisplayImageOptions.Builder().showImageOnFail(R.mipmap.ic_launcher)
                .showImageOnLoading(R.mipmap.ic_launcher)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(imageOptions)
                .memoryCache(new WeakMemoryCache())
                .build();

        ImageLoader.getInstance().init(config);

    }

    public static synchronized MyApplication getInstance()
    {
        return mInstance;
    }

    public ImageLoader getImageLoader()
    {
        if( imageLoader == null )
        {
            imageLoader = ImageLoader.getInstance();
        }

        return imageLoader;

    }
    public DisplayImageOptions getDisplayImageOptions()
    {

        if( imageOptions == null )
        {
            imageOptions = new DisplayImageOptions.Builder().showImageOnFail(R.mipmap.ic_launcher)
                    .showImageOnLoading(R.mipmap.ic_launcher)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheInMemory(true)
                    .displayer(new FadeInBitmapDisplayer( 300 ) )
                    .bitmapConfig( Bitmap.Config.RGB_565 )
                    .build();
        }

        return imageOptions;
    }
}
