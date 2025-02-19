package ca.ualberta.compileorcry.features.mood.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<MoodEvent> moodEvents;

    public MoodAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_event, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return moodEvents.size();
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTimestamp;
        private final TextView textViewEmotionalState;
        private final TextView textViewTrigger;
        private final TextView textViewSocialSituation;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTimestamp = itemView.findViewById(R.id.textview_timestamp);
            textViewEmotionalState = itemView.findViewById(R.id.textview_emotional_state);
            textViewTrigger = itemView.findViewById(R.id.textview_trigger);
            textViewSocialSituation = itemView.findViewById(R.id.textview_social_situation);
        }

        public void bind(MoodEvent event) {
            // Use SimpleDateFormat to format the java.util.Date timestamp.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            textViewTimestamp.setText(sdf.format(event.getTimestamp()));
            textViewEmotionalState.setText(event.getEmotionalState().name());
            textViewTrigger.setText(event.getTrigger() != null ? event.getTrigger() : "");
            textViewSocialSituation.setText(event.getSocialSituation() != null ? event.getSocialSituation() : "");
        }
    }
}