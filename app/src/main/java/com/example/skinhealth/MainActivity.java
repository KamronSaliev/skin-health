package com.example.skinhealth;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_CANNY     = 1;
    public static final int      VIEW_MODE_SEPIA     = 2;
    public static final int      VIEW_MODE_PIXELIZE  = 3;
    public static final int      VIEW_MODE_POSTERIZE = 4;

    private Size mSize0;

    private Mat                  mIntermediateMat;
    private Mat                  mSepiaKernel;

    public static int           viewMode = VIEW_MODE_RGBA;

    private ImageView imageView;
    private Bitmap bmp;
    private Bitmap initialBmp;

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    onInit();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
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
        mSize0 = new Size();

        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        initialBmp = bitmapDrawable.getBitmap();
    }

    public void onClickCanny(View view) {
        viewMode = VIEW_MODE_CANNY;
        imageView.setImageBitmap(onConvertImage());
    }

    public void onClickSepia(View view) {
        viewMode = VIEW_MODE_SEPIA;
        imageView.setImageBitmap(onConvertImage());
    }

    public void onClickPixelize(View view) {
        viewMode = VIEW_MODE_PIXELIZE;
        imageView.setImageBitmap(onConvertImage());
    }

    public void onClickPosterize(View view) {
        viewMode = VIEW_MODE_POSTERIZE;
        imageView.setImageBitmap(onConvertImage());
    }

    public void onClickReset(View view) {
        viewMode = VIEW_MODE_RGBA;
        imageView.setImageBitmap(onConvertImage());
    }

    public Bitmap onConvertImage() {
        Mat srcMat = new Mat (initialBmp.getHeight(), initialBmp.getWidth(), CvType.CV_8UC3);
        bmp = initialBmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp, srcMat);

        mIntermediateMat = new Mat();
        Bitmap convertBmp = initialBmp;
        if (MainActivity.viewMode == MainActivity.VIEW_MODE_RGBA) {
            return convertBmp;
        } else if (MainActivity.viewMode == MainActivity.VIEW_MODE_CANNY) {
            Imgproc.Canny(srcMat, mIntermediateMat, 80, 90);
            Imgproc.cvtColor(mIntermediateMat, srcMat, Imgproc.COLOR_GRAY2RGBA, 4);
        } else if (MainActivity.viewMode == MainActivity.VIEW_MODE_SEPIA) {
            Core.transform(srcMat, srcMat, mSepiaKernel);
        } else if (MainActivity.viewMode == MainActivity.VIEW_MODE_PIXELIZE) {
            Imgproc.resize(srcMat, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
            Imgproc.resize(mIntermediateMat, srcMat, srcMat.size(), 0., 0., Imgproc.INTER_NEAREST);
        } else if (MainActivity.viewMode == MainActivity.VIEW_MODE_POSTERIZE) {
            Imgproc.Canny(srcMat, mIntermediateMat, 80, 90);
            srcMat.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
            Core.convertScaleAbs(srcMat, mIntermediateMat, 1./16, 0);
            Core.convertScaleAbs(mIntermediateMat, srcMat, 16, 0);
        }

        convertBmp = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(srcMat, convertBmp);
        srcMat.release();
        return convertBmp;
    }
}