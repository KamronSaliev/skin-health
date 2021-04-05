package com.example.skinhealth;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_DETECT = 1;

    private Size size;

    private Mat mSrcMat;
    private Mat mIntermediateMat;
    // private Mat mResizedMat;
    private Mat mMaskMat;
    private Mat mHierarchy;

    public static int viewMode = VIEW_MODE_RGBA;

    private ImageView imageView;
    private TextView textViewCounter;

    private Bitmap bmp;
    private Bitmap initialBmp;
    private ArrayList<MatOfPoint> contours = new ArrayList<>();
    private int count = 0;

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
    }

    @SuppressLint("DefaultLocale")
    public Bitmap onConvertImage() {
        mSrcMat = new Mat (initialBmp.getHeight(), initialBmp.getWidth(), CvType.CV_8UC3);
        bmp = initialBmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp, mSrcMat);

        mIntermediateMat = new Mat();
        Bitmap convertBmp = initialBmp;

        // DEFAULT RGBA
        if (MainActivity.viewMode == MainActivity.VIEW_MODE_RGBA) {
            count = 0;
            String stringCounter = getResources().getString(R.string.tv_count);
            textViewCounter.setText(String.format("%s %d", stringCounter, count));

            return convertBmp;
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
            int brightnessAlpha = 1;
            int brightnessBeta = 50;
            mIntermediateMat.convertTo(mIntermediateMat, -1, 1, 50);

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
            Core.bitwise_not(mMaskMat, mMaskMat);
            Core.add(mMaskMat, mIntermediateMat, mIntermediateMat);

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
                    Rect rect = Imgproc.boundingRect(contours.get(i));
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

        convertBmp = Bitmap.createBitmap(mSrcMat.cols(), mSrcMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mSrcMat, convertBmp);

        mSrcMat.release();
        mIntermediateMat.release();
        mMaskMat.release();
        mHierarchy.release();

        return convertBmp;
    }
}