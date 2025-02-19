package ca.ualberta.compileorcry.domain.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.Map;

public class User {
    private String username;
    private String name;
    private String password;
    private FirebaseFirestore db;
    private CollectionReference users;
    private DocumentReference userDocRef;
    private SetOptions setOptions;
    private ListenerRegistration listenerRegistration;

    //Use this constructor for registering a user
    public User(String username, String name) {
        this.username = username;
        this.name = name;
        this.db = FirebaseFirestore.getInstance();
        this.users = db.collection("users");
        this.userDocRef = users.document(this.username);
        this.setOptions = SetOptions.mergeFields("username", "name");
        User outerUserInstance = this;
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, throw an exception
                        throw new IllegalArgumentException("This username is already in use");
                    } else {
                        // Document does not exist, proceed with setting the document
                        userDocRef.set(outerUserInstance, outerUserInstance.setOptions);
                    }
                } else {
                    // Handle any errors that occurred while fetching the document
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
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

