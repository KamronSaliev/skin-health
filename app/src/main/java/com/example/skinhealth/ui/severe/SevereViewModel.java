package com.example.skinhealth.ui.severe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SevereViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public SevereViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is severe fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
