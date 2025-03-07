package ca.ualberta.compileorcry.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class FeedViewModel extends ViewModel{

    private final MutableLiveData<MoodList> moodListLiveData;
    private MoodList moodList;
    private FirebaseFirestore db;
    private User user;
    private QueryType filter = null;
    public FeedViewModel(User user) {
        moodListLiveData = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        loadMoodList();
    }
    public LiveData<MoodList> getMoodListLiveData() {
        return moodListLiveData;
    }

    private void setMoodList(MoodList moodList) {
        this.moodList = moodList;
    }
    public void setFilter(QueryType filter) {
        this.filter = filter;
        loadMoodList();
    }
    private void loadMoodList() {
        db.collection("most_recent_moods").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MoodEvent> moodEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvent.setIdFromDocRef(document.getReference());
                            moodEvents.add(moodEvent);
                        }
                        moodList.setMoodEvents(moodEvents);
                        moodListLiveData.setValue(moodList);
                    } else {
                        Log.e("FeedViewModel", "Error getting documents: ", task.getException());
                    }
                });
    }

}