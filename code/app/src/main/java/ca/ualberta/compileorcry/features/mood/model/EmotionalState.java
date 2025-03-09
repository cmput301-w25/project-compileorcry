package ca.ualberta.compileorcry.features.mood.model;

import android.content.Context;

import androidx.core.content.ContextCompat;

import ca.ualberta.compileorcry.R;

public enum EmotionalState {
    ANGER(1L, "Anger", R.color.anger),
    CONFUSION(2L, "Confusion", R.color.confusion),
    DISGUST(3L, "Disgust", R.color.disgust),
    FEAR(4L, "Fear", R.color.fear),
    HAPPINESS(5L, "Happiness", R.color.happy),
    SADNESS(6L, "Sadness", R.color.sadness),
    SHAME(7L, "Shame", R.color.shame),
    SURPRISE(8L, "Surprise", R.color.surprise);

    private final int colorResId;
    private final long code;
    private final String description;

    EmotionalState(long code, String description, int colorResId) {
        this.code = code;
        this.description = description;
        this.colorResId = colorResId;
    }

    public long getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getColorResId() { return colorResId; }
    public int getColor(Context context) {
        return ContextCompat.getColor(context, colorResId);
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
