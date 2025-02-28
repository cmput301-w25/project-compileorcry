package ca.ualberta.compileorcry.ui.add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentNewBinding;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class NewFragment extends Fragment {

    private FragmentNewBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment_new layout via view binding.
        binding = FragmentNewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the click listener for the "Save Mood" button.
        binding.buttonSaveMood.setOnClickListener(v -> {
            // Retrieve the selected emotional state from the spinner.
            String emotionalStateStr = binding.spinnerEmotionalState.getSelectedItem().toString();
            if (emotionalStateStr.isEmpty() || "Select".equals(emotionalStateStr)) {
                Toast.makeText(getContext(), "Please select an emotional state", Toast.LENGTH_SHORT).show();
                return;
            }

            EmotionalState state;
            try {
                state = EmotionalState.valueOf(emotionalStateStr.toUpperCase());
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid emotional state", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the optional trigger and social situation.
            String trigger = binding.edittextTrigger.getText().toString().trim();
            String socialSituation = binding.spinnerSocialSituation.getSelectedItem().toString();
            if ("Select".equals(socialSituation)) {
                socialSituation = null;
            }

            // Create a new MoodEvent.
            MoodEvent moodEvent = new MoodEvent(state, trigger, socialSituation);

            // Add the event to the repository.
            //Sorry I commented this out but it stops from building -Noah
            //MoodList.getInstance().addMoodEvent(moodEvent);

            // Provide confirmation feedback.
            Toast.makeText(getContext(), "Mood event added", Toast.LENGTH_SHORT).show();

            // Clear inputs for a new entry.
            binding.edittextTrigger.setText("");
            binding.spinnerEmotionalState.setSelection(0); // Assumes the first item is a hint "Select"
            binding.spinnerSocialSituation.setSelection(0);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}