package ca.ualberta.compileorcry.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class FeedViewModel extends ViewModel {
    private final MutableLiveData<List<MoodEvent>> moodEvents = new MutableLiveData<>();

    public LiveData<List<MoodEvent>> getMoodEvents() {
        return moodEvents;
    }

    public void setMoodEvents(List<MoodEvent> events) {
        Log.d("FeedViewModel", "setMoodEvents() called with " + events.size() + " moods.");

        // Always return list in rev. chron. order
        if (events != null) {
            Collections.sort(events, (m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
        }
        moodEvents.setValue(events);
    }

//    public void refreshMoodEvents(List<MoodEvent> events) {
//        moodEvents.setValue(new ArrayList<>());  // Clear first to ensure UI updates
//        moodEvents.setValue(events);  // Then update with new values
//    }

}