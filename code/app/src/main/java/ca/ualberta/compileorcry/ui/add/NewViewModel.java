package ca.ualberta.compileorcry.ui.add;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Manages UI-related data.
 * This class follows the MVVM architecture pattern, separating the UI logic from UI controllers.
 *
 * Currently, this ViewModel has minimal functionality and only provides text data
 * for display purposes. As the application evolves, this class can be extended
 * to handle more complex data operations related to creating new mood events.
 *
 */
public class NewViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    /**
     * Constructs a new NewViewModel instance.
     */
    public NewViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is 'new' fragment");
    }

    /**
     * Returns the LiveData object containing text to be displayed.
     *
     * @return LiveData containing the text to be displayed.
     */
    public LiveData<String> getText() {
        return mText;
    }
}