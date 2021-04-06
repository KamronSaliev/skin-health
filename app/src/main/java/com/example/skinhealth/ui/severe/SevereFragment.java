package com.example.skinhealth.ui.severe;

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

public class SevereFragment extends Fragment {
    private SevereViewModel severeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        severeViewModel =
                new ViewModelProvider(this).get(SevereViewModel.class);
        View root = inflater.inflate(R.layout.fragment_severe, container, false);
        return root;
    }
}
