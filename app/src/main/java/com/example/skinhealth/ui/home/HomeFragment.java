package com.example.skinhealth.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.skinhealth.DietScrollingActivity;
import com.example.skinhealth.MainActivity;
import com.example.skinhealth.R;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ImageButton mainButton;
    private ImageView dietButtonView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mainButton = root.findViewById(R.id.button_main);
        dietButtonView = root.findViewById(R.id.dietImage);
        onDietButtonClick();

        onAddPhotoButtonClick();

        return root;
    }

    public void onAddPhotoButtonClick() {
        mainButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }

    public void onDietButtonClick() {
        dietButtonView.setOnClickListener(v -> {
            Intent activityDiet = new Intent(getActivity().getApplicationContext(), DietScrollingActivity.class);
            startActivity(activityDiet);
        });
    }
}