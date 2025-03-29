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

import ca.ualberta.compileorcry.features.mood.model.Comment;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();
    private MoodEvent moodEvent;

    public CommentViewModel(String moodId) {
        this.moodEvent = new MoodEvent(moodId);
    }

    public LiveData<List<Comment>> getCommentsLiveData() {
        return commentsLiveData;
    }

    public void addComment(Comment comment) {
        if (moodEvent == null) {
            Log.e("CommentViewModel", "MoodEvent is null, cannot add comment");
            return;
        }

        moodEvent.addComment(comment, new MoodEvent.AddCommentCallback() {
            @Override
            public void onSuccess() {
                Log.d("CommentViewModel", "Comment added successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CommentViewModel", "Failed to add comment", e);
            }
        }, "TestUser"); // Pass the correct username here
    }

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

}


