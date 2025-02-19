package ca.ualberta.compileorcry.features.mood.model;

import java.util.Date;
import java.util.UUID;

public class MoodEvent {
    private String id;
    private Date timestamp; // changed from LocalDateTime to Date
    private EmotionalState emotionalState;
    private String trigger;          // Optional text
    private String socialSituation;  // Optional

    public MoodEvent(EmotionalState emotionalState, String trigger, String socialSituation) {
        if (emotionalState == null) {
            throw new IllegalArgumentException("Emotional state is required");
        }
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date(); // Capture current date/time using java.util.Date
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
    }

    // Getters
    public String getId() {
        return id;
    }

    public Date getTimestamp() {
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