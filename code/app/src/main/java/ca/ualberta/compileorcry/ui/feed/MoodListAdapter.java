package ca.ualberta.compileorcry.ui.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodListAdapter extends RecyclerView.Adapter<MoodListAdapter.ViewHolder>{
    private MoodList moodList;
    private TextView TimestampTextView;
    private TextView EmotionalStateTextView;
    private TextView TriggerTextView;
    private TextView SocialSituationTextView;

    public MoodListAdapter(MoodList moodList){
        this.moodList = moodList;
    }

    public void setMoodList(MoodList moodList){
        this.moodList = moodList;
        notifyDataSetChanged();
    }

    public MoodList getMoodList(){
        return this.moodList;
    }

    @NonNull
    @Override
    public MoodListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodListAdapter.ViewHolder holder, int position) {
        MoodEvent mood = moodList.getMoodEvents().get(position);
        holder.TimestampTextView.setText(mood.getTimestamp().toString());
        holder.EmotionalStateTextView.setText(mood.getEmotionalState().toString());
        holder.TriggerTextView.setText(mood.getTrigger().toString());
        holder.SocialSituationTextView.setText(mood.getSocialSituation().toString());
    }

    @Override
    public int getItemCount() {
        return moodList.getMoodEvents().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView TimestampTextView, EmotionalStateTextView, TriggerTextView, SocialSituationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            TimestampTextView = itemView.findViewById(R.id.textview_timestamp);
            EmotionalStateTextView = itemView.findViewById(R.id.textview_emotional_state);
            TriggerTextView = itemView.findViewById(R.id.textview_trigger);
            SocialSituationTextView = itemView.findViewById(R.id.textview_social_situation);
        }
    }

}
