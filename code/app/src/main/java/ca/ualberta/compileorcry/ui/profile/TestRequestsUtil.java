package ca.ualberta.compileorcry.ui.profile;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ca.ualberta.compileorcry.domain.models.User;

/**
 * Utility class to create test friend requests.
 * This class provides methods to add test follow requests to the active user's account.
 */
public class TestRequestsUtil {
    private static final String TAG = "TestRequestsUtil";

    /**
     * Creates dummy follow requests from test users to the active user.
     * This method adds random users from a predefined list to the active user's
     * follow_request collection.
     *
     * @param context The context to show Toast messages
     * @param count Number of dummy requests to create (max 5)
     */
    public static void createDummyRequests(Context context, int count) {
        User activeUser = User.getActiveUser();
        if (activeUser == null || activeUser.getUserDocRef() == null) {
            Toast.makeText(context, "No active user found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Limit count to available test users
        count = Math.min(count, 5);

        // List of test usernames to create requests from
        List<String> testUsers = Arrays.asList(
                "testuser1", "testuser2", "testuser3", "testuser4", "testuser5"
        );

        // List of test names
        List<String> testNames = Arrays.asList(
                "Test User", "Jane Doe", "John Smith", "Alex Johnson", "Sam Wilson"
        );

        // Create random selection of users
        Random random = new Random();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, ensure all test users exist in Firestore
        for (int i = 0; i < testUsers.size(); i++) {
            final String username = testUsers.get(i);
            final String name = testNames.get(i);

            DocumentReference userDocRef = db.collection("users").document(username);

            // Check if user exists, create if not
            userDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().exists()) {
                    // Create test user document
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("name", name);
                    userDocRef.set(userData)
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "Test user created: " + username))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error creating test user: " + username, e));
                }
            });
        }

        // Create a shuffled list of indices to pick random users
        List<Integer> indices = Arrays.asList(0, 1, 2, 3, 4);
        java.util.Collections.shuffle(indices);

        // Add follow requests from selected users
        for (int i = 0; i < count; i++) {
            String requestUser = testUsers.get(indices.get(i));

            // Create empty document in follow_request collection
            activeUser.getUserDocRef()
                    .collection("follow_request")
                    .document(requestUser)
                    .set(new HashMap<>())
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Follow request added from: " + requestUser))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error adding follow request", e));
        }

        Toast.makeText(context,
                "Created " + count + " test friend requests",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Clears all test follow requests from the active user.
     *
     * @param context The context to show Toast messages
     */
    public static void clearTestRequests(Context context) {
        User activeUser = User.getActiveUser();
        if (activeUser == null || activeUser.getUserDocRef() == null) {
            Toast.makeText(context, "No active user found", Toast.LENGTH_SHORT).show();
            return;
        }

        activeUser.getUserDocRef().collection("follow_request")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Count deleted items
                        final int[] count = {0};

                        // Delete each document
                        task.getResult().forEach(document -> {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        count[0]++;
                                        Log.d(TAG, "Request deleted: " + document.getId());
                                    })
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error deleting request", e));
                        });

                        Toast.makeText(context,
                                "Cleared all test friend requests",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error getting requests to clear", task.getException());
                        Toast.makeText(context,
                                "Error clearing requests",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}