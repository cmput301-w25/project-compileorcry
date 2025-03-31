package ca.ualberta.compileorcry.ui.feed;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory class for creating CommentViewModel instances with a required parameter.
 * Since ViewModelProvider requires a zero-argument constructor, this factory ensures
 * the ViewModel gets initialized properly with a moodEventId.
 */
public class CommentViewModelFactory implements ViewModelProvider.Factory {
    private final String moodEventId;

    /**
     * Constructor for CommentViewModelFactory.
     *
     * @param moodEventId The unique ID of the mood event.
     */
    public CommentViewModelFactory(String moodEventId) {
        this.moodEventId = moodEventId;
    }

    /**
     * Creates a new instance of the commentViewModel with correct moodId.
     * */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CommentViewModel.class)) {
            return (T) new CommentViewModel(moodEventId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

