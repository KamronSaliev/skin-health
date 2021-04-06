package com.example.skinhealth.ui.moderate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.skinhealth.R;

public class ModerateFragment extends Fragment {

    private ModerateViewModel moderateViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        moderateViewModel =
                new ViewModelProvider(this).get(ModerateViewModel.class);
        View root = inflater.inflate(R.layout.fragment_moderate, container, false);
        return root;
    }
}