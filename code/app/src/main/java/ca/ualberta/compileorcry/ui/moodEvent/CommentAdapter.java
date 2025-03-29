package ca.ualberta.compileorcry.ui.moodEvent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.Comment;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.CommentViewHolder> {
    public CommentAdapter() {
        super(DIFF_CALLBACK);
    }

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

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameTextView;
        private final TextView textTextView;
        private final TextView timestampTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textview_username);
            textTextView = itemView.findViewById(R.id.textview_comment);
            timestampTextView = itemView.findViewById(R.id.textview_date);
        }

        public void bind(Comment comment) {
            usernameTextView.setText(comment.getUsername());
            textTextView.setText(comment.getCommentMsg());
            timestampTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(comment.getDate()));
        }

    }
}



