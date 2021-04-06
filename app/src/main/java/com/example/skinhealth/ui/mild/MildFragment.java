package com.example.skinhealth.ui.mild;

import android.app.Activity;
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
import com.example.skinhealth.ui.moderate.ModerateViewModel;

public class MildFragment extends Fragment {
    private MildViewModel mildViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mildViewModel =
                new ViewModelProvider(this).get(MildViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mild, container, false);
        return root;
    }
}
