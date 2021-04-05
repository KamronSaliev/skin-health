package com.example.skinhealth.ui.home;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.skinhealth.DialogActivity;
import com.example.skinhealth.MainActivity;
import com.example.skinhealth.R;

import java.io.FileNotFoundException;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button mainButton;
    private DialogActivity dialog;

    private TextView textView;
    private Bitmap Image = null;
    private Bitmap rotateImage = null;
    private ImageView imageView;

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        textView = root.findViewById(R.id.text_home);
        mainButton = root.findViewById(R.id.button_main);
        imageView = root.findViewById(R.id.imageView2);

        onAddPhotoButtonClick();

        homeViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));

        return root;
    }


    public void onAddPhotoButtonClick() {
        mainButton.setOnClickListener(v -> {
            dialog = new DialogActivity(getActivity(), new DialogClickListener() {

                public void onClickCamera() {
                    Intent activityCamera = new Intent(getActivity(), MainActivity.class);
                    startActivity(activityCamera);
                }

                public void onClickGallery() {
                    imageView.setImageBitmap(null);

                    if (Image != null)
                        Image.recycle();

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST_CODE);
                }
            });

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode != 0) {
            Uri mImageUri = data.getData();
            try {
                Image = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), mImageUri);
                if (getOrientation(getActivity().getApplicationContext(), mImageUri) != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getOrientation(getActivity().getApplicationContext(), mImageUri));

                    if (rotateImage != null)
                        rotateImage.recycle();

                    rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix,true);
                    imageView.setImageBitmap(rotateImage);
                } else
                    imageView.setImageBitmap(Image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

}