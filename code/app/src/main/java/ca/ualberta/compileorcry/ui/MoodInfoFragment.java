package ca.ualberta.compileorcry.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import ca.ualberta.compileorcry.databinding.FragmentMoodInfoBinding;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodInfoFragment extends Fragment {

    private FragmentMoodInfoBinding binding;
    private MoodEvent moodEvent;
    private MoodList moodList;

    public MoodInfoFragment() {}

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        binding = FragmentMoodInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ){
        super.onViewCreated(view, savedInstanceState);

        String moodEventId = null;


        // Retrieve moodEventId from bundle
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
        }
        /*
        if (moodEventId != null) {
            // Fetch MoodList and get the specific mood event
            MoodList.createMoodList(
                    User.getActiveUser(),
                    QueryType.HISTORY_MODIFIABLE,
                    initializedMoodList -> {
                        moodList = initializedMoodList;

                        for (MoodEvent event : moodList.getMoodEvents()) {
                            if (event.getId().equals(moodEventId)) {
                                moodEvent = event;
                                populateUI();
                                break;
                            }
                        }
                    },
                    null
            );
        }*/

        // Add button listeners
        binding.saveButton.setOnClickListener(v -> editMoodEvent());
        binding.deleteButton.setOnClickListener(v -> deleteMoodEvent());


    }


    //TODO add the button listeners
    private void populateUI() {
        if (moodEvent == null) return;

        // Set the emotional state text
        binding.moodinfoStateText.setText(moodEvent.getEmotionalState().getDescription());

        // Set the description
        binding.moodinfoDescriptionText.setText(moodEvent.getTrigger());

        // Set the trigger (if available)
        if (moodEvent.getTrigger() != null) {
            binding.moodinfoTriggerText.setText(moodEvent.getTrigger());
        } else {
            binding.moodinfoTriggerText.setText(""); // Clear if null
        }

        // Set the social situation (if available)
        if (moodEvent.getSocialSituation() != null) {
            binding.moodinfoSituationText.setText(moodEvent.getSocialSituation());
        } else {
            binding.moodinfoSituationText.setText(""); // Clear if null
        }


    }

    private void editMoodEvent() {
        if (moodEvent == null || moodList == null) return;

        // Get updated values from UI
        String newEmotion = binding.moodinfoStateText.getText().toString().trim();
        String newDescription = binding.moodinfoDescriptionText.getText().toString().trim();
        String newTrigger = binding.moodinfoTriggerText.getText().toString().trim();
        String newSocialSituation = binding.moodinfoSituationText.getText().toString().trim();


        // Prepare the updated data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("emotional_state", newEmotion.isEmpty() ? null : newEmotion);
        updatedData.put("trigger", newTrigger.isEmpty() ? null : newTrigger);
        updatedData.put("social_situation", newSocialSituation.isEmpty() ? null : newSocialSituation);
        updatedData.put("description", newDescription.isEmpty() ? null : newDescription);

        // Use MoodList's editMoodEvent function
        try {
            moodList.editMoodEvent(moodEvent, updatedData);
            Toast.makeText(getContext(), "Mood event updated!", Toast.LENGTH_SHORT).show();
            //TODO navigate back
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error updating mood event.", Toast.LENGTH_SHORT).show();
        }

    }
    private void deleteMoodEvent() {
        if (moodEvent == null || moodList == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Mood Event")
                .setMessage("Are you sure you want to delete this mood event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        moodList.deleteMoodEvent(moodEvent);
                        Toast.makeText(getContext(), "Mood event deleted", Toast.LENGTH_SHORT).show();
                         //TODO Navigate back
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error deleting mood event.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // recommended best practice to avoid memory leaks
    }
    


}
