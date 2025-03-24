package ca.ualberta.compileorcry.features.mood.model;

import com.google.firebase.Timestamp;

public class Comment {
    private final MoodEvent parent;   //The mood event the comment is on
    private final String username;    //The username of the commenter
    private String commentMsg;  //The text of the comment
    private final String id;          //Firestore id of the comment
    private final Timestamp dateMade;     //Date the comment was made
    private Timestamp dateEdited;     //Date the comment was edited, here for wow factor if we want to add editing

    public Comment(MoodEvent parent, String username, String id, Timestamp dateMade) {
        this.parent = parent;
        this.username = username;
        this.id = id;
        this.dateMade = dateMade;
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

    public Timestamp getDate() {
        return dateMade;
    }

}
