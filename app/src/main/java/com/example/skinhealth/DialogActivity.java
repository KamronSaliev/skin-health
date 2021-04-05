package com.example.skinhealth;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.skinhealth.ui.home.DialogClickListener;

public class DialogActivity extends AlertDialog {

    private DialogClickListener dialogClickListener;
    private ImageView cameraButton;
    private ImageView galleryButton;

    public DialogActivity(@NonNull Context context, DialogClickListener dialogClickListener) {
        super(context);
        this.dialogClickListener = dialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        cameraButton = findViewById(R.id.camera_button);
        galleryButton = findViewById(R.id.gallery_button);
        cameraButton.setClickable(true);
        galleryButton.setClickable(true);

        cameraButton.setOnClickListener(v -> {
            dialogClickListener.onClickCamera();
            dismiss();
        });

        galleryButton.setOnClickListener(v -> {
            dialogClickListener.onClickGallery();
            dismiss();
        });
    }
}