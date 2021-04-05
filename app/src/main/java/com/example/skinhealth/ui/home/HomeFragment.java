package com.example.skinhealth.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.skinhealth.DialogActivity;
import com.example.skinhealth.DietScrollingActivity;
import com.example.skinhealth.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ImageButton mainButton;
    private ImageView dietButtonView;
    private DialogActivity dialog;
    private ImageView imageView;
    private Bitmap imageBmp = null;
    private String currentPhotoPath;

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mainButton = root.findViewById(R.id.button_main);
        imageView = root.findViewById(R.id.imageView2);
        dietButtonView = root.findViewById(R.id.dietImage);
        onDietButtonClick();

        onAddPhotoButtonClick();

        return root;
    }

    public void onAddPhotoButtonClick() {
        mainButton.setOnClickListener(v -> {
            dialog = new DialogActivity(getActivity(), new DialogClickListener() {

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

    public void onDietButtonClick() {
        dietButtonView.setOnClickListener(v -> {
            Intent activityDiet = new Intent(getActivity().getApplicationContext(), DietScrollingActivity.class);
            startActivity(activityDiet);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != 0) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri uri = data.getData();

                try {
                    imageBmp = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), uri);
                    imageView.setImageBitmap(imageBmp);
                    Log.d(TAG, "Image from Gallery is set");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == CAMERA_REQUEST_CODE) {
                setPicture();
                Log.d(TAG, "Image from Camera is set");
            }
        }
    }

    private void setPicture() {
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

    private File createImageFile(){
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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

        if (imageBmp != null)
            imageBmp.recycle();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getContext().getApplicationContext().getPackageManager()) != null) {

            File photoFile = null;
            photoFile = createImageFile();

            if (photoFile != null) {
                Uri uri  = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void openGallery() {
        imageView.setImageBitmap(null);

        if (imageBmp != null)
            imageBmp.recycle();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
    }
}