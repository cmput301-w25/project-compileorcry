package ca.ualberta.compileorcry.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.ualberta.compileorcry.R;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {
    private final Context context;
    private final List<String> usernames;

    public UserSearchAdapter(Context context, List<String> usernames) {
        this.context = context;
        this.usernames = usernames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.usernameText.setText(usernames.get(position));
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
        }
    }
}
