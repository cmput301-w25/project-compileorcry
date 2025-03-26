package ca.ualberta.compileorcry.features.mood.model;

import com.google.firebase.Timestamp;

public class Comment {
    private String id;
    private String username;
    private String text;
    private Timestamp timestamp;

    public Comment() {
        // Empty constructor for Firestore
    }

    public Comment(String username, String text, Timestamp timestamp) {
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
}

