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
 * Adapter for displaying user items in a RecyclerView.
 * This adapter handles the display of users for both the followers and following lists.
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

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTextView.setText("@" + user.getUsername());

        // You can add more fields here if the User model has more attributes
        // For example, if the User model has a display name:
        // holder.nameTextView.setText(user.getName());

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
     * ViewHolder for the user item views
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