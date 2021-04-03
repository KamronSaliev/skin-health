package com.example.skinhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.skinhealth.ui.home.DialogClickListener;

public class DialogActivity extends Dialog {

    private Context context;
    private DialogClickListener dialogClickListener;
    private ImageView CameraButton, GalleryButton;

    public DialogActivity(@NonNull Context context, DialogClickListener dialogClickListener) {
        super(context);
        this.context = context;
        this.dialogClickListener = dialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        CameraButton = findViewById(R.id.camera_button);
        GalleryButton = findViewById(R.id.gallery_button);
        CameraButton.setClickable(true);
        GalleryButton.setClickable(true);

        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListener.onClickCamera();
                dismiss();
            }
        });

        GalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListener.onClickGallery();
                dismiss();
            }
        });
    }
}