package ca.ualberta.compileorcry.features.mood.model;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The MoodEvent class represents a single mood event entry in the application.
 * It encapsulates all data associated with a user's recorded mood, including
 * emotional state, timestamp, location, and contextual information.
 *
 * Each mood event contains:
 * - A unique identifier
 * - Timestamp when the mood was recorded
 * - Required emotional state (from EmotionalState enum)
 * - Requred boolean to determine if the moodEvent is public
 * - Optional trigger text explanation (reason for the mood)
 * - Optional social situation context
 * - Optional location data as GeoHash
 * - Optional photograph
 *
 */
public class MoodEvent implements Serializable {
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
    private GeoHash location;
    private Boolean isPublic;
    private  Boolean commentsLoaded = false;
    private ArrayList<Comment> comments = new ArrayList<>();

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
        this.location = null;
    }

    public MoodEvent(EmotionalState emotionalState, Timestamp date, String trigger, String socialSituation, String picture, Boolean isPublic, GeoHash location) {
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
        this.location = location;
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
     * Decodes a GeoHash into latitude and longitude.
     */
    public LatLng getDecodedLocation() {
        GeoLocation geoLocation = GeoHash.locationFromHash(location.getGeoHashString());
        return new LatLng(geoLocation.latitude, geoLocation.longitude);
    }

    /**
     * Returns the picture associated with this mood event.
     *
     * @return The picture path, or null if not set
     */
    public String getPicture() {
        return picture;
    }


    /**
     * Returns if the mood event is a public mood event
     *
     * @return true or false depending on if the moodEvent is public
     */
    public Boolean getIsPublic() { return this.isPublic; }

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
     * @param val true if the event is public, false if not.
     */
    public void setIsPublic(Boolean val) {
        this.isPublic = val;
    }

    /**
     *
     * @return returns a map of all relevant date to be used in firestore interactions
     */
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
     * @return A string representation of the timestamp in "yyyy-MM-dd @ HH:mm" format.
     */
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '@' HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    /**
     * Call for getting the comments of a moodEvent. Check if the return is null as a timeout will return false
     * Runs synchronously so no need for callbacks.
     * The moodUsername is needed if you're using a personal query as they do not have username.
     * Note that this does not implement a snapshot listener and thus does not update in realtime.
     * Use {@link #reloadComments} to ensure comments are up to date.
     * @param moodUsername Use to pass in the username of MoodEvent
     * @return Returns the list of Commons
     * @throws RuntimeException
     */
    public ArrayList<Comment> getComments(String moodUsername) throws InterruptedException {
        if(!this.isPublic){
            throw new RuntimeException("private moodEvents cannot have comments");
        }
        if(!(this.username == null)){
            moodUsername = this.username;
        }
        if(moodUsername == null){
            throw new RuntimeException("username and moodEvent username are null");
        }
        if(!commentsLoaded){
            comments.clear();
            //Executor prevents deadlock due to the firestore operations callbacks hapening on main
            ExecutorService executor = Executors.newSingleThreadExecutor();
            //Runs the firestore stuff
            String finalUsername = moodUsername;
            executor.execute(() -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                QuerySnapshot commentsSnapshot;
                try {
                    commentsSnapshot = Tasks.await(db.collection("users").document(finalUsername).collection("mood_events").document(this.id).collection("comments").get());
                    for (DocumentSnapshot doc : commentsSnapshot.getDocuments()) {
                        Map<String,Object> docData = doc.getData();
                        if(this.isValidCommentMap(docData)){
                            comments.add(new Comment(this,
                                (String) docData.get("username"),
                                doc.getId(),
                                Timestamp.now(),
                                (String) docData.get("comment_message")));
                        } else {
                            doc.getReference().delete();
                        }
                    }
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            executor.shutdown();
            Boolean success = executor.awaitTermination(10, TimeUnit.SECONDS);
            if(!success){
                comments = null;
            } else {
                commentsLoaded = true;
                return comments;
            }
        }
        return comments;
    }

    /**
     * Listener for AddCommentCallbacks
     */
    public interface AddCommentCallback {
        void onSuccess() throws InterruptedException;
        void onFailure(Exception e);
    }

    /**
     * Method for adding comments to a moodEvent
     * Unlike get comments this is not synchronous and requires a callback so you can handle if it fails.
     * The username of a moodEvent can be null if it's from a personal query.
     *
     * @param toAdd the comment to be added
     * @param callback  a listener for success or failure of the addition
     * @param username  optional username for if the moodEvent has a null username
     */
    public void addComment(Comment toAdd, AddCommentCallback callback, String username){
        if(!this.isPublic){
            throw new RuntimeException("private moodEvents cannot have comments");
        }
        if(!this.containsComment(toAdd)){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if(!(this.username == null)){
                username = this.username;
            }
            if(username == null){
                throw new RuntimeException("username and moodEvent username are null");
            }
            DocumentReference docRef = db.collection("users").document(username).collection("mood_events").document(this.id).collection("comments").document();
            toAdd.setIdFromDocRef(docRef);
            Map<String,Object> commentMap = toAdd.toFireStoreMap();
            // Write failed
            docRef.set(commentMap)
                    .addOnSuccessListener(aVoid -> {
                        // Write succeeded
                        comments.add(toAdd);
                        try {
                            callback.onSuccess();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .addOnFailureListener(callback::onFailure);
        }
    }
    /**
     * Validates a map of data to ensure it is suitable for storing a comment in the firstore
     *
     * @param map The map of data to validate.
     * @return True if the data is valid, false otherwise.
     */
    private boolean isValidCommentMap(Map<String,Object> map){
        Set<String> requiredKeys = Set.of("username","date_made","comment_message");
        if(!map.keySet().containsAll(requiredKeys)){
            return false;
        }
        if(!(map.get("username") instanceof String)){
            return false;
        }
        if(!(map.get("date_made") instanceof Timestamp)){
            return false;
        }
        if(!(map.get("comment_message") instanceof String)){
            return false;
        }
        return true;
    }

    /**
     * Use this is you want to reload the comments.
     *
     * @param username  The moodEvent username if its null
     * @throws InterruptedException
     */
    public void reloadComments(String username) throws InterruptedException {
        this.commentsLoaded = false;
        if(!(this.username == null)){
            username = this.username;
        }
        if(username == null){
            throw new RuntimeException("username and moodEvent username are null");
        }
        this.getComments(username);
    }

    /**
     * See if a comment with the same ID as the comment is passed in exist.
     * Note that Comments from different MoodEvents can share the same ID.
     * This is intended for internal use but I've left it public if someone finds use of it.
     *
     * @param comment The comment to check if it's contained in the MoodList
     * @return Returns true if the moodList contains the comment and false if not, the ID is the comparison
     *
     */
    public boolean containsComment(Comment comment){
        for(Comment containedComment: this.comments){
            if(comment.getId().equals(containedComment.getId())){
                return true;
            }
        }
        return false;
    }

    /**
     * Use this to see if a MoodEvent has comments
     *
     * @param username      The username of the comment owner, will be overwritten if the parent event has a username
     * @return              Returns a string, either "yes","no","null","failed", yes is an afirmation that the moodEvent has comments
     * @throws InterruptedException
     */
    public String hasComments(String username) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<String> returnString = new AtomicReference<>("null");
        // Runs the firestore stuff
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(!(this.username == null)){
            username = this.username;
        }
        if(username == null){
            throw new RuntimeException("username and moodEvent username are null");
        }
        String finalUsername = username;
        executor.execute(() -> {
            QuerySnapshot commentsSnapshot;
            try {
                commentsSnapshot = Tasks.await(db.collection("users").document(finalUsername).collection("mood_events").document(this.id).collection("comments").get());
                if(commentsSnapshot.size() <= 0){
                    returnString.set("no");
                } else {
                    returnString.set("yes");
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();
        Boolean success = executor.awaitTermination(10, TimeUnit.SECONDS);
        if(!success){
            returnString.set("failed");
            return returnString.get();
        } else {
            return returnString.get();
        }
    }
}