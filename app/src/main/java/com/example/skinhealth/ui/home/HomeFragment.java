package com.example.skinhealth.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.skinhealth.DietScrollingActivity;
import com.example.skinhealth.MainActivity;
import com.example.skinhealth.R;
import com.example.skinhealth.SkinActivity;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ImageButton mainButton;
    private ImageView dietButtonView;
    private ImageView skinCareButton;
    private TextView damageLevelText;
    private TextView percentageText;
    private TextView lastUpdatedText;

    private static final String PREFS_COUNT_KEY = "count";
    private static final String PREFS_LEVEL_KEY = "level";
    private static final String PREFS_UPDATE_DATE_KEY = "update_date";

    private SharedPreferences preferences;
    private String currentPercentage;
    private String currentLevel;
    private String lastUpdatedDate;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mainButton = root.findViewById(R.id.button_main);
        dietButtonView = root.findViewById(R.id.dietImage);
        skinCareButton = root.findViewById(R.id.productImage);

        percentageText = root.findViewById(R.id.numOfPimles);
        damageLevelText = root.findViewById(R.id.textViewDamageLevel);
        lastUpdatedText = root.findViewById(R.id.textViewLastUpdated);

        onAddPhotoButtonClick();
        onDietButtonClick();
        onSkinCareButtonClick();

        UpdateData();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        UpdateData();
    }

    private void UpdateData() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        currentPercentage = preferences.getString(PREFS_COUNT_KEY, "??? %");
        currentLevel = preferences.getString(PREFS_LEVEL_KEY, "Unknown");
        lastUpdatedDate = preferences.getString(PREFS_UPDATE_DATE_KEY, "01.01.1970, 00:00");

        percentageText.setText(currentPercentage);
        damageLevelText.setText(currentLevel);
        lastUpdatedText.setText(lastUpdatedDate);

        Log.d(TAG, "Updated data");
    }

    private void onAddPhotoButtonClick() {
        mainButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }

    private void onDietButtonClick() {
        dietButtonView.setOnClickListener(v -> {
            Intent activityDiet = new Intent(getActivity().getApplicationContext(), DietScrollingActivity.class);
            startActivity(activityDiet);
        });
    }

    public void onSkinCareButtonClick() {
        skinCareButton.setOnClickListener(v -> {
            Intent activitySkin = new Intent(getActivity().getApplicationContext(), SkinActivity.class);
            startActivity(activitySkin);
        });
    }
}