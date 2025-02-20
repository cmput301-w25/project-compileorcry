package ca.ualberta.compileorcry.features.mood.model;

public enum EmotionalState {
    ANGER(1L, "Anger"),
    CONFUSION(2L, "Confusion"),
    DISGUST(3L, "Disgust"),
    FEAR(4L, "Fear"),
    HAPPINESS(5L, "Happiness"),
    SADNESS(6L, "Sadness"),
    SHAME(7L, "Shame"),
    SURPRISE(8L, "Surprise");

    private final long code;
    private final String description;

    EmotionalState(long code, String description) {
        this.code = code;
        this.description = description;
    }

    public long getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EmotionalState fromCode(long code) {
        for (EmotionalState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    public static EmotionalState fromDescription(String description) {
        for (EmotionalState state : values()) {
            if (state.description.equalsIgnoreCase(description)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid description: " + description);
    }
}
