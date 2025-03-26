package ca.ualberta.compileorcry.features.mood.data;

/**
 * Defines the various query types used by the MoodList class to retrieve and filter mood events.
 * Each query type corresponds to a specific user story requirement and determines how
 * mood events are fetched from the database.
 *
 * Query types are categorized into three main groups:
 * 1. HISTORY_* - Queries for the current user's own mood events
 * 2. FOLLOWING_* - Queries for mood events from users the current user follows
 * 3. MAP_* - Queries for location-based mood events for map visualization
 *
 * The numeric suffixes in comments (e.g., "4.1.01") refer to specific user story identifiers
 * in the application requirements documentation.
 */
public enum QueryType {
    //for 4.1.01
    HISTORY_MODIFIABLE,
    //for 4.02.01
    HISTORY_RECENT,
    //for 4.03.01
    HISTORY_STATE,
    //for 4.04.01
    HISTORY_REASON,
    //for 5.03.01
    FOLLOWING,
    //for 5.04.01
    FOLLOWING_RECENT,
    //for 5.05.01
    FOLLOWING_STATE,
    //5.06.01
    FOLLOWING_REASON,
    //6.1.1
    MAP_PERSONAL,
    //6.2.1
    MAP_FOLLOWING,
    //6.3.1
    MAP_CLOSE,
    //Extra
    MAP_PERSONAL_CLOSE

}
