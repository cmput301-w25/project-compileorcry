package ca.ualberta.compileorcry.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.Comment;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

/**
 * ViewModel for managing and storing comment data related to a specific mood event.
 * Uses LiveData to observe changes and update UI automatically.
 */
public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();
    private MoodEvent moodEvent;

    public CommentViewModel(String moodId) {
        this.moodEvent = new MoodEvent(moodId);
    }
    public MoodEvent getMoodEvent() {
        return moodEvent;
    }

    /**
     * Gets the LiveData object containing the list of comments.
     * Observers can listen for changes and update UI accordingly.
     *
     * @return LiveData object containing the list of comments.
     */
    public LiveData<List<Comment>> getCommentsLiveData() {
        return commentsLiveData;
    }

    /**
     * Adds a new comment to the list and updates LiveData.
     * Uses MoodEvent.java's addComment() method
     *
     * @param comment The comment to be added.
     */
    public void addComment(Comment comment) {
        if (moodEvent == null) {
            Log.e("CommentViewModel", "MoodEvent is null, cannot add comment");
            return;
        }
        CommentViewModel ptr = this;

        moodEvent.addComment(comment, new MoodEvent.AddCommentCallback() {

            @Override
            public void onSuccess() {
                Log.d("CommentViewModel", "Comment added successfully");
                ptr.reloadComments(User.getActiveUser().getUsername());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CommentViewModel", "Failed to add comment", e);
            }
        }, User.getActiveUser().getUsername());
    }

    /**
     * Adds a new comment to the list and updates LiveData.
     * Uses MoodEvent.java's loadComment() method
     *
     * @param username The username of the commenter.
     */
    public void loadComments(String username) {
        if (moodEvent == null) {
            Log.e("CommentViewModel", "MoodEvent is null, cannot load comments");
            return;
        }

        new Thread(() -> {
            try {
                List<Comment> loadedComments = moodEvent.getComments(username);
                commentsLiveData.postValue(loadedComments);
            } catch (InterruptedException e) {
                Log.e("CommentViewModel", "Failed to load comments", e);
            }
        }).start();
    }

    /**
     * Refreshes the comments, basically the same as loadComments().
     * Uses MoodEvent.java's reloadComment() method
     *
     * @param username The username of the commenter.
     */
    public void reloadComments(String username) {
        if (moodEvent == null) {
            Log.e("CommentViewModel", "MoodEvent is null, cannot reload comments");
            return;
        }

        new Thread(() -> {
            try {
                moodEvent.reloadComments(username);
                List<Comment> updatedComments = moodEvent.getComments(username);
                commentsLiveData.postValue(updatedComments);
            } catch (InterruptedException e) {
                Log.e("CommentViewModel", "Failed to reload comments", e);
            }
        }).start();
    }

    /**
     *
     * @param moodEvent
     */
    public void setMoodEvent(MoodEvent moodEvent) {
        this.moodEvent = moodEvent;
    }
}


