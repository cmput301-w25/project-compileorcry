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

/**
 * Bottom sheet dialog fragment that displays pending friend requests.
 * This class shows a list of users who have sent friend requests to the current user
 * and provides options to accept or decline each request.
 */
public class RequestsBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "RequestsBottomSheet";

    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;
    private TextView emptyView;
    private View progressBar;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up views
        recyclerView = view.findViewById(R.id.recyclerView_requests);
        emptyView = view.findViewById(R.id.empty_message);
        progressBar = view.findViewById(R.id.progress_bar);

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
     * Load pending friend requests from Firestore
     */
    private void loadFriendRequests() {
        User activeUser = User.getActiveUser();
        if (activeUser == null) {
            showEmptyView("No active user found");
            return;
        }

        // Show loading indicator
        showLoading(true);

        DocumentReference userDocRef = activeUser.getUserDocRef();
        if (userDocRef == null) {
            showLoading(false);
            showEmptyView("User data not available");
            return;
        }

        // Get friend requests from the "follow_request" subcollection
        userDocRef.collection("follow_request").get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            showEmptyView("No pending friend requests");
                            return;
                        }

                        List<User> requestUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            fetchUserDetails(userId, requestUsers);
                        }
                    } else {
                        Log.e(TAG, "Error getting friend requests", task.getException());
                        showEmptyView("Unable to load friend requests");
                    }
                });
    }

    /**
     * Fetch user details for each request
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
     * Accept a friend request
     */
    private void acceptRequest(User requestUser, int position) {
        User activeUser = User.getActiveUser();
        if (activeUser == null || activeUser.getUserDocRef() == null || requestUser == null) {
            return;
        }

        showLoading(true);

        // 1. Remove from follow_request collection
        activeUser.getUserDocRef()
                .collection("follow_request")
                .document(requestUser.getUsername())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Add to followers collection
                    activeUser.getUserDocRef()
                            .collection("followers")
                            .document(requestUser.getUsername())
                            .set(new HashMap<>())
                            .addOnSuccessListener(aVoid2 -> {
                                // 3. Add to the other user's following collection
                                requestUser.getUserDocRef()
                                        .collection("following")
                                        .document(activeUser.getUsername())
                                        .set(new HashMap<>())
                                        .addOnSuccessListener(aVoid3 -> {
                                            showLoading(false);
                                            // Remove from adapter
                                            requestAdapter.removeItem(position);
                                            if (requestAdapter.getItemCount() == 0) {
                                                showEmptyView("No pending friend requests");
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            showLoading(false);
                                            Log.e(TAG, "Error adding to following", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Log.e(TAG, "Error adding to followers", e);
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error removing request", e);
                });
    }

    /**
     * Decline a friend request
     */
    private void declineRequest(User requestUser, int position) {
        User activeUser = User.getActiveUser();
        if (activeUser == null || activeUser.getUserDocRef() == null || requestUser == null) {
            return;
        }

        showLoading(true);

        // Remove from follow_request collection
        activeUser.getUserDocRef()
                .collection("follow_request")
                .document(requestUser.getUsername())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    // Remove from adapter
                    requestAdapter.removeItem(position);
                    if (requestAdapter.getItemCount() == 0) {
                        showEmptyView("No pending friend requests");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error declining request", e);
                });
    }

    /**
     * Show/hide the loading indicator
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Show the empty view with a message
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
     * Adapter for friend requests
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