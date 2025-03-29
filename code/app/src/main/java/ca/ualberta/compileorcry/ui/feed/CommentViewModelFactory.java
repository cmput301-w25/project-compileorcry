package ca.ualberta.compileorcry.ui.feed;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class CommentViewModelFactory implements ViewModelProvider.Factory {
    private final String moodEventId;

    public CommentViewModelFactory(String moodEventId) {
        this.moodEventId = moodEventId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CommentViewModel.class)) {
            return (T) new CommentViewModel(moodEventId);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

