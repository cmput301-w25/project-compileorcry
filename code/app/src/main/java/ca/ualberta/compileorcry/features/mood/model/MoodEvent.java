package ca.ualberta.compileorcry.features.mood.model;

import com.firebase.geofire.core.GeoHash;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * The MoodEvent class represents a single mood event entry in the application.
 * It encapsulates all data associated with a user's recorded mood, including
 * emotional state, timestamp, location, and contextual information.
 *
 * Each mood event contains:
 * - A unique identifier
 * - Timestamp when the mood was recorded
 * - Required emotional state (from EmotionalState enum)
 * - Optional trigger text explanation (reason for the mood)
 * - Optional social situation context
 * - Optional location data as GeoHash
 * - Optional photograph (not fully implemented)
 *
 * Outstanding issues:
 * - Picture data handling is not fully implemented (marked with TODO)
 * - Location data handling needs further implementation
 */
public class MoodEvent {
    private String id;
    public void setUsername(String username) {
        this.username = username;
    }

    public void setLocation(GeoHash location) {
        this.location = location;
    }

    private Timestamp timestamp;
    private EmotionalState emotionalState;
    private String picture;
    private String trigger;

    private String socialSituation;
    private String username;
    private GeoHash location;  //TODO: this is not fully implemented yet, any setter needs to know how to get the geopoint
    private Boolean isPublic;

    /**
     * Constructs a new MoodEvent with the specified emotional state and optional context information.
     * Creates a unique ID and sets the current timestamp.
     *
     * @param emotionalState The emotional state for this event. Cannot be null.
     * @param trigger Optional text description explaining the reason for this mood event.
     * @param socialSituation Optional description of the social context (alone, with one person, etc).
     * @param picture Optional reference to an image stored in Firebase Storage.
     * @throws IllegalArgumentException If emotionalState is null.
     */
    public MoodEvent(EmotionalState emotionalState, Timestamp date, String trigger, String socialSituation, String picture, Boolean isPublic) {
        if (emotionalState == null) {
            throw new IllegalArgumentException("Emotional state is required");
        }
        this.id = UUID.randomUUID().toString();
        this.timestamp = date;
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.picture = picture;
        this.isPublic = isPublic;
    }

    /**
     * Constructs a MoodEvent with only an ID.
     * This constructor is primarily used when reconstructing MoodEvent objects from database records
     * where other properties will be set individually.
     *
     * @param id The unique identifier for this mood event
     */
    public MoodEvent(String id) {
        this.id = id;
    }

    // Getters
    /**
     * Returns the unique identifier for this mood event.
     *
     * @return The unique ID string
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the timestamp when this mood event occurred.
     *
     * @return The timestamp object
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the emotional state associated with this mood event.
     *
     * @return The emotional state enum value
     */
    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    /**
     * Returns the trigger (reason) text for this mood event.
     *
     * @return The trigger text, or null if not set
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * Returns the social situation context for this mood event.
     *
     * @return The social situation text, or null if not set
     */
    public String getSocialSituation() {
        return socialSituation;
    }

    /**
     * Returns the username of the user who created this mood event.
     *
     * @return The username, or null if not set
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the location where this mood event occurred.
     *
     * @return The GeoHash representing the location, or null if not set
     */

    public GeoHash getLocation() {
        return location;
    }

    /**
     * Returns the picture associated with this mood event.
     * Note: Picture handling is not fully implemented yet.
     *
     * @return The picture object, or null if not set
     */
    public Object getPicture() {
        return picture;
    }


    /**
     * Returns if the mood event is a public mood event
     *
     * @return true or false depending on if the moodEvent is public
     */
    public Boolean getPublic() { return isPublic; }

    //Setters
    /**
     * Sets the timestamp when this mood event occurred.
     *
     * @param timestamp The timestamp for this mood event
     */

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the emotional state for this mood event.
     *
     * @param emotionalState The emotional state to set
     */
    public void setEmotionalState(EmotionalState emotionalState) {
        this.emotionalState = emotionalState;
    }

    /**
     * Sets the picture associated with this mood event.
     * Note: Picture handling is not fully implemented yet.
     *
     * @param picture The picture object to associate with this mood event
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * Sets the trigger (reason) text for this mood event.
     *
     * @param trigger A brief textual explanation for the mood
     */
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /**
     * Sets the social situation context for this mood event.
     *
     * @param socialSituation The social context (e.g., "alone", "with one person", etc.)
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }
    /**
     * Used to set a moodEvent's Id from a document reference. MoodEvent ID should only be made if
     * it has an ID in the DB.
     *
     * @param ref The MoodEvent document DocumentReference
     */
    public void setIdFromDocRef(DocumentReference ref){
        this.id = ref.getId();
    }
    /**
     * Converts this MoodEvent to a Map that can be stored in Firestore.
     * Only non-null fields are included in the map.
     *
     * @return A Map containing the MoodEvent data in a format suitable for Firestore.
     */


    /**
     * Sets the public boolean for this mood event.
     *
     * @param aPublic true if the event is public, false if not.
     */
    public void setPublic(Boolean aPublic) {
        this.isPublic = aPublic;
    }

    public Map<String,Object> toFireStoreMap() {
        Map<String,Object> map =  new HashMap<>();
        putIfNotNull(map, "mood_id", this.id);
        //emotional state and locations are not stored in class as same datatype in db
        if(this.emotionalState != null){
            map.put("emotional_state",this.emotionalState.getCode());
        }
        if(this.location != null){
            map.put("location",this.location.getGeoHashString());
        }
        putIfNotNull(map, "picture", this.picture);
        putIfNotNull(map, "username", this.username);
        putIfNotNull(map, "date", this.timestamp);
        putIfNotNull(map, "social_situation", this.socialSituation);
        putIfNotNull(map, "trigger", this.trigger);
        putIfNotNull(map, "is_public", this.isPublic);
        return map;
    }

    /**
     * Helper method to add a key-value pair to a map only if the value is not null.
     * Used by toFireStoreMap() to ensure only non-null values are stored in Firestore.
     *
     * @param map The map to add the key-value pair to
     * @param key The key to use
     * @param value The value to add if not null
     */
    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Returns the formatted date string for display purposes.
     *
     * @return A string representation of the timestamp in "yyyy-MM-dd HH:mm" format.
     */
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

}