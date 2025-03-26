package ca.ualberta.compileorcry.ui.feed;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

/**
 * Adapter for displaying MoodEvent objects in a RecyclerView.
 * This adapter handles the creation and binding of ViewHolders that display
 * mood events with appropriate styling based on their emotional state.
 *
 * Features:
 * - Displays emotional state with corresponding color
 * - Shows timestamp, trigger text, and social situation
 * - Handles proper spacing between items
 * - Safely handles null references and optional fields
 */
public class MoodEventAdapter extends RecyclerView.Adapter<MoodEventAdapter.ViewHolder> {
    /** The current list of mood events to display */
    private List<MoodEvent> moodEvents;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(MoodEvent moodEvent);
    }

    /**
     * Constructs a new adapter with the given list of mood events.
     * If the provided list is null, an empty list will be used.
     *
     * @param moodEvents List of mood events to display
     */
    public MoodEventAdapter(List<MoodEvent> moodEvents, OnItemClickListener clickListener) {
        this.moodEvents = moodEvents != null ? moodEvents : new ArrayList<>();
        this.clickListener = clickListener;
    }

    /**
     * Updates the adapter's data with a new list of mood events.
     * If the provided list is null, an empty list will be used.
     * This method also triggers UI updates through notifyDataSetChanged().
     *
     * @param newEvents New list of mood events to display
     */
    public void updateData(List<MoodEvent> newEvents) {
        moodEvents = newEvents != null ? newEvents : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder by inflating the item layout.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (not used in this implementation)
     * @return A new ViewHolder that holds the View for each mood event item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_event, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to a ViewHolder at the specified position.
     * This method:
     * 1. Sets proper spacing between items
     * 2. Applies the appropriate background color based on emotional state
     * 3. Binds all mood event data to the ViewHolder
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MoodEvent event = moodEvents.get(position);

        // Space out list items (mood events)
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        layoutParams.bottomMargin = 22; // Add space between items
        holder.itemView.setLayoutParams(layoutParams);

        // Get color of mood event
        EmotionalState state = event.getEmotionalState();
        int moodColor = state.getColor(holder.itemView.getContext());
        Drawable background = holder.itemView.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(background);
        DrawableCompat.setTint(wrappedDrawable, moodColor);
        holder.itemView.setBackground(wrappedDrawable);
        holder.bind(event);

        // Open MoodEventDialogFragment when clicked
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(event);
            }
        });
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The total number of mood events
     */
    @Override
    public int getItemCount() {
        return moodEvents.size();
    }

    /**
     * ViewHolder class that contains the UI elements for a single mood event item.
     * This class is responsible for binding MoodEvent data to the UI elements.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView emotionalStateTextView;
        private final TextView timestampTextView;
        private final ImageView moodIconImageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            emotionalStateTextView = itemView.findViewById(R.id.textview_emotional_state);
            timestampTextView = itemView.findViewById(R.id.textview_timestamp);
            moodIconImageView = itemView.findViewById(R.id.mood_icon);
        }

        public void bind(MoodEvent event) {
            if (event != null) {
                emotionalStateTextView.setText(event.getEmotionalState().getDescription());
                timestampTextView.setText(event.getFormattedDate());
                moodIconImageView.setImageResource(event.getEmotionalState().getIconResId());
            }
        }
    }
}
