package ca.ualberta.compileorcry.features.mood.model;

/**
 * Defines the visibility options for mood events in the application.
 * Each visibility option has an associated numeric code that can be used
 * for database storage and retrieval.
 */
public enum Visibility {
    PUBLIC(1L),
    PRIVATE(2L);

    private final long code;

    /**
     * Constructs a Visibility enum value.
     *
     * @param code The numeric code used to identify this visibility state in storage
     */
    Visibility(long code) {
        this.code = code;
    }

    /**
     * Returns the numeric code for this visibility state.
     *
     * @return The unique numeric code
     */
    public long getCode() {
        return code;
    }

    /**
     * Determines if this visibility state is public.
     *
     * @return true if this is the PUBLIC visibility state, false otherwise
     */
    public boolean isPublic() {
        return this == PUBLIC;
    }

    /**
     * Determines if this visibility state is private.
     *
     * @return true if this is the PRIVATE visibility state, false otherwise
     */
    public boolean isPrivate() {
        return this == PRIVATE;
    }

    /**
     * Finds a Visibility enum value by its numeric code.
     *
     * @param code The numeric code to search for
     * @return The matching Visibility enum value
     * @throws IllegalArgumentException If no matching visibility state is found
     */
    public static Visibility fromCode(long code) {
        for (Visibility visibility : values()) {
            if (visibility.code == code) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Invalid visibility code: " + code);
    }
}