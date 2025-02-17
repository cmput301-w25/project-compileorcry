package ca.ualberta.compileorcry.features.mood.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

import ca.ualberta.compileorcry.R; // Adjust as necessary
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private List<MoodEvent> moodEvents;

    public MoodAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
    }

    @Override
    public MoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_event, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoodViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return moodEvents.size();
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTimestamp;
        private TextView textViewEmotionalState;
        private TextView textViewTrigger;
        private TextView textViewSocialSituation;

        public MoodViewHolder(View itemView) {
            super(itemView);
            textViewTimestamp = itemView.findViewById(R.id.textview_timestamp);
            textViewEmotionalState = itemView.findViewById(R.id.textview_emotional_state);
            textViewTrigger = itemView.findViewById(R.id.textview_trigger);
            textViewSocialSituation = itemView.findViewById(R.id.textview_social_situation);
        }

        public void bind(MoodEvent event) {
            // Format the timestamp (e.g., "2025-02-17 10:30")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            textViewTimestamp.setText(event.getTimestamp().format(formatter));
            textViewEmotionalState.setText(event.getEmotionalState().name());
            textViewTrigger.setText(event.getTrigger() != null ? event.getTrigger() : "");
            textViewSocialSituation.setText(event.getSocialSituation() != null ? event.getSocialSituation() : "");
        }
    }
}