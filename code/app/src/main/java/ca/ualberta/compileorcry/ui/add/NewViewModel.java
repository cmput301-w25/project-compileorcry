package ca.ualberta.compileorcry.ui.add;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NewViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NewViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is 'new' fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}