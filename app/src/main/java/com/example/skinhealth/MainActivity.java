package com.example.skinhealth;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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

import static org.opencv.core.Core.mean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_DETECT = 1;

    private Size size;

    private Mat mSrcMat;
    private Mat mIntermediateMat;
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

            // TODO: Implement logic
            // mIntermediateMat = mSrcMat.clone();

            Mat mResized = new Mat();
            Imgproc.resize(mSrcMat, mResized, new Size(512, 512));
            mSrcMat = mResized.clone();
            mIntermediateMat = mResized.clone();

            Imgproc.cvtColor(mIntermediateMat, mIntermediateMat, Imgproc.COLOR_BGR2GRAY);

            // Blur
            Imgproc.blur(mIntermediateMat, mIntermediateMat, new Size(5, 5));

            // Adaptive threshold of the image
            Imgproc.adaptiveThreshold(mIntermediateMat, mIntermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 5);

            // Dilation
            Imgproc.dilate(mIntermediateMat, mIntermediateMat, new Mat(), new Point(-1, -1), 1);

            count = 0;
            contours.clear();
            mHierarchy = new Mat();
            Imgproc.findContours(mIntermediateMat, contours, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


            // Test
            MatOfPoint2f[] contoursPoly  = new MatOfPoint2f[contours.size()];
            Rect[] boundRect = new Rect[contours.size()];
            Point[] centers = new Point[contours.size()];
            float[][] radius = new float[contours.size()][1];
            // Test

            for (int i = 0; i < contours.size(); i++) {

                int minArea = 20;
                int maxArea = 150;

                if (Imgproc.contourArea(contours.get(i)) > minArea && Imgproc.contourArea(contours.get(i)) < maxArea) {
                    Rect rect = Imgproc.boundingRect(contours.get(i));
                    Mat imgROI = new Mat(mSrcMat, rect);

                    // Imgproc.cvtColor(imgROI, imgROI, Imgproc.COLOR_GRAY2BGR);
                    Imgproc.cvtColor(imgROI, imgROI, Imgproc.COLOR_BGR2HSV);
                    Scalar meanColor = mean(imgROI);

                    Log.d(TAG, meanColor.toString());

                    Imgproc.cvtColor(imgROI, imgROI, Imgproc.COLOR_HSV2BGR);

                    contoursPoly[i] = new MatOfPoint2f();
                    Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
                    boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));
                    centers[i] = new Point();
                    Imgproc.minEnclosingCircle(contoursPoly[i], centers[i], radius[i]);

                    if (radius[i][0] > 20)
                        continue;

                    // Used to analyze some test subject
                    int testMinArea = 53;
                    int testMaxArea = 55;

                    if (Imgproc.contourArea(contours.get(i)) > testMinArea && Imgproc.contourArea(contours.get(i)) < testMaxArea) {
                        Imgproc.rectangle(mSrcMat, boundRect[i].tl(), boundRect[i].br(), new Scalar(255, 0, 0, 255), 2);
                        Imgproc.rectangle(mIntermediateMat, boundRect[i].tl(), boundRect[i].br(), new Scalar(255, 0, 0, 255), 2);
                        Log.d(TAG, "TEST SUBJECT" + meanColor.toString());
                    }
                    else {
                        Imgproc.rectangle(mSrcMat, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 0, 0, 255), 2);
                        Imgproc.rectangle(mIntermediateMat, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 0, 0, 255), 2);
                    }

                    count++;
                }
            }

            // TODO: Temporary
            // mSrcMat = mIntermediateMat.clone();

            String stringCounter = getResources().getString(R.string.tv_count);
            textViewCounter.setText(String.format("%s %d", stringCounter, count));
        }

        convertBmp = Bitmap.createBitmap(mSrcMat.cols(), mSrcMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mSrcMat, convertBmp);
        mSrcMat.release();
        mIntermediateMat.release();
        // mHierarchy.release();
        return convertBmp;
    }
}