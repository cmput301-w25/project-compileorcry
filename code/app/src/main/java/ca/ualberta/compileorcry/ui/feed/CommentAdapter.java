package ca.ualberta.compileorcry.ui.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.Comment;

/**
 * Adapter for displaying a list of comments in a RecyclerView.
 * Uses DiffUtil for efficient updates, so basically loads changes, not entire list over again.
 * (Updates with submitList() which is for listviews, but recyclerviews are just the better version of them)
 */
public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.CommentViewHolder> {
    /** Constructor for CommentAdapter. */
    public CommentAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * DiffUtil callback for comparing comment objects efficiently.
     */
    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getId().equals(newItem.getId());  // Ensure comments have unique IDs
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.equals(newItem);
        }
    };

    /**
     * create and bind viewHolder to survive configuration changes/reuse existing view
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bind(comment);
    }

    /**
     * ViewHolder class for displaying individual comments.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        // Comment attributes visible in each comment item
        private final TextView usernameTextView;
        private final TextView textTextView;
        private final TextView timestampTextView;

        /** Constructor for CommentViewHolder. */
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            // bind to UI elements in item_comment.xml
            usernameTextView = itemView.findViewById(R.id.textview_username);
            textTextView = itemView.findViewById(R.id.textview_comment);
            timestampTextView = itemView.findViewById(R.id.textview_date);
        }

        /**
         * Binds a comment to the ViewHolder.
         *
         * @param comment The comment object to display.
         */
        public void bind(Comment comment) {
            usernameTextView.setText(comment.getUsername());
            textTextView.setText(comment.getCommentMsg());
            timestampTextView.setText(new SimpleDateFormat("yyyy-MM-dd '@' HH:mm", Locale.getDefault()).format(comment.getDate().toDate()));
        }

    }
}



