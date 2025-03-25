package ca.ualberta.compileorcry.ui.moodEvent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameTextView;
        private final TextView textTextView;
        private final TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textview_username);
            textTextView = itemView.findViewById(R.id.textview_comment);
            timestampTextView = itemView.findViewById(R.id.textview_timestamp);
        }

        public void bind(Comment comment) {
            usernameTextView.setText(comment.getUsername());
            textTextView.setText(comment.getText());
            timestampTextView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(comment.getTimestamp().toDate()));
        }
    }
}

