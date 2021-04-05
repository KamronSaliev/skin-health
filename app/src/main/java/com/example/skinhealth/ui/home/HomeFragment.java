package com.example.skinhealth.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ImageButton mainButton;
    private ImageView dietButton;
    private DialogActivity dialog;
    private ImageView imageView;
    private Bitmap imageBmp = null;
    private Bitmap rotateImageBmp = null;
    private String currentPhotoPath;

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mainButton = root.findViewById(R.id.button_main);
        dietButton = root.findViewById(R.id.dietImage);
        imageView = root.findViewById(R.id.imageView2);

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
        dietButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityDiet = new Intent(getActivity().getApplicationContext(), DietScrollingActivity.class);
                startActivity(activityDiet);
            }
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == CAMERA_REQUEST_CODE) {
                setPic();
            }
        }
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private void setPic() {
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

        Matrix matrix = new Matrix();
        Uri uri = getImageUri(getActivity().getApplicationContext(), bitmap);
        matrix.postRotate(getOrientation(getActivity().getApplicationContext(), uri));

        if (rotateImageBmp != null)
            rotateImageBmp.recycle();

        rotateImageBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,true);
        imageView.setImageBitmap(rotateImageBmp);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context
                .getContentResolver()
                .query(photoUri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
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

            FileOutputStream fos = new FileOutputStream(image);
            ExifInterface exif = new ExifInterface(image.toString());

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