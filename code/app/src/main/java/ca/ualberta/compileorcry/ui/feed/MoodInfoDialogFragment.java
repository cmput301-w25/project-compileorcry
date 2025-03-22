package ca.ualberta.compileorcry.ui.feed;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.HashMap;
import java.util.Map;

import ca.ualberta.compileorcry.databinding.FragmentMoodInfoDialogBinding;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;



import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodInfoDialogFragment extends DialogFragment {




    private MoodEvent moodEvent;


    private void notifyParentAndDismiss() {
        getParentFragmentManager().setFragmentResult("moodEventUpdated", new Bundle());
        dismiss();
    }

    private FragmentMoodInfoDialogBinding binding;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentMoodInfoDialogBinding.inflate(getLayoutInflater());

        Bundle args = getArguments();
        if (args != null) {
            String moodId = args.getString("moodId", "Unknown");

            String emotionalState = args.getString("emotionalState", "Unknown");
            String trigger = args.getString("trigger", "No Trigger");
            String socialSituation = args.getString("socialSituation", "No Situation");

            // Populate the UI
            binding.moodinfoStateText.setText(emotionalState);
            binding.moodinfoTriggerText.setText(trigger);
            binding.moodinfoSituationText.setText(socialSituation);

            moodEvent = new MoodEvent(moodId);
        }



        // Save Button (Edit existing Mood Event)
        binding.saveButton.setOnClickListener(v -> {
            if (moodEvent != null) {
                Map<String, Object> changes = new HashMap<>();
                changes.put("emotional_state", EmotionalState.fromDescription(binding.moodinfoStateText.getText().toString()));
                changes.put("trigger", binding.moodinfoTriggerText.getText().toString());
                changes.put("social_situation", binding.moodinfoSituationText.getText().toString());

                MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE, new MoodList.MoodListListener() {
                    @Override
                    public void returnMoodList(MoodList moodList) {
                        if (moodList.getMoodEvents().contains(moodEvent)) {
                            moodList.editMoodEvent(moodEvent, changes);
                            notifyParentAndDismiss();
                        } else {
                            Log.e("MoodInfoDialogFragment", "Mood event with ID " + moodEvent.getId() + " not found in MoodList. Cannot edit.");
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    @Override
                    public void updatedMoodList() {}
                }, null);
            }
        });

// Delete Button Logic
        binding.deleteButton.setOnClickListener(v -> {
            if (moodEvent != null) {
                MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE, new MoodList.MoodListListener() {
                    @Override
                    public void returnMoodList(MoodList moodList) {
                        moodList.deleteMoodEvent(moodEvent);
                        notifyParentAndDismiss();
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    @Override
                    public void updatedMoodList() {}
                }, null);
            }
        });

// Notify FeedFragment and dismiss dialog


        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setTitle("Mood Event Details")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create();
    }

}
