package ca.ualberta.compileorcry.features.mood.model;

import com.firebase.geofire.core.GeoHash;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MoodEvent {
    private String id;
    public void setUsername(String username) {
        this.username = username;
    }

    public void setLocation(GeoHash location) {
        this.location = location;
    }

    private Timestamp timestamp; // changed from LocalDateTime to Date
    private EmotionalState emotionalState;
    private Object picture;         //TODO: this is not implemented yet, setters and getter will need to be fixed once we have a datatype
    private String trigger;          // Optional text
    private String socialSituation;  // Optional
    private String username;  // Optional
    private GeoHash location;  //TODO: this is not fully implemented yet, any setter needs to know how to get the geopoint
    public MoodEvent(EmotionalState emotionalState, String trigger, String socialSituation) {
        if (emotionalState == null) {
            throw new IllegalArgumentException("Emotional state is required");
        }
        this.id = UUID.randomUUID().toString();
        this.timestamp = Timestamp.now(); // Capture current date/time using java.util.Date
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;

    }
    public MoodEvent(String id) {
        this.id = id;
    }

    // Getters
    public String getId() {
        return id;
    }

    public Timestamp getTimestamp() {
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
    public String getUsername() {
        return username;
    }

    public GeoHash getLocation() {
        return location;
    }

    public Object getPicture() {
        return picture;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setEmotionalState(EmotionalState emotionalState) {
        this.emotionalState = emotionalState;
    }

    public void setPicture(Object picture) {
        this.picture = picture;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

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
        return map;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

}