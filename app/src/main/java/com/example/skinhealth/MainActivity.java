package com.example.skinhealth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.skinhealth.ui.home.DialogClickListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_DETECT = 1;

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    private DialogActivity dialog;

    private Size size;

    private Mat mSrcMat;
    private Mat mIntermediateMat;
    // private Mat mResizedMat;
    private Mat mMaskMat;
    private Mat mHierarchy;

    public static int viewMode = VIEW_MODE_RGBA;

    private Button chooseImageButton;
    private ImageView imageView;
    private TextView textViewCounter;

    private Bitmap bmp;
    private Bitmap initialBmp;
    private ArrayList<MatOfPoint> contours = new ArrayList<>();
    private int count = 0;

    private String currentPhotoPath;

    // Preferences
    private static final String PREFS_COUNT_KEY = "count";
    private static final String PREFS_LEVEL_KEY = "level";
    private static final String PREFS_UPDATE_DATE_KEY = "update_date";

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");

                onInit();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.profile_image);
        textViewCounter = (TextView) findViewById(R.id.tv_count);
        chooseImageButton = (Button) findViewById(R.id.btn_choose_image);

        onAddPhotoButtonClick();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesEditor = preferences.edit();
    }

    public void onAddPhotoButtonClick() {
        chooseImageButton.setOnClickListener(v -> {
            dialog = new DialogActivity(MainActivity.this, new DialogClickListener() {

                public void onClickCamera() {
                    openCamera();
                }

                public void onClickGallery() {
                    openGallery();
                }
            });

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.show();
        });
    }

    private File createImageFile(){
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",  /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        imageView.setImageBitmap(null);

        if (bmp != null)
            bmp.recycle();

        // TODO: relocate to the start of the program
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission66","Completion of permission setting");
            } else {
                Log.d("Permission66", "Request for permission setting");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {

            File photoFile = createImageFile();

            if (photoFile != null) {
                Uri uri  = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void openGallery() {
        imageView.setImageBitmap(null);

        if (bmp != null)
            bmp.recycle();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onInit() {
        mIntermediateMat = new Mat();
        size = new Size();

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        initialBmp = bitmapDrawable.getBitmap();
    }

    public void onClickReset(View view) {
        viewMode = VIEW_MODE_RGBA;
        imageView.setImageBitmap(onConvertImage());
    }

    public void onClickDetect(View view) {
        viewMode = VIEW_MODE_DETECT;
        imageView.setImageBitmap(onConvertImage());

        onDetectSucceeded();
    }

    private void onDetectSucceeded() {
        // TODO: implement the actual grading system for the damage levels, test code
        if (count < 100) {
            preferencesEditor.putString(PREFS_LEVEL_KEY, "Mild");
        }
        else
            preferencesEditor.putString(PREFS_LEVEL_KEY, "Severe");

        preferencesEditor.putString(PREFS_COUNT_KEY, String.valueOf(count));
        preferencesEditor.putString(PREFS_UPDATE_DATE_KEY, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        preferencesEditor.apply();

        Log.d(TAG, "Cached data");
    }

    @SuppressLint("DefaultLocale")
    public Bitmap onConvertImage() {
        mSrcMat = new Mat (initialBmp.getHeight(), initialBmp.getWidth(), CvType.CV_8UC3);
        bmp = initialBmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp, mSrcMat);

        mIntermediateMat = new Mat();
        // Bitmap convertBmp = initialBmp;

        // DEFAULT RGBA
        if (MainActivity.viewMode == MainActivity.VIEW_MODE_RGBA) {
            count = 0;
            String stringCounter = getResources().getString(R.string.tv_count);
            textViewCounter.setText(String.format("%s %d", stringCounter, count));

            return bmp;
        }

        // DETECT
        else if (MainActivity.viewMode == MainActivity.VIEW_MODE_DETECT) {
            Log.i(TAG, "Detect button was pressed");

            // Random seed
            Random rng = new Random(10000);

            Size srcSize = mSrcMat.size();
            Log.i(TAG, srcSize.toString());

            // TODO: Resize logic, might be used later
            int referencedHeightInDP = 500;
            float scale = getResources().getDisplayMetrics().density;
            int referencedHeightInPixels = (int) (referencedHeightInDP * scale + 0.5f);

            // mResizedMat = new Mat();
            // Imgproc.resize(mSrcMat, mResizedMat, new Size(srcSize.width, referencedHeightInPixels));

            // mSrcMat = mResizedMat.clone();
            mIntermediateMat = mSrcMat.clone();

            // TODO: extract to constants
            // Increase brightness
            int brightnessAlpha = 1;
            int brightnessBeta = 50;
            mIntermediateMat.convertTo(mIntermediateMat, -1, brightnessAlpha, brightnessBeta);

            Imgproc.cvtColor(mIntermediateMat, mIntermediateMat, Imgproc.COLOR_BGR2GRAY);

            // Blur
            Imgproc.blur(mIntermediateMat, mIntermediateMat, new Size(5, 5));

            // Adaptive threshold of the image
            Imgproc.adaptiveThreshold(mIntermediateMat, mIntermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 5);

            // Dilation
            Imgproc.dilate(mIntermediateMat, mIntermediateMat, new Mat(), new Point(-1, -1), 1);

            // Implementation of addition of the mask to the main intermediate Mat
            Bitmap maskBitmap = BitmapFactory.decodeResource(getResources(), R.drawable._skinhealth_mask);
            mMaskMat = new Mat (maskBitmap.getHeight(), maskBitmap.getWidth(), CvType.CV_8UC3);
            Utils.bitmapToMat(maskBitmap, mMaskMat);

            Imgproc.resize(mMaskMat, mMaskMat, new Size(mIntermediateMat.size().width, mIntermediateMat.size().height));
            Imgproc.cvtColor(mMaskMat, mMaskMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(mMaskMat, mMaskMat, 100, 255, Imgproc.THRESH_BINARY_INV);
            Core.bitwise_not(mMaskMat, mMaskMat);// inversion of colors of mask
            Core.add(mMaskMat, mIntermediateMat, mIntermediateMat); // addition of the mask to the image in question

            // Main logic of detecting acne contours
            count = 0;
            contours.clear();
            mHierarchy = new Mat();
            Imgproc.findContours(mIntermediateMat, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            MatOfPoint2f[] contoursPoly  = new MatOfPoint2f[contours.size()];
            Rect[] boundRect = new Rect[contours.size()];
            Point[] centers = new Point[contours.size()];
            float[][] radius = new float[contours.size()][1];

            for (int i = 0; i < contours.size(); i++) {

                // TODO: extract to constants
                int minArea = 20;
                int maxArea = 150;

                if (Imgproc.contourArea(contours.get(i)) > minArea && Imgproc.contourArea(contours.get(i)) < maxArea) {
                    contoursPoly[i] = new MatOfPoint2f();
                    Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
                    boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));
                    centers[i] = new Point();
                    Imgproc.minEnclosingCircle(contoursPoly[i], centers[i], radius[i]);


                    // TODO: extract to constants
                    if (boundRect[i].height < 3 || boundRect[i].width < 3)
                        continue;

                    // used to draw rectangles on the source Mat
                    Imgproc.rectangle(mSrcMat, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 255, 0, 255), 1);

                    count++;
                }
            }

            // TODO: Temporary, remove, used for visualization of the mask used to find contours
            // mSrcMat = mIntermediateMat.clone();

            String stringCounter = getResources().getString(R.string.tv_count);
            textViewCounter.setText(String.format("%s %d", stringCounter, count));
        }

        bmp = Bitmap.createBitmap(mSrcMat.cols(), mSrcMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mSrcMat, bmp);

        mSrcMat.release();
        mIntermediateMat.release();
        mMaskMat.release();
        mHierarchy.release();

        return bmp;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != 0) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri uri = data.getData();

                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageView.setImageBitmap(bmp);
                    Log.d(TAG, "Image from Gallery is set");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == CAMERA_REQUEST_CODE) {
                setCameraPicture();
                Log.d(TAG, "Image from Camera is set");
            }
        }
    }

    private void setCameraPicture() {
        int width = imageView.getWidth();
        int height = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/width, photoH/height));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotate(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotate(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotate(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        imageView.setImageBitmap(rotatedBitmap);
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, mtx, true);
    }
}