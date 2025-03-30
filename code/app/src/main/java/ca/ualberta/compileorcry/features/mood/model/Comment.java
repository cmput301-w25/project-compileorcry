package ca.ualberta.compileorcry.features.mood.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The Comment class represents a single comment entry for a mood event.
 * It encapsulates all data associated with a comment.
 *
 * Each mood event contains:
 * - A unique identifier
 * - Timestamp when the comment was recorded
 * - Required emotional state (from EmotionalState enum)
 * - Required String to hold the username of who made the comment.
 * - Required String to hold the message of the comment.
 * - Required MoodEvent to hold the parent MoodEvent.
 */
public class Comment {
    private final MoodEvent parent;   //The mood event the comment is on
    private final String username;    //The username of the commenter
    private String commentMsg;  //The text of the comment
    private String id;          //Firestore id of the comment
    private final Timestamp dateMade;     //Date the comment was made
    private Timestamp dateEdited;     //Date the comment was edited, here for wow factor if we want to add editing

    public Comment(MoodEvent parent, String username, String id, Timestamp dateMade, String commentMsg) {
        this.parent = parent;
        this.username = username;
        this.id = id;
        this.dateMade = dateMade;
        this.commentMsg = commentMsg;
    }

    public MoodEvent getParent() {
        return parent;
    }


    public String getUsername() {
        return username;
    }


    public String getCommentMsg() {
        return commentMsg;
    }
    //set private until we decide if we want to make editing a thing
    private void setCommentMsg(String commentMsg) {
        this.commentMsg = commentMsg;
    }

    public String getId() {
        return id;
    }

    public void setIdFromDocRef(DocumentReference snap){
        this.id = snap.getId();
    }

    public Timestamp getDate() {
        return dateMade;
    }
    /**
     *
     * @return returns a map of all relevant date to be used in firestore interactions
     */
    public Map<String,Object> toFireStoreMap() {
        Map<String,Object> map =  new HashMap<>();
        putIfNotNull(map, "date_made", this.dateMade);
        putIfNotNull(map, "comment_message", this.commentMsg);
        putIfNotNull(map, "username", this.username);
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
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;  // Same object reference
        if (obj == null || getClass() != obj.getClass()) return false; // Type check
        Comment comment = (Comment) obj;
        return id.equals(comment.id);  // Compare IDs
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
