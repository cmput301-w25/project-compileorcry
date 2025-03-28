package ca.ualberta.compileorcry.domain.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import ca.ualberta.compileorcry.R;

/**
 * Class to represent a User stored in Firestore.
 * <p>
 * The class manages retrieving, updating, and deleting user information.
 * Additionally it tracks the currently logged in user.
 */
public class User {

    private static User activeUser;

    private final String username;
    private String name;
    private final DocumentReference userDocRef;
    private ListenerRegistration listenerRegistration;
    private static final String TAG = "User";

    public interface OnUserLoadedListener {
        /**
         * Callback which contains the resulting user from a function.
         * @param user Resulting user from a function. Null if an error occurred.
         * @param error Description of the error if one occurred, otherwise null.
         */
        void onUserLoaded(User user, String error);
    }

    public interface ActiveUserUpdatedListener {
        void onActiveUserUpdated(boolean resumed, String error);
    }

    /**
     * Constructor for creating a User object.
     * If documentReference is null, creates a display-only user without Firestore functionality.
     *
     * @param username Username of user
     * @param name Display name of user
     * @param documentReference Document reference to user in Firestore (can be null for display-only users)
     */
    public User(String username, String name, DocumentReference documentReference){
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
     * @param callback Callback to receive user object
     */
    public static void register_user(String username, String name, OnUserLoadedListener callback){
        Log.i("Firestore", "Registering User");
        DocumentReference userDocReference = get_doc_reference_by_username(username);
        userDocReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){ // User already exists
                    Log.i("UserRepository", "Username already registered in firebase.");
                    if (callback != null) {
                        callback.onUserLoaded(null, "Username already registered.");
                    }
                } else { // Register new user
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("name", name);
                    userDocReference.set(userData).addOnCompleteListener(utask -> {
                        if(utask.isSuccessful()){ // If added successfully return user and no error
                            if (callback != null)
                                callback.onUserLoaded(new User(username, name, userDocReference), null);
                            return;
                        }
                        Log.e("UserRepository", "Error Registering User");
                        if (callback != null)
                            callback.onUserLoaded(null, "Error occurred during registration.");
                    });
                }
            } else {
                Log.e("UserRepository", "Error Registering User");
                if (callback != null)
                    callback.onUserLoaded(null, "Error occurred during registration.");
            }
        });
    }

    /**
     * Get user object from username
     * @param username Username of user
     * @param callback Callback to receive user object
     */
    public static void get_user(String username, OnUserLoadedListener callback){
        DocumentReference userDocReference = get_doc_reference_by_username(username);
        userDocReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){ // User exists
                    if (callback != null) {
                        User newUser = new User(username, document.getString("name"), userDocReference);
                        callback.onUserLoaded(newUser, null);
                    }
                } else { // User does not exist
                    if (callback != null)
                        callback.onUserLoaded(null, "User does not exist.");
                }
            } else {
                if (callback != null)
                    callback.onUserLoaded(null, "Error while logging in.");
            }
        });
    }

    /**
     * Attaches a snapshot listener to the Firestore document to keep the local user object in sync.
     * Only attaches if the document reference is not null.
     */
    private void attachSnapshotListener() {

        try {
            listenerRegistration = this.userDocRef.addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Listen failed: " + error);
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
                    Log.e(TAG, "Attach Failed. User document does not exist.");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error attaching snapshot listener: " + e.getMessage());
        }
    }

    /**
     * Returns username of the user as a string.
     * @return Username of user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the display name of the user as a string.
     * @return Display name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a reference to the user document in firestore.
     * May return null for display-only users.
     *
     * @return DocumentReference to user in firestore, or null for display-only users.
     */
    public DocumentReference getUserDocRef() {
        return userDocRef;
    }


    /**
     * Change the name of a user.
     * For display-only users, only updates the local name.
     * For regular users, also updates the name in Firestore.
     *
     * @param name New name for the user
     */
    public void setName(String name) {
        this.name = name;
        if(this.userDocRef != null){
            userDocRef.update(Map.of("name", this.name));
        }
    }
    /*
    The following function was has significant help in design from Deepseek, a bunch of it's mine
    Input: On the android java dev. If I have a document with sub collections. How do I delete all its sub collections considering the limits on the api for java android.
    At:  16:00, Feb 18, 2025
     */

    /**
     * Deletes the user object and related entries from the user collection and sub-collections.
     * Does nothing for display-only users.
     */
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
    private void deleteSubcollections(CollectionReference collectionRef) {
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
                                        Log.d("UserRepository", "All documents deleted successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors
                                        Log.e("UserRepository", "Error deleting documents: ", e);
                                    });
                        }
                    } else {
                        // Handle the error
                        Log.e("UserRepository", "Error getting documents: ", task.getException());
                    }
                });
    }

    /**
     * Returns the object of the currently logged in user. Returns null if logged out
     * @return Logged-in user object
     */
    public static User getActiveUser(){
        return activeUser;
    }

    private final static String loggedin_key = "ca.ualberta.compileorcry.USER_ACTIVE";
    private final static String username_key = "ca.ualberta.compileorcry.ACTIVE_USERNAME";

    /**
     * Set's the currently active user object so it will persist across app restarts.
     * <p>
     * Should only be used by login, register, or logout actions.
     *
     * @param user User object of the currently logged-in user.
     * @param activity FragmentActivity of active fragment
     */
    public static void setActiveUserPersist(User user, FragmentActivity activity){
        setActiveUser(user);

        // Update activeUser in persistent data
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(user == null){
            editor.putBoolean(loggedin_key, false);
            editor.putString(username_key, "");
        } else {
            editor.putBoolean(loggedin_key, true);
            editor.putString(username_key, user.username);
        }
        editor.apply();
    }

    /**
     * Sets the currently active user object.
     * <p>
     * Should only be used by login, register, or logout actions.
     * <p>
     * The active user will not persist across app restarts. To do this, use:
     * <pre>
     * {@code
     * User.setActiveUserPersist(User user, FragmentActivity activity)
     * }
     * </pre>
     * @param user User object of the currently logged-in user.
     */
    public static void setActiveUser(User user){
        activeUser = user;
    }

    /**
     * Check if a user was active and if so, restore the activeUser variable. To only be ran on startup or resume.
     * @param activity FragmentActivity of active fragment
     */
    public static void checkActiveUser(FragmentActivity activity, ActiveUserUpdatedListener callback){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.getBoolean(loggedin_key, false)){
            get_user(sharedPref.getString(username_key, ""), (User user, String error) -> {
                if(error == null && user != null){ // If found resumed user, set and return
                    setActiveUserPersist(user, activity);
                    callback.onActiveUserUpdated(true, null);
                } else { // On error getting active user
                    if(callback != null)
                        callback.onActiveUserUpdated(false, error);
                }
            });
        } else {
            callback.onActiveUserUpdated(false, null);
        }
    }

    /**
     * Logout signed in user
     * Will reset the activeUser and navigation to the login fragment
     * @param activity FragmentActivity of active fragment
     */
    public static void logoutUser(FragmentActivity activity){
        setActiveUserPersist(null, activity); // Reset activeUser

        // Navigate to login
        NavHostFragment navHostFragment = (NavHostFragment) activity.getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        navHostFragment.getNavController().navigate(R.id.navigation_login);
        activity.findViewById(R.id.nav_view).setVisibility(BottomNavigationView.GONE);
    }

    /**
     * Cleanup method to remove any attached listeners.
     * Should be called when the user object is no longer needed.
     */
    public void cleanup() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}