package com.happiestminds.template.ui.CustomImageView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.happiestminds.template.R;
import com.happiestminds.template.service.genericservices.croppingasynctask.BitmapCroppingWorkerTask;
import com.happiestminds.template.service.genericservices.croppingasynctask.BitmapLoadingWorkerTask;
import com.happiestminds.template.service.genericservices.imageutilservices.imagecrophandler.Defaults;
import com.happiestminds.template.service.genericservices.imageutilservices.imagecrophandler.Edge;
import com.happiestminds.template.service.genericservices.imageviewcroppingutils.ImageViewUtil;

import java.lang.ref.WeakReference;

/**
 * Created by Barun.Gupta on 1/11/2016.
 */
public class CropImageView extends FrameLayout {
    //region: Fields and Consts

    /**
     * Image view widget used to show the image for cropping.
     */
    private final ImageView mImageView;

    /**
     * Overlay over the image view to show cropping UI.
     */
    private final CropOverlayView mCropOverlayView;

    /**
     * Progress bar widget to show progress bar on async image loading and cropping.
     */
    private final ProgressBar mProgressBar;

    private Bitmap mBitmap;

    private int mDegreesRotated = 0;

    private int mLayoutWidth;

    private int mLayoutHeight;

    private int mImageResource = 0;

    /**
     * if to show progress bar when image async loading/cropping is in progress.<br>
     * default: true, disable to provide custom progress bar UI.
     */
    private boolean mShowProgressBar = true;

    /**
     * callback to be invoked when image async loading is complete
     */
    private WeakReference<OnSetImageUriCompleteListener> mOnSetImageUriCompleteListener;

    /**
     * callback to be invoked when image async cropping is complete
     */
    private WeakReference<OnGetCroppedImageCompleteListener> mOnGetCroppedImageCompleteListener;

    /**
     * The URI that the image was loaded from (if loaded from URI)
     */
    private Uri mLoadedImageUri;

    /**
     * The sample size the image was loaded by if was loaded by URI
     */
    private int mLoadedSampleSize = 1;

    /**
     * Task used to load bitmap async from UI thread
     */
    private WeakReference<BitmapLoadingWorkerTask> mBitmapLoadingWorkerTask;

    /**
     * Task used to crop bitmap async from UI thread
     */
    private WeakReference<BitmapCroppingWorkerTask> mBitmapCroppingWorkerTask;
    //endregion

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int guidelines = Defaults.DEFAULT_GUIDELINES;
        boolean fixAspectRatio = Defaults.DEFAULT_FIXED_ASPECT_RATIO;
        int aspectRatioX = Defaults.DEFAULT_ASPECT_RATIO_X;
        int aspectRatioY = Defaults.DEFAULT_ASPECT_RATIO_Y;
        ImageView.ScaleType scaleType = Defaults.VALID_SCALE_TYPES[Defaults.DEFAULT_SCALE_TYPE_INDEX];
        CropShape cropShape = CropShape.RECTANGLE;
        float snapRadius = Defaults.SNAP_RADIUS_DP;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0);
            try {
                guidelines = ta.getInteger(R.styleable.CropImageView_guidelines, guidelines);
                fixAspectRatio = ta.getBoolean(R.styleable.CropImageView_fixAspectRatio, Defaults.DEFAULT_FIXED_ASPECT_RATIO);
                aspectRatioX = ta.getInteger(R.styleable.CropImageView_aspectRatioX, Defaults.DEFAULT_ASPECT_RATIO_X);
                aspectRatioY = ta.getInteger(R.styleable.CropImageView_aspectRatioY, Defaults.DEFAULT_ASPECT_RATIO_Y);
                scaleType = Defaults.VALID_SCALE_TYPES[ta.getInt(R.styleable.CropImageView_scaleType, Defaults.DEFAULT_SCALE_TYPE_INDEX)];
                cropShape = Defaults.VALID_CROP_SHAPES[ta.getInt(R.styleable.CropImageView_cropShape, Defaults.DEFAULT_CROP_SHAPE_INDEX)];
                snapRadius = ta.getFloat(R.styleable.CropImageView_snapRadius, snapRadius);
                mShowProgressBar = ta.getBoolean(R.styleable.CropImageView_showProgressBar, mShowProgressBar);
            } finally {
                ta.recycle();
            }
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.crop_image_view, this, true);

        mImageView = (ImageView) v.findViewById(R.id.ImageView_image);
        mImageView.setScaleType(scaleType);

        mCropOverlayView = (CropOverlayView) v.findViewById(R.id.CropOverlayView);
        mCropOverlayView.setInitialAttributeValues(guidelines, fixAspectRatio, aspectRatioX, aspectRatioY);
        mCropOverlayView.setCropShape(cropShape);
        mCropOverlayView.setSnapRadius(snapRadius);
        mCropOverlayView.setVisibility(mBitmap != null ? VISIBLE : INVISIBLE);

        mProgressBar = (ProgressBar) v.findViewById(R.id.CropProgressBar);
        setProgressBarVisibility();
    }

    /**
     * Set the scale type of the image in the crop view
     */
    public ImageView.ScaleType getScaleType() {
        return mImageView.getScaleType();
    }

    /**
     * Set the scale type of the image in the crop view
     */
    public void setScaleType(ImageView.ScaleType scaleType) {
        mImageView.setScaleType(scaleType);
    }

    /**
     * The shape of the cropping area - rectangle/circular.
     */
    public CropShape getCropShape() {
        return mCropOverlayView.getCropShape();
    }

    /**
     * The shape of the cropping area - rectangle/circular.
     */
    public void setCropShape(CropShape cropShape) {
        mCropOverlayView.setCropShape(cropShape);
    }

    /**
     * Sets whether the aspect ratio is fixed or not; true fixes the aspect ratio, while
     * false allows it to be changed.
     *
     * @param fixAspectRatio Boolean that signals whether the aspect ratio should be
     * maintained.
     */
    public void setFixedAspectRatio(boolean fixAspectRatio) {
        mCropOverlayView.setFixedAspectRatio(fixAspectRatio);
    }

    /**
     * Sets the guidelines for the CropOverlayView to be either on, off, or to show when
     * resizing the application.
     *
     * @param guidelines Integer that signals whether the guidelines should be on, off, or
     * only showing when resizing.
     */
    public void setGuidelines(int guidelines) {
        mCropOverlayView.setGuidelines(guidelines);
    }

    /**
     * Sets the both the X and Y values of the aspectRatio.
     *
     * @param aspectRatioX int that specifies the new X value of the aspect ratio
     * @param aspectRatioY int that specifies the new Y value of the aspect ratio
     */
    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
        mCropOverlayView.setAspectRatioX(aspectRatioX);
        mCropOverlayView.setAspectRatioY(aspectRatioY);
    }

    /**
     * An edge of the crop window will snap to the corresponding edge of a
     * specified bounding box when the crop window edge is less than or equal to
     * this distance (in pixels) away from the bounding box edge. (default: 3)
     */
    public void setSnapRadius(float snapRadius) {
        if (snapRadius >= 0) {
            mCropOverlayView.setSnapRadius(snapRadius);
        }
    }

    /**
     * if to show progress bar when image async loading/cropping is in progress.<br>
     * default: true, disable to provide custom progress bar UI.
     */
    public boolean isShowProgressBar() {
        return mShowProgressBar;
    }

    /**
     * if to show progress bar when image async loading/cropping is in progress.<br>
     * default: true, disable to provide custom progress bar UI.
     */
    public void setShowProgressBar(boolean showProgressBar) {
        mShowProgressBar = showProgressBar;
        setProgressBarVisibility();
    }

    /**
     * Returns the integer of the imageResource
     */
    public int getImageResource() {
        return mImageResource;
    }

    /**
     * Get the URI of an image that was set by URI, null otherwise.
     */
    public Uri getImageUri() {
        return mLoadedImageUri;
    }

    /**
     * Gets the crop window's position relative to the source Bitmap (not the image
     * displayed in the CropImageView).
     *
     * @return a RectF instance containing cropped area boundaries of the source Bitmap
     */
    public Rect getActualCropRect() {
        if (mBitmap != null) {
            final Rect displayedImageRect = ImageViewUtil.getBitmapRect(mBitmap, mImageView, mImageView.getScaleType());

            // Get the scale factor between the actual Bitmap dimensions and the displayed dimensions for width.
            final float actualImageWidth = mBitmap.getWidth();
            final float displayedImageWidth = displayedImageRect.width();
            final float scaleFactorWidth = actualImageWidth / displayedImageWidth;

            // Get the scale factor between the actual Bitmap dimensions and the displayed dimensions for height.
            final float actualImageHeight = mBitmap.getHeight();
            final float displayedImageHeight = displayedImageRect.height();
            final float scaleFactorHeight = actualImageHeight / displayedImageHeight;

            // Get crop window position relative to the displayed image.
            final float displayedCropLeft = Edge.LEFT.getCoordinate() - displayedImageRect.left;
            final float displayedCropTop = Edge.TOP.getCoordinate() - displayedImageRect.top;
            final float displayedCropWidth = Edge.getWidth();
            final float displayedCropHeight = Edge.getHeight();

            // Scale the crop window position to the actual size of the Bitmap.
            float actualCropLeft = displayedCropLeft * scaleFactorWidth;
            float actualCropTop = displayedCropTop * scaleFactorHeight;
            float actualCropRight = actualCropLeft + displayedCropWidth * scaleFactorWidth;
            float actualCropBottom = actualCropTop + displayedCropHeight * scaleFactorHeight;

            // Correct for floating point errors. Crop rect boundaries should not exceed the source Bitmap bounds.
            actualCropLeft = Math.max(0f, actualCropLeft);
            actualCropTop = Math.max(0f, actualCropTop);
            actualCropRight = Math.min(mBitmap.getWidth(), actualCropRight);
            actualCropBottom = Math.min(mBitmap.getHeight(), actualCropBottom);

            return new Rect((int) actualCropLeft, (int) actualCropTop, (int) actualCropRight, (int) actualCropBottom);
        } else {
            return null;
        }
    }

    /**
     * Gets the crop window's position relative to the source Bitmap (not the image
     * displayed in the CropImageView) and the original rotation.
     *
     * @return a RectF instance containing cropped area boundaries of the source Bitmap
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public Rect getActualCropRectNoRotation() {
        if (mBitmap != null) {
            Rect rect = getActualCropRect();
            int rotateSide = mDegreesRotated / 90;
            if (rotateSide == 1) {
                rect.set(rect.top, mBitmap.getWidth() - rect.right, rect.bottom, mBitmap.getWidth() - rect.left);
            } else if (rotateSide == 2) {
                rect.set(mBitmap.getWidth() - rect.right, mBitmap.getHeight() - rect.bottom, mBitmap.getWidth() - rect.left, mBitmap.getHeight() - rect.top);
            } else if (rotateSide == 3) {
                rect.set(mBitmap.getHeight() - rect.bottom, rect.left, mBitmap.getHeight() - rect.top, rect.right);
            }
            rect.set(rect.left * mLoadedSampleSize, rect.top * mLoadedSampleSize, rect.right * mLoadedSampleSize, rect.bottom * mLoadedSampleSize);
            return rect;
        } else {
            return null;
        }
    }

    /**
     * Gets the cropped image based on the current crop window.
     *
     * @return a new Bitmap representing the cropped image
     */
    public Bitmap getCroppedImage() {
        return getCroppedImage(0, 0);
    }

    /**
     * Gets the cropped image based on the current crop window.<br>
     * If image loaded from URI will use sample size to fit in the requested width and height down-sampling
     * if required - optimization to get best size to quality.
     *
     * @return a new Bitmap representing the cropped image
     */
    public Bitmap getCroppedImage(int reqWidth, int reqHeight) {
        if (mBitmap != null) {
            if (mLoadedImageUri != null && mLoadedSampleSize > 1) {
                return ImageViewUtil.cropBitmap(
                        getContext(),
                        mLoadedImageUri,
                        getActualCropRectNoRotation(),
                        mDegreesRotated,
                        reqWidth,
                        reqHeight);
            } else {
                return ImageViewUtil.cropBitmap(mBitmap, getActualCropRect());
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the cropped circle image based on the current crop selection.<br>
     * Same as {@link #getCroppedImage()} (as the bitmap is rectangular by nature) but the pixels beyond the
     * oval crop will be transparent.
     *
     * @return a new Circular Bitmap representing the cropped image
     */
    public Bitmap getCroppedOvalImage() {
        if (mBitmap != null) {
            Bitmap cropped = getCroppedImage();
            return ImageViewUtil.toOvalBitmap(cropped);
        } else {
            return null;
        }
    }

    /**
     * Gets the cropped image based on the current crop window.<br>
     * The result will be invoked to listener set by {@link #setOnGetCroppedImageCompleteListener(OnGetCroppedImageCompleteListener)}.
     */
    public void getCroppedImageAsync() {
        getCroppedImageAsync(CropShape.RECTANGLE, 0, 0);
    }

    /**
     * Gets the cropped image based on the current crop window.<br>
     * If image loaded from URI will use sample size to fit in the requested width and height down-sampling
     * if required - optimization to get best size to quality.<br>
     * The result will be invoked to listener set by {@link #setOnGetCroppedImageCompleteListener(OnGetCroppedImageCompleteListener)}.
     *
     * @param cropShape the shape to crop the image
     */
    public void getCroppedImageAsync(CropShape cropShape, int reqWidth, int reqHeight) {
        if (mOnGetCroppedImageCompleteListener == null) {
            throw new IllegalArgumentException("OnGetCroppedImageCompleteListener is not set");
        }
        BitmapCroppingWorkerTask currentTask = mBitmapCroppingWorkerTask != null ? mBitmapCroppingWorkerTask.get() : null;
        if (currentTask != null) {
            // cancel previous cropping
            currentTask.cancel(true);
        }

        mBitmapCroppingWorkerTask = mLoadedImageUri != null && mLoadedSampleSize > 1
                ? new WeakReference<>(new BitmapCroppingWorkerTask(this, mLoadedImageUri, getActualCropRectNoRotation(), cropShape, mDegreesRotated, reqWidth, reqHeight))
                : new WeakReference<>(new BitmapCroppingWorkerTask(this, mBitmap, getActualCropRect(), cropShape));
        mBitmapCroppingWorkerTask.get().execute();
        setProgressBarVisibility();
    }

    /**
     * Set the callback to be invoked when image async loading ({@link #setImageUriAsync(Uri)})
     * is complete (successful or failed).
     */
    public void setOnSetImageUriCompleteListener(OnSetImageUriCompleteListener listener) {
        mOnSetImageUriCompleteListener = listener != null ? new WeakReference<>(listener) : null;
    }

    /**
     * Set the callback to be invoked when image async cropping ({@link #getCroppedImageAsync()})
     * is complete (successful or failed).
     */
    public void setOnGetCroppedImageCompleteListener(OnGetCroppedImageCompleteListener listener) {
        mOnGetCroppedImageCompleteListener = listener != null ? new WeakReference<>(listener) : null;
    }

    /**
     * Sets a Bitmap as the content of the CropImageView.
     *
     * @param bitmap the Bitmap to set
     */
    public void setImageBitmap(Bitmap bitmap) {
        setBitmap(bitmap, true);
    }

    /**
     * Sets a Bitmap and initializes the image rotation according to the EXIT data.<br>
     * <br>
     * The EXIF can be retrieved by doing the following:
     * <code>ExifInterface exif = new ExifInterface(path);</code>
     *
     * @param bitmap the original bitmap to set; if null, this
     * @param exif the EXIF information about this bitmap; may be null
     */
    public void setImageBitmap(Bitmap bitmap, ExifInterface exif) {
        if (bitmap != null && exif != null) {
            ImageViewUtil.RotateBitmapResult result = ImageViewUtil.rotateBitmapByExif(bitmap, exif);
            bitmap = result.bitmap;
            mDegreesRotated = result.degrees;
        }
        setBitmap(bitmap, true);
    }

    /**
     * Sets a Drawable as the content of the CropImageView.
     *
     * @param resId the drawable resource ID to set
     */
    public void setImageResource(int resId) {
        if (resId != 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
            setBitmap(bitmap, true);
            mImageResource = resId;
        }
    }

    /**
     * Sets a bitmap loaded from the given Android URI as the content of the CropImageView.<br>
     * Can be used with URI from gallery or camera source.<br>
     * Will rotate the image by exif data.<br>
     *
     * @param uri the URI to load the image from
     * @deprecated Use {@link #setImageUriAsync(Uri)} for better async handling
     */
    @Deprecated
    public void setImageUri(Uri uri) {
        if (uri != null) {

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            double densityAdj = metrics.density > 1 ? 1 / metrics.density : 1;

            int width = (int) (metrics.widthPixels * densityAdj);
            int height = (int) (metrics.heightPixels * densityAdj);
            ImageViewUtil.DecodeBitmapResult decodeResult =
                    ImageViewUtil.decodeSampledBitmap(getContext(), uri, width, height);

            ImageViewUtil.RotateBitmapResult rotateResult =
                    ImageViewUtil.rotateBitmapByExif(getContext(), decodeResult.bitmap, uri);

            setBitmap(rotateResult.bitmap, true);

            mLoadedImageUri = uri;
            mLoadedSampleSize = decodeResult.sampleSize;
            mDegreesRotated = rotateResult.degrees;
        }
    }

    /**
     * Sets a bitmap loaded from the given Android URI as the content of the CropImageView.<br>
     * Can be used with URI from gallery or camera source.<br>
     * Will rotate the image by exif data.<br>
     *
     * @param uri the URI to load the image from
     */
    public void setImageUriAsync(Uri uri) {
        setImageUriAsync(uri, null);
    }

    /**
     * Clear the current image set for cropping.
     */
    public void clearImage() {
        clearImage(true);
    }

    /**
     * Rotates image by the specified number of degrees clockwise. Cycles from 0 to 360
     * degrees.
     *
     * @param degrees Integer specifying the number of degrees to rotate.
     */
    public void rotateImage(int degrees) {
        if (mBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            Bitmap bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            setBitmap(bitmap, false);

            mDegreesRotated += degrees;
            mDegreesRotated = mDegreesRotated % 360;
        }
    }

    //region: Private methods

    /**
     * Load image from given URI async using {@link BitmapLoadingWorkerTask}<br>
     * optionally rotate the loaded image given degrees, used for restore state.
     */
    private void setImageUriAsync(Uri uri, Integer preSetRotation) {
        if (uri != null) {
            BitmapLoadingWorkerTask currentTask = mBitmapLoadingWorkerTask != null ? mBitmapLoadingWorkerTask.get() : null;
            if (currentTask != null) {
                // cancel previous loading (no check if the same URI because camera URI can be the same for different images)
                currentTask.cancel(true);
            }

            // either no existing task is working or we canceled it, need to load new URI
            clearImage(true);
            mBitmapLoadingWorkerTask = new WeakReference<>(new BitmapLoadingWorkerTask(this, uri, preSetRotation));
            mBitmapLoadingWorkerTask.get().execute();
            setProgressBarVisibility();
        }
    }

    /**
     * On complete of the async bitmap loading by {@link #setImageUriAsync(Uri)} set the result
     * to the widget if still relevant and call listener if set.
     *
     * @param result the result of bitmap loading
     */
   public void onSetImageUriAsyncComplete(BitmapLoadingWorkerTask.Result result) {

        mBitmapLoadingWorkerTask = null;
        setProgressBarVisibility();

        if (result.error == null) {
            setBitmap(result.bitmap, true);
            mLoadedImageUri = result.uri;
            mLoadedSampleSize = result.loadSampleSize;
            mDegreesRotated = result.degreesRotated;
        }

        OnSetImageUriCompleteListener listener = mOnSetImageUriCompleteListener != null
                ? mOnSetImageUriCompleteListener.get() : null;
        if (listener != null) {
            listener.onSetImageUriComplete(this, result.uri, result.error);
        }
    }

    /**
     * On complete of the async bitmap cropping by {@link #getCroppedImageAsync()} call listener if set.
     *
     * @param result the result of bitmap cropping
     */
    public void onGetImageCroppingAsyncComplete(BitmapCroppingWorkerTask.Result result) {

        mBitmapCroppingWorkerTask = null;
        setProgressBarVisibility();

        OnGetCroppedImageCompleteListener listener = mOnGetCroppedImageCompleteListener != null
                ? mOnGetCroppedImageCompleteListener.get() : null;
        if (listener != null) {
            listener.onGetCroppedImageComplete(this, result.bitmap, result.error);
        }
    }

    /**
     * Set the given bitmap to be used in for cropping<br>
     * Optionally clear full if the bitmap is new, or partial clear if the bitmap has been manipulated.
     */
    private void setBitmap(Bitmap bitmap, boolean clearFull) {
        if (mBitmap != bitmap) {

            clearImage(clearFull);

            mBitmap = bitmap;
            mImageView.setImageBitmap(mBitmap);
            if (mCropOverlayView != null) {
                mCropOverlayView.resetCropOverlayView();
                mCropOverlayView.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * Clear the current image set for cropping.<br>
     * Full clear will also clear the data of the set image like Uri or Resource id while partial clear
     * will only clear the bitmap and recycle if required.
     */
    private void clearImage(boolean full) {

        // if we allocated the bitmap, release it as fast as possible
        if (mBitmap != null && (mImageResource > 0 || mLoadedImageUri != null)) {
            mBitmap.recycle();
            mBitmap = null;
        }

        if (full) {
            // clean the loaded image flags for new image
            mImageResource = 0;
            mLoadedImageUri = null;
            mLoadedSampleSize = 1;
            mDegreesRotated = 0;

            mImageView.setImageBitmap(null);

            if (mCropOverlayView != null) {
                mCropOverlayView.setVisibility(INVISIBLE);
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putParcelable("LOADED_IMAGE_URI", mLoadedImageUri);
        bundle.putInt("LOADED_IMAGE_RESOURCE", mImageResource);
        if (mLoadedImageUri == null && mImageResource < 1) {
            bundle.putParcelable("SET_BITMAP", mBitmap);
        }
        if (mBitmapLoadingWorkerTask != null) {
            BitmapLoadingWorkerTask task = mBitmapLoadingWorkerTask.get();
            if (task != null) {
                bundle.putParcelable("LOADING_IMAGE_URI", task.getUri());
            }
        }
        bundle.putInt("DEGREES_ROTATED", mDegreesRotated);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            Bitmap bitmap = null;
            Uri uri = bundle.getParcelable("LOADED_IMAGE_URI");
            if (uri != null) {
                setImageUriAsync(uri, bundle.getInt("DEGREES_ROTATED"));
            } else {
                int resId = bundle.getInt("LOADED_IMAGE_RESOURCE");
                if (resId > 0) {
                    setImageResource(resId);
                } else {
                    bitmap = bundle.getParcelable("SET_BITMAP");
                    if (bitmap != null) {
                        setBitmap(bitmap, true);
                    } else {
                        uri = bundle.getParcelable("LOADING_IMAGE_URI");
                        if (uri != null) {
                            setImageUriAsync(uri);
                        }
                    }
                }
            }

            mDegreesRotated = bundle.getInt("DEGREES_ROTATED");
            if (mBitmap != null && bitmap == null) {
                // Fixes the rotation of the image when we reloaded it.
                int tmpRotated = mDegreesRotated;
                rotateImage(mDegreesRotated);
                mDegreesRotated = tmpRotated;
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (mBitmap != null) {
            Rect bitmapRect = ImageViewUtil.getBitmapRect(mBitmap, this, mImageView.getScaleType());
            mCropOverlayView.setBitmapRect(bitmapRect);
        } else {
            mCropOverlayView.setBitmapRect(Defaults.EMPTY_RECT);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mBitmap != null) {

            // Bypasses a baffling bug when used within a ScrollView, where heightSize is set to 0.
            if (heightSize == 0) {
                heightSize = mBitmap.getHeight();
            }

            int desiredWidth;
            int desiredHeight;

            double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
            double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;

            // Checks if either width or height needs to be fixed
            if (widthSize < mBitmap.getWidth()) {
                viewToBitmapWidthRatio = (double) widthSize / (double) mBitmap.getWidth();
            }
            if (heightSize < mBitmap.getHeight()) {
                viewToBitmapHeightRatio = (double) heightSize / (double) mBitmap.getHeight();
            }

            // If either needs to be fixed, choose smallest ratio and calculate from there
            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize;
                    desiredHeight = (int) (mBitmap.getHeight() * viewToBitmapWidthRatio);
                } else {
                    desiredHeight = heightSize;
                    desiredWidth = (int) (mBitmap.getWidth() * viewToBitmapHeightRatio);
                }
            } else {
                // Otherwise, the picture is within frame layout bounds. Desired width is simply picture size
                desiredWidth = mBitmap.getWidth();
                desiredHeight = mBitmap.getHeight();
            }

            int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);

            mLayoutWidth = width;
            mLayoutHeight = height;

            Rect bitmapRect = ImageViewUtil.getBitmapRect(mBitmap.getWidth(),
                    mBitmap.getHeight(),
                    mLayoutWidth,
                    mLayoutHeight,
                    mImageView.getScaleType());
            mCropOverlayView.setBitmapRect(bitmapRect);

            // MUST CALL THIS
            setMeasuredDimension(mLayoutWidth, mLayoutHeight);

        } else {
            mCropOverlayView.setBitmapRect(Defaults.EMPTY_RECT);
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if (mLayoutWidth > 0 && mLayoutHeight > 0) {
            // Gets original parameters, and creates the new parameters
            ViewGroup.LayoutParams origParams = this.getLayoutParams();
            origParams.width = mLayoutWidth;
            origParams.height = mLayoutHeight;
            setLayoutParams(origParams);
        }
    }

    /**
     * Determines the specs for the onMeasure function. Calculates the width or height
     * depending on the mode.
     *
     * @param measureSpecMode The mode of the measured width or height.
     * @param measureSpecSize The size of the measured width or height.
     * @param desiredSize The desired size of the measured width or height.
     * @return The final size of the width or height.
     */
    private static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {

        // Measure Width
        int spec;
        if (measureSpecMode == MeasureSpec.EXACTLY) {
            // Must be this size
            spec = measureSpecSize;
        } else if (measureSpecMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...; match_parent value
            spec = Math.min(desiredSize, measureSpecSize);
        } else {
            // Be whatever you want; wrap_content
            spec = desiredSize;
        }

        return spec;
    }

    /**
     * Set visibility of progress bar when async loading/cropping is in process and show is enabled.
     */
    private void setProgressBarVisibility() {
        boolean visible =
                mShowProgressBar && (
                        (mBitmap == null && mBitmapLoadingWorkerTask != null) ||
                                (mBitmapCroppingWorkerTask != null));
        mProgressBar.setVisibility(visible ? VISIBLE : INVISIBLE);
    }
    //endregion

    //region: Inner class: CropShape

    /**
     * The possible cropping area shape.
     */
    public enum CropShape {
        RECTANGLE,
        OVAL
    }
    //endregion

    //region: Inner class: OnSetImageUriCompleteListener

    /**
     * Interface definition for a callback to be invoked when image async loading is complete.
     */
    public interface OnSetImageUriCompleteListener {

        /**
         * Called when a crop image view has completed loading image for cropping.<br>
         * If loading failed error parameter will contain the error.
         *
         * @param view The crop image view that loading of image was complete.
         * @param uri the URI of the image that was loading
         * @param error if error occurred during loading will contain the error, otherwise null.
         */
        void onSetImageUriComplete(CropImageView view, Uri uri, Exception error);
    }
    //endregion

    //region: Inner class: OnGetCroppedImageCompleteListener

    /**
     * Interface definition for a callback to be invoked when image async cropping is complete.
     */
    public interface OnGetCroppedImageCompleteListener {

        /**
         * Called when a crop image view has completed loading image for cropping.<br>
         * If loading failed error parameter will contain the error.
         *
         * @param view The crop image view that cropping of image was complete.
         * @param bitmap the cropped image bitmap (null if failed)
         * @param error if error occurred during cropping will contain the error, otherwise null.
         */
        void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error);
    }
    //endregion
}
