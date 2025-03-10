package ca.ualberta.compileorcry.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

/**
 * ViewModel for the FeedFragment that manages UI data related to mood events.
 * This class follows the MVVM architecture pattern, providing a clean separation
 * between the UI and the data.
 *
 * The FeedViewModel is responsible for:
 * - Storing and managing the list of mood events to be displayed
 * - Ensuring mood events are always sorted in reverse chronological order
 * - Providing LiveData for the UI to observe changes
 */
public class FeedViewModel extends ViewModel {
    /** LiveData containing the list of mood events to display */
    private final MutableLiveData<List<MoodEvent>> moodEvents = new MutableLiveData<>();

    /**
     * Returns LiveData containing the list of mood events.
     * UI components should observe this LiveData to receive updates
     * when the list of mood events changes.
     *
     * @return LiveData wrapping a List of MoodEvent objects
     */
    public LiveData<List<MoodEvent>> getMoodEvents() {
        return moodEvents;
    }

    /**
     * Updates the list of mood events and ensures they are sorted in reverse chronological order.
     * This method is typically called when new mood events are fetched from the repository.
     *
     * @param events The new list of mood events to display
     */
    public void setMoodEvents(List<MoodEvent> events) {
        Log.d("FeedViewModel", "setMoodEvents() called with " + events.size() + " moods.");

        // Always return list in rev. chron. order
        if (events != null) {
            Collections.sort(events, (m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
        }
        moodEvents.setValue(events);
    }
}