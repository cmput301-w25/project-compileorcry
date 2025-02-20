package ca.ualberta.compileorcry.domain.models;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class User {
    private final String username;
    private String name;
    private DocumentReference userDocRef;
    private ListenerRegistration listenerRegistration;

    public interface OnUserLoadedListener {
        /**
         * Callback which contains the resulting user from a function.
         * @param user Resulting user from a function. Null if an error occurred.
         */
        void onUserLoaded(User user);
    }

    private User(String username, String name, DocumentReference documentReference){
        this.username = username;
        this.name = name;
        this.userDocRef = documentReference;
        this.attachSnapshotListener();
    }

    /**
     * Get a document reference for a user, from a username.
     * @param username Username of user
     * @return Document reference of user
     */
    private static DocumentReference get_doc_reference_by_username(String username){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        return users.document(username);
    }

    /**
     * Register a new user. On success the User is passed to the callback, otherwise null is passed.
     * @param username Username of new user.
     * @param name Name of new user.
     * @param callback
     */
    public static void register_user(String username, String name, OnUserLoadedListener callback){
        DocumentReference userDocReference = get_doc_reference_by_username(username);
        userDocReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){ // User already exists
                    if (callback != null)
                        callback.onUserLoaded(null);
                } else { // Register new user
                    User newUser = new User(username, name, userDocReference);
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("name", name);
                    userDocReference.set(userData).addOnCompleteListener(utask -> {
                        if(utask.isSuccessful()){
                            if (callback != null)
                                callback.onUserLoaded(newUser);
                            return;
                        }
                        if (callback != null)
                            callback.onUserLoaded(null);

                    });
                }
            } else {
                if (callback != null)
                    callback.onUserLoaded(null);
            }
        });
    }

    /**
     *
     * @param username
     * @param callback
     */
    public static void get_user(String username, OnUserLoadedListener callback){
        DocumentReference userDocReference = get_doc_reference_by_username(username);
        userDocReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){ // User exists
                    if (callback != null) {
                        User newUser = new User(username, document.getString("name"), userDocReference);
                        callback.onUserLoaded(newUser);
                    }
                } else { // User does not exist
                    if (callback != null)
                        callback.onUserLoaded(null);
                }
            } else {
                if (callback != null)
                    callback.onUserLoaded(null);
            }
        });
    }

    private void attachSnapshotListener() {
        listenerRegistration = userDocRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                System.err.println("Listen failed: " + error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Update the name field if it changes in Firestore
                String updatedName = documentSnapshot.getString("name");
                if (updatedName != null && !updatedName.equals(this.name)) {
                    this.name = updatedName;
                    System.out.println("Name updated to: " + this.name);
                }
            } else {
                System.out.println("User document does not exist.");
            }
        });
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }
    public DocumentReference getUserDocRef() {
        return userDocRef;
    }

    /**
     * Change the name of a user
     * @param name New name for the user
     */
    public void setName(String name) {
        this.name = name;
        userDocRef.update(Map.of("name",this.name));
    }
    /*
    The following function was has significant help in design from Deepseek, a bunch of it's mine
    Input: On the android java dev. If I have a document with sub collections. How do I delete all its sub collections considering the limits on the api for java android.
    At:  16:00, Feb 18, 2025
     */
    //This is for deleting the user from the DB.
    public void deleteUserFromDB() {
        deleteSubcollections(this.userDocRef.collection("mood_events"));
        deleteSubcollections(this.userDocRef.collection("follow_request"));
        deleteSubcollections(this.userDocRef.collection("following"));
        deleteSubcollections(this.userDocRef.collection("followers"));
        userDocRef.delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("User document deleted successfully.");
                    if (listenerRegistration != null) {
                        listenerRegistration.remove(); // Stop listening to changes
                    }
                })
                .addOnFailureListener(e -> System.err.println("Error deleting user document: " + e));
    }

    // Helper method to delete subcollections
    public void deleteSubcollections(CollectionReference collectionRef) {
        // Get all documents in the collection
        collectionRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // Use a batch to delete all documents
                            WriteBatch batch = collectionRef.getFirestore().batch();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                batch.delete(document.getReference());
                            }

                            // Commit the batch
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        // All documents deleted successfully
                                        Log.d("Firestore", "All documents deleted successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors
                                        Log.e("Firestore", "Error deleting documents: ", e);
                                    });
                        }
                    } else {
                        // Handle the error
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

}

