package ca.ualberta.compileorcry.features.mood.model;

import android.os.Build;

import java.time.LocalDateTime;
import java.util.UUID;

public class MoodEvent {
    private String id;
    private LocalDateTime timestamp;
    private EmotionalState emotionalState;
    private String trigger;          // Optional text (e.g., reason behind the mood)
    private String socialSituation;  // Optional (e.g., "alone", "with one person", etc.)

    /**
     * Constructor for MoodEvent.
     *
     * @param emotionalState  the required emotional state
     * @param trigger         an optional trigger/reason (can be null or empty)
     * @param socialSituation an optional social situation (can be null or empty)
     */
    public MoodEvent(EmotionalState emotionalState, String trigger, String socialSituation) {
        if(emotionalState == null) {
            throw new IllegalArgumentException("Emotional state is required");
        }
        this.id = UUID.randomUUID().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.timestamp = LocalDateTime.now(); // Automatically capture current date and time
        }
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
    }

    // Getters (setters can be added later if editing is needed)
    public String getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getSocialSituation() {
        return socialSituation;
    }
}