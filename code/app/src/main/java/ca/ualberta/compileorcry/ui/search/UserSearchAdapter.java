package ca.ualberta.compileorcry.ui.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Adapter class for displaying a list of usernames in the user search results.
 * Each item shows a placeholder profile picture, the user's display name,
 * and their username. Items are clickable, currently showing a toast on click.
 */
public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    private final Context context;
    private List<User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(String username);
    }


    /**
     * Constructs a new UserSearchAdapter.
     *
     * @param context   the context in which the adapter is used
     * @param users a list of usernames to display
     */
    public UserSearchAdapter(Context context, List<User> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    /**
     * Creates a new view holder by inflating the item layout.
     *
     * @param parent   the parent view group
     * @param viewType the view type of the new view
     * @return a new instance of UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds data to a specific view holder.
     *
     * @param holder   the view holder to bind data to
     * @param position the position in the dataset
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameText.setText("@" + user.getUsername());
        holder.nameText.setText(user.getName());
        holder.profileImage.setImageResource(R.drawable.ic_person_24dp);

        // Set up click behavior
        holder.itemView.setOnClickListener(v -> {
            listener.onUserClick(user.getUsername());
        });
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return number of usernames in the list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class that represents a single item view in the search results.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView nameText;
        ImageView profileImage;

        /**
         * Constructs a new UserViewHolder.
         *
         * @param itemView the view of the item
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            nameText = itemView.findViewById(R.id.name_text);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
