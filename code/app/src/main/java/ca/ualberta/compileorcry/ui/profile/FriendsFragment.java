package ca.ualberta.compileorcry.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentFriendsBinding;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Fragment that displays and manages the user's friends.
 * This class shows a list of the user's followers or people they are following
 * and provides functionality for managing friend relationships.
 *
 * Features:
 * - Toggles between Followers and Following views
 * - Displays users in a RecyclerView
 * - Allows navigation back to the profile page
 */
public class FriendsFragment extends Fragment {
    /**
     * View binding for accessing UI elements
     */
    private FragmentFriendsBinding binding;

    /**
     * Switch for toggling between Followers and Following
     */
    private Switch followSwitch;

    /**
     * RecyclerView to display users
     */
    private RecyclerView recyclerView;

    /**
     * Adapter for the user list
     */
    private UserAdapter userAdapter;

    /**
     * Firestore reference for database operations
     */
    private FirebaseFirestore db;

    /**
     * Tag for logging
     */
    private static final String TAG = "FriendsFragment";

    /**
     * Inflates the fragment layout using view binding and initializes UI elements.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get references to views
        followSwitch = root.findViewById(R.id.switch1);
        recyclerView = root.findViewById(R.id.recyclerView_users);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter();
        recyclerView.setAdapter(userAdapter);

        // Set up user click listener
        userAdapter.setOnUserClickListener((user, position) -> {
            // Handle user click (e.g., view user profile)
            Toast.makeText(getContext(), "Clicked on: " + user.getUsername(), Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    /**
     * Sets up button click listeners and initializes the UI state.
     *
     * @param view               The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up back button
        binding.backButton.setOnClickListener((View v) -> {
            // Navigate back using Navigation component
            Navigation.findNavController(view).navigateUp();
        });

        // Set up switch listener
        followSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateContent(isChecked);
            }
        });

        // Initialize the content based on the initial switch state
        updateContent(followSwitch.isChecked());
    }

    /**
     * Update the content based on the switch state
     *
     * @param isFollowing true if showing Following, false if showing Followers
     */
    private void updateContent(boolean isFollowing) {
        if (isFollowing) {
            // Switch is checked/on - show Following
            binding.imageView3.setImageResource(R.drawable.txt_following);
            loadFollowingUsers();
        } else {
            // Switch is unchecked/off - show Followers
            binding.imageView3.setImageResource(R.drawable.txt_followers);
            loadFollowerUsers();
        }
    }

    /**
     * Load the list of users that the current user is following
     */
    private void loadFollowingUsers() {
        User activeUser = User.getActiveUser();
        if (activeUser == null) {
            Log.e(TAG, "No active user found");
            return;
        }

        // Get the user's document reference
        DocumentReference userDocRef = activeUser.getUserDocRef();
        if (userDocRef == null) {
            Log.e(TAG, "Active user has no document reference");
            return;
        }

        // Assuming there's a "following" subcollection or field in the user document
        userDocRef.collection("following").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> followingUsers = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming each document in the "following" collection
                            // contains a "userId" field or is the user ID itself
                            String userId = document.getId();

                            // Create a User object for each following user
                            fetchUserDetails(userId, followingUsers);
                        }
                    } else {
                        Log.e(TAG, "Error getting following list", task.getException());
                    }
                });
    }

    /**
     * Load the list of users that are following the current user
     */
    private void loadFollowerUsers() {
        User activeUser = User.getActiveUser();
        if (activeUser == null) {
            Log.e(TAG, "No active user found");
            return;
        }

        // Get the user's document reference
        DocumentReference userDocRef = activeUser.getUserDocRef();
        if (userDocRef == null) {
            Log.e(TAG, "Active user has no document reference");
            return;
        }

        // Assuming there's a "followers" subcollection or field in the user document
        userDocRef.collection("followers").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> followerUsers = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Assuming each document in the "followers" collection
                            // contains a "userId" field or is the user ID itself
                            String userId = document.getId();

                            // Create a User object for each follower
                            fetchUserDetails(userId, followerUsers);
                        }
                    } else {
                        Log.e(TAG, "Error getting followers list", task.getException());
                    }
                });
    }

    /**
     * Fetch full user details from Firestore
     *
     * @param userId   The ID of the user to fetch
     * @param userList The list to add the fetched user to
     */
    private void fetchUserDetails(String userId, List<User> userList) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Create a User object from the document data
                        String username = documentSnapshot.getString("username");
                        String name = documentSnapshot.getString("name");

                        // Create a display-only User object without attaching listeners
                        User user = User.createDisplayUser(username, name);
                        userList.add(user);

                        // Update the adapter after adding the user
                        userAdapter.updateUserList(userList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user details", e);
                });
    }

    /**
     * Cleans up resources when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}