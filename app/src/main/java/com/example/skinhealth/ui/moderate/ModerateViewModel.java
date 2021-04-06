package com.example.skinhealth.ui.moderate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ModerateViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ModerateViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is moderate fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}