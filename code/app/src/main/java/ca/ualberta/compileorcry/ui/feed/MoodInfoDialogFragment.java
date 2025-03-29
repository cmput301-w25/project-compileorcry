package ca.ualberta.compileorcry.ui.feed;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.HashMap;
import java.util.Map;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentMoodInfoDialogBinding;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

/**
 * A DialogFragment that allows the user to view, edit, and delete a {@link MoodEvent}.
 * Now includes dropdowns for emotional state and social situation.
 */
public class MoodInfoDialogFragment extends DialogFragment {

    private MoodEvent moodEvent;
    private FragmentMoodInfoDialogBinding binding;

    /**
     * Notifies the parent fragment that a mood event was updated or deleted,
     * then dismisses this dialog.
     */
    private void notifyParentAndDismiss() {
        if (isAdded()) {
            getParentFragmentManager().setFragmentResult("moodEventUpdated", new Bundle());
            Log.d("MoodInfoDialogFragment", "Sending result to parent before dismiss");

        }
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String currentUser = User.getActiveUser().getUsername();
        String moodOwner = "";
        String feedType = null;

        binding = FragmentMoodInfoDialogBinding.inflate(getLayoutInflater());
        binding.moodinfoStateAutoComplete.setDropDownBackgroundResource(R.color.dark);
        binding.moodinfoSituationAutoComplete.setDropDownBackgroundResource(R.color.dark);

        Bundle args = getArguments();


        if (args != null) {
            Log.d("MoodInfoDialogFragment", "Arguments: " + args);
            String moodId = args.getString("moodId", "Unknown");
            String emotionalState = args.getString("emotionalState", "Unknown");
            EmotionalState state = EmotionalState.fromDescription(emotionalState);
            int backgroundColor = state.getColor(requireContext());
            String trigger = args.getString("trigger", "No Trigger");
            String socialSituation = args.getString("socialSituation", "No Situation");
            feedType = args.getString("feedType", "Feed");


            // Populate the UI with initial values
            setupEmotionalStateDropdown(emotionalState);
            setupSocialSituationDropdown(socialSituation);
            binding.moodinfoTriggerText.setText(trigger);
            binding.getRoot().setBackgroundColor(backgroundColor);
            if (!"History".equals(feedType)) {
                // Hide editable components
                binding.moodinfoStateLayout.setVisibility(View.GONE);
                binding.moodinfoTriggerLayout.setVisibility(View.GONE);
                binding.moodinfoSituationLayout.setVisibility(View.GONE);
                binding.buttonBar.setVisibility(View.GONE);
                binding.moodinfoEditText.setText("View Mood Event");

                // Show read-only layouts
                binding.moodinfoStateReadonlyLayout.setVisibility(View.VISIBLE);
                binding.moodinfoTriggerReadonlyLayout.setVisibility(View.VISIBLE);
                binding.moodinfoSituationReadonlyLayout.setVisibility(View.VISIBLE);

                // Set text values
                binding.moodinfoStateText.setText(emotionalState);
                binding.moodinfoTriggerDisplay.setText(trigger);
                binding.moodinfoSituationText.setText(socialSituation);
            }
            moodEvent = new MoodEvent(moodId);
        }



        // Save button logic
        binding.saveButton.setOnClickListener(v -> {
            if (moodEvent != null) {
                Map<String, Object> changes = new HashMap<>();
                changes.put("emotional_state", EmotionalState.fromDescription(binding.moodinfoStateAutoComplete.getText().toString()));
                changes.put("trigger", binding.moodinfoTriggerText.getText().toString());
                changes.put("social_situation", binding.moodinfoSituationAutoComplete.getText().toString());

                MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE, new MoodList.MoodListListener() {
                    @Override
                    public void returnMoodList(MoodList moodList) {
                        if (moodList.containsMoodEvent(moodEvent)) {
                            moodList.editMoodEvent(moodEvent, changes);

                            notifyParentAndDismiss();
                        } else {

                            notifyParentAndDismiss();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("MoodInfoDialogFragment", "Mood event with ID " + moodEvent.getId() + " not found in MoodList. Cannot edit.");
                    }

                    @Override
                    public void updatedMoodList() {

                    }
                }, null);
            }
        });

        // Delete button logic
        binding.deleteButton.setOnClickListener(v -> {
            if (moodEvent != null) {
                MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE, new MoodList.MoodListListener() {
                    @Override
                    public void returnMoodList(MoodList moodList) {
                        moodList.deleteMoodEvent(moodEvent);
                        notifyParentAndDismiss();
                        Toast.makeText(requireContext(), "Mood deleted successfully", Toast.LENGTH_SHORT).show();


                    }

                    @Override
                    public void onError(Exception e) {
                    }

                    @Override
                    public void updatedMoodList() {

                    }
                }, null);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .create();

        // Rounded corners support
        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.rounded_dialog)
            );
        });

        return dialog;
    }

    /**
     * Sets up the dropdown for emotional state with preselected value if provided.
     */
    private void setupEmotionalStateDropdown(String selected) {
        String[] emotionalStates = getResources().getStringArray(R.array.emotional_states);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                emotionalStates
        );
        binding.moodinfoStateAutoComplete.setAdapter(adapter);
        binding.moodinfoStateAutoComplete.setText(selected, false);
    }

    /**
     * Sets up the dropdown for social situation with preselected value if provided.
     */
    private void setupSocialSituationDropdown(String selected) {
        String[] socialSituations = getResources().getStringArray(R.array.social_situations);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                socialSituations
        );
        binding.moodinfoSituationAutoComplete.setAdapter(adapter);
        binding.moodinfoSituationAutoComplete.setText(selected, false);
    }
}
