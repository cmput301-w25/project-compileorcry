package ca.ualberta.compileorcry.features.mood.data;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodRepository {
    private static MoodRepository instance;
    private List<MoodEvent> moodEvents;

    private MoodRepository() {
        moodEvents = new ArrayList<>();
    }

    public static synchronized MoodRepository getInstance() {
        if (instance == null) {
            instance = new MoodRepository();
        }
        return instance;
    }

    // Adds a mood event to the list (most recent first)
    public void addMoodEvent(MoodEvent event) {
        // Insert at index 0 to keep reverse chronological order.
        moodEvents.add(0, event);
    }

    public List<MoodEvent> getMoodHistory() {
        return moodEvents;
    }
}