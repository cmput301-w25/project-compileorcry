package ca.ualberta.compileorcry.ui.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

// MoodEventAdapter.java
public class MoodEventAdapter extends RecyclerView.Adapter<MoodEventAdapter.ViewHolder> {
    private List<MoodEvent> moodEvents;

    public MoodEventAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents != null ? moodEvents : new ArrayList<>();
    }

    public void updateData(List<MoodEvent> newEvents) {
        moodEvents = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return moodEvents.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView emotionalStateTextView;
        private final TextView timestampTextView;
        private final TextView triggerTextView;
        private final TextView socialSituationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionalStateTextView = itemView.findViewById(R.id.textview_emotional_state);
            timestampTextView = itemView.findViewById(R.id.textview_timestamp);
            triggerTextView = itemView.findViewById(R.id.textview_trigger);
            socialSituationTextView = itemView.findViewById(R.id.textview_social_situation);
        }

        public void bind(MoodEvent event) {
            if (event != null) {
                emotionalStateTextView.setText(event.getEmotionalState().getDescription());
                timestampTextView.setText(event.getFormattedDate());
                // Assuming you have these methods in your MoodEvent class
                if (event.getTrigger() != null) {
                    triggerTextView.setText(event.getTrigger());
                }
                if (event.getSocialSituation() != null) {
                    socialSituationTextView.setText(event.getSocialSituation().toString());
                }
            }
        }
    }
}
