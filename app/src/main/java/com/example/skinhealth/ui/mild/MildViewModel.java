package com.example.skinhealth.ui.mild;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MildViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public MildViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is mild fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
