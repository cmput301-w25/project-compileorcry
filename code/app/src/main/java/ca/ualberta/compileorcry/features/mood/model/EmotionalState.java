package ca.ualberta.compileorcry.features.mood.model;

import android.content.Context;

import androidx.core.content.ContextCompat;

import ca.ualberta.compileorcry.R;

/**
 * Defines the set of possible emotional states for mood events in the application.
 * Each emotional state has an associated numeric code, descriptive text, and color resource
 * to ensure consistent representation across the application UI.
 *
 * This enum supports:
 * - Conversion between numeric codes and enum values
 * - Conversion between descriptive text and enum values
 * - Access to associated colors for UI display
 *
 * The emotional states included are the core set required by the application
 * specification: anger, confusion, disgust, fear, happiness, sadness, shame, and surprise.
 */
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

    /**
     * Constructs an EmotionalState enum value.
     *
     * @param code The numeric code used to identify this emotional state in storage
     * @param description Description of this emotional state
     * @param colorResId The resource ID for the color associated with this emotional state
     */
    EmotionalState(long code, String description, int colorResId) {
        this.code = code;
        this.description = description;
        this.colorResId = colorResId;
    }

    /**
     * Returns the numeric code for this emotional state.
     *
     * @return The unique numeric code
     */
    public long getCode() {
        return code;
    }

    /**
     * Returns the human-readable description of this emotional state.
     *
     * @return The descriptive text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the resource ID for the color associated with this emotional state.
     *
     * @return The color resource ID
     */
    public int getColorResId() { return colorResId; }

    /**
     * Returns the actual color value for this emotional state.
     *
     * @param context The context used to resolve the color resource
     * @return The color value
     */
    public int getColor(Context context) {
        return ContextCompat.getColor(context, colorResId);
    }

    /**
     * Finds an EmotionalState enum value by its numeric code.
     *
     * @param code The numeric code to search for
     * @return The matching EmotionalState enum value
     * @throws IllegalArgumentException If no matching emotional state is found
     */
    public static EmotionalState fromCode(long code) {
        for (EmotionalState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

    /**
     * Finds an EmotionalState enum value by its description text.
     * The comparison is case-insensitive.
     *
     * @param description The description text to search for
     * @return The matching EmotionalState enum value
     * @throws IllegalArgumentException If no matching emotional state is found
     */
    public static EmotionalState fromDescription(String description) {
        for (EmotionalState state : values()) {
            if (state.description.equalsIgnoreCase(description)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid description: " + description);
    }
}
