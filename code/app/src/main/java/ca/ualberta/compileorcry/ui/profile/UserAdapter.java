package ca.ualberta.compileorcry.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * A RecyclerView adapter for displaying user items in lists throughout the application.
 *
 * <p>This adapter provides functionality to display user data in a consistent format
 * across different sections of the application, particularly in the followers and following
 * lists. Each user item displays:
 * <ul>
 *   <li>A user icon/avatar</li>
 *   <li>The user's username (prefixed with '@')</li>
 * </ul>
 *
 * <p>The adapter supports click interactions with user items through the
 * {@link OnUserClickListener} interface, allowing activities or fragments using this
 * adapter to respond when a user is selected from the list.</p>
 *
 * <p>The adapter maintains an internal list of {@link User} objects and provides methods
 * to update this list when new data is available. Empty list handling is supported
 * by defaulting to an empty ArrayList when null data is provided.</p>
 *
 * <p>Example usage:
 * <pre>
 * // Initialize the adapter
 * UserAdapter userAdapter = new UserAdapter();
 * recyclerView.setAdapter(userAdapter);
 *
 * // Set up click listener
 * userAdapter.setOnUserClickListener((user, position) -> {
 *     // Handle user selection
 * });
 *
 * // Update with new data
 * userAdapter.updateUserList(newUsersList);
 * </pre>
 *
 * @see RecyclerView.Adapter
 * @see User
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    /** List of users to display */
    private List<User> userList;

    /** Optional click listener for user item interactions */
    private OnUserClickListener clickListener;

    /**
     * Interface for handling user item click events
     */
    public interface OnUserClickListener {
        void onUserClick(User user, int position);
    }

    /**
     * Constructor to initialize the adapter with an empty user list
     */
    public UserAdapter() {
        this.userList = new ArrayList<>();
    }

    /**
     * Constructor to initialize the adapter with a user list
     *
     * @param userList The initial list of users to display
     */
    public UserAdapter(List<User> userList) {
        this.userList = userList != null ? userList : new ArrayList<>();
    }

    /**
     * Sets a click listener for user item interactions
     *
     * @param listener The click listener to set
     */
    public void setOnUserClickListener(OnUserClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Updates the adapter with a new list of users
     *
     * @param newUserList The new list of users to display
     */
    public void updateUserList(List<User> newUserList) {
        this.userList = newUserList != null ? newUserList : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder by inflating the user item layout.
     * This is called by the RecyclerView when it needs a new ViewHolder.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (unused in this implementation)
     * @return A new UserViewHolder that holds a user item view
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    /**
     * Binds user data to a ViewHolder at the specified position.
     * This method:
     * <ul>
     *   <li>Sets the username text (prefixed with '@')</li>
     *   <li>Sets up the click listener for the item</li>
     * </ul>
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTextView.setText("@" + user.getUsername());

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onUserClick(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for user items in the RecyclerView.
     * Holds references to the views within each user list item.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public ImageView userIconImageView;
        public TextView usernameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userIconImageView = itemView.findViewById(R.id.user_icon);
            usernameTextView = itemView.findViewById(R.id.textview_username);
        }
    }
}