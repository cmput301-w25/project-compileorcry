package ca.ualberta.compileorcry.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.FollowHelper;

/**
 * A bottom sheet dialog fragment that displays and manages pending follow/friend requests.
 *
 * <p>This component presents a list of users who have requested to follow the current user,
 * retrieved from the Firestore database. For each request, the sheet displays:
 * <ul>
 *   <li>The requester's profile picture</li>
 *   <li>The requester's username</li>
 *   <li>The requester's display name</li>
 *   <li>Accept and Decline buttons to respond to the request</li>
 * </ul>
 * </p>
 *
 * <p>The bottom sheet handles various states:
 * <ul>
 *   <li><b>Empty state</b> - When no requests are available</li>
 *   <li><b>Error state</b> - When there's an issue loading the requests</li>
 *   <li><b>Content state</b> - When requests are available and displayed in a list</li>
 * </ul>
 * </p>
 *
 * <p>The implementation uses {@link FollowHelper} to handle database operations
 * related to accepting or declining requests, ensuring consistent data handling
 * across the application.</p>
 *
 * <p>Typical usage:
 * <pre>
 * RequestsBottomSheet requestsBottomSheet = new RequestsBottomSheet();
 * requestsBottomSheet.show(getChildFragmentManager(), "requestsBottomSheet");
 * </pre>
 * </p>
 *
 * @see BottomSheetDialogFragment
 * @see FollowHelper
 * @see User
 */
public class RequestsBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "RequestsBottomSheet";

    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;
    private TextView emptyView;
    private FirebaseFirestore db;

    /**
     * Inflates the bottom sheet layout.
     *
     * @param inflater The LayoutInflater object for creating views
     * @param container The parent view that will contain the bottom sheet's UI
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The root View for the bottom sheet's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_requests, container, false);
    }

    /**
     * Initializes UI components and loads friend requests data.
     *
     * <p>This method:
     * <ul>
     *   <li>Initializes the Firestore database reference</li>
     *   <li>Sets up the RecyclerView with layout manager and adapter</li>
     *   <li>Configures accept/decline button handlers</li>
     *   <li>Triggers friend request data loading</li>
     * </ul>
     * </p>
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up views
        recyclerView = view.findViewById(R.id.recyclerView_requests);
        emptyView = view.findViewById(R.id.empty_message);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        requestAdapter = new RequestAdapter(new ArrayList<>(), new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(User user, int position) {
                acceptRequest(user, position);
            }

            @Override
            public void onDecline(User user, int position) {
                declineRequest(user, position);
            }
        });
        recyclerView.setAdapter(requestAdapter);

        // Load friend requests
        loadFriendRequests();
    }

    /**
     * Loads pending friend requests from Firestore using FollowHelper.
     *
     * <p>This method:
     * <ul>
     *   <li>Retrieves the active user</li>
     *   <li>Uses FollowHelper to get follow requests from Firestore</li>
     *   <li>Handles various error conditions (no active user, null results, empty lists)</li>
     *   <li>For each request username, fetches complete user details</li>
     * </ul>
     * </p>
     */
    private void loadFriendRequests() {
        User activeUser = User.getActiveUser();
        if (activeUser == null) {
            showEmptyView("No active user found");
            return;
        }

        try {
            // Use FollowHelper to get the list of follow requests
            ArrayList<String> requestUsernames = FollowHelper.getFollowRequest(activeUser.getUsername());

            if (requestUsernames == null) {
                showEmptyView("Error loading friend requests");
                return;
            }

            if (requestUsernames.isEmpty()) {
                showEmptyView("No pending friend requests");
                return;
            }

            // Fetch user details for each requester
            List<User> requestUsers = new ArrayList<>();
            for (String username : requestUsernames) {
                fetchUserDetails(username, requestUsers);
            }

        } catch (InterruptedException e) {
            Log.e(TAG, "Error getting friend requests", e);
            showEmptyView("Unable to load friend requests");
        }
    }

    /**
     * Fetches complete user details for each request from Firestore.
     *
     * <p>For each user ID:
     * <ul>
     *   <li>Queries the main users collection in Firestore</li>
     *   <li>Extracts username and display name</li>
     *   <li>Creates a User object with the retrieved data</li>
     *   <li>Adds the user to the provided list if not already present</li>
     *   <li>Updates the adapter with the sorted list</li>
     *   <li>Handles various error conditions with appropriate UI feedback</li>
     * </ul>
     * </p>
     *
     * @param userId The ID or username of the user to fetch
     * @param userList The list to which the fetched user will be added
     */
    private void fetchUserDetails(String userId, List<User> userList) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String name = documentSnapshot.getString("name");

                        if (username == null) {
                            username = userId;
                        }

                        // Create a user object for display (without document reference for simplicity)
                        DocumentReference userDocRef = db.collection("users").document(userId);
                        User user = new User(username, name, userDocRef);

                        // Add to list if not already present
                        boolean isDuplicate = false;
                        for (User existingUser : userList) {
                            if (existingUser.getUsername().equals(username)) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate) {
                            userList.add(user);
                            java.util.Collections.sort(userList, (u1, u2) ->
                                    u1.getUsername().compareToIgnoreCase(u2.getUsername()));
                            requestAdapter.updateRequestList(userList);

                            // Hide empty view when we have data
                            hideEmptyView();
                        }
                    } else {
                        Log.w(TAG, "User document not found for ID: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user details", e);
                    if (userList.isEmpty()) {
                        showEmptyView("Error loading user data");
                    }
                });
    }

    /**
     * Accepts a friend request from a specified user.
     *
     * <p>This method:
     * <ul>
     *   <li>Verifies that the active user and request user are valid</li>
     *   <li>Uses FollowHelper to handle the acceptance process</li>
     *   <li>Updates the UI to remove the accepted request</li>
     *   <li>Shows the empty view if no requests remain</li>
     *   <li>Handles errors with appropriate logging</li>
     * </ul>
     * </p>
     *
     * @param requestUser The User object representing the person who sent the request
     * @param position The position of the request in the RecyclerView adapter
     */
    private void acceptRequest(User requestUser, int position) {
        User activeUser = User.getActiveUser();
        if (activeUser == null || activeUser.getUserDocRef() == null || requestUser == null) {
            return;
        }

        try {
            // Use the FollowHelper to handle the friend request
            boolean success = FollowHelper.handleFollowRequest(activeUser, requestUser.getUsername(), true);

            if (success) {
                // Remove from adapter on success
                requestAdapter.removeItem(position);

                if (requestAdapter.getItemCount() == 0) {
                    showEmptyView("No pending friend requests");
                }
            } else {
                Log.e(TAG, "Error accepting request: FollowHelper returned false");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error accepting request", e);
        }
    }

    /**
     * Declines a friend request from a specified user.
     *
     * <p>This method:
     * <ul>
     *   <li>Verifies that the active user and request user are valid</li>
     *   <li>Uses FollowHelper to handle the decline process</li>
     *   <li>Updates the UI to remove the declined request</li>
     *   <li>Shows the empty view if no requests remain</li>
     *   <li>Handles errors with appropriate logging</li>
     * </ul>
     * </p>
     *
     * @param requestUser The User object representing the person who sent the request
     * @param position The position of the request in the RecyclerView adapter
     */
    private void declineRequest(User requestUser, int position) {
        User activeUser = User.getActiveUser();
        if (activeUser == null || activeUser.getUserDocRef() == null || requestUser == null) {
            return;
        }

        try {
            // Use the FollowHelper to handle the friend request (decline = false)
            boolean success = FollowHelper.handleFollowRequest(activeUser, requestUser.getUsername(), false);

            if (success) {
                // Remove from adapter on success
                requestAdapter.removeItem(position);

                if (requestAdapter.getItemCount() == 0) {
                    showEmptyView("No pending friend requests");
                }
            } else {
                Log.e(TAG, "Error declining request: FollowHelper returned false");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error declining request", e);
        }
    }

    /**
     * Displays the empty view with a custom message.
     *
     * <p>Shows a message to the user when there are no requests to display
     * or when an error occurs loading the requests. This provides feedback
     * instead of showing a blank screen.</p>
     *
     * @param message The message to display in the empty view
     */
    private void showEmptyView(String message) {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(message);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Hide the empty view
     */
    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Custom adapter for displaying friend requests in a RecyclerView.
     *
     * <p>This adapter:
     * <ul>
     *   <li>Binds User objects to request item views</li>
     *   <li>Manages the list of pending requests</li>
     *   <li>Handles accept/decline button clicks via listener callbacks</li>
     * </ul>
     * </p>
     */
    private static class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

        private List<User> requestList;
        private final OnRequestActionListener listener;

        public interface OnRequestActionListener {
            void onAccept(User user, int position);
            void onDecline(User user, int position);
        }

        public RequestAdapter(List<User> requestList, OnRequestActionListener listener) {
            this.requestList = requestList;
            this.listener = listener;
        }

        public void updateRequestList(List<User> newRequestList) {
            this.requestList = newRequestList;
            notifyDataSetChanged();
        }

        public void removeItem(int position) {
            if (position >= 0 && position < requestList.size()) {
                requestList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, requestList.size());
            }
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            User user = requestList.get(position);
            holder.bind(user, position, listener);
        }

        @Override
        public int getItemCount() {
            return requestList.size();
        }

        static class RequestViewHolder extends RecyclerView.ViewHolder {
            private final TextView usernameTextView;
            private final TextView nameTextView;
            private final View acceptButton;
            private final View declineButton;

            public RequestViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.textview_username);
                nameTextView = itemView.findViewById(R.id.textview_name);
                acceptButton = itemView.findViewById(R.id.button_accept);
                declineButton = itemView.findViewById(R.id.button_decline);
            }

            public void bind(User user, int position, OnRequestActionListener listener) {
                usernameTextView.setText("@" + user.getUsername());
                nameTextView.setText(user.getName());

                acceptButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAccept(user, position);
                    }
                });

                declineButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDecline(user, position);
                    }
                });
            }
        }
    }
}