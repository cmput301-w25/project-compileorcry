package ca.ualberta.compileorcry.features.mood.model;

public enum EmotionalState {
    ANGER(1, "Anger"),
    CONFUSION(2, "Confusion"),
    DISGUST(3, "Disgust"),
    FEAR(4, "Fear"),
    HAPPINESS(5, "Happiness"),
    SADNESS(6, "Sadness"),
    SHAME(7, "Shame"),
    SURPRISE(8, "Surprise");

    private final int code;
    private final String description;

    EmotionalState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EmotionalState fromCode(int code) {
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
