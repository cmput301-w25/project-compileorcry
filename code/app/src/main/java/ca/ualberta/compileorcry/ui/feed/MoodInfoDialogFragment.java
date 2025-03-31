package ca.ualberta.compileorcry.ui.feed;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

        binding = FragmentMoodInfoDialogBinding.inflate(getLayoutInflater());
        binding.moodinfoStateAutoComplete.setDropDownBackgroundResource(R.color.dark);
        binding.moodinfoSituationAutoComplete.setDropDownBackgroundResource(R.color.dark);

        Bundle args = getArguments();
        if (args != null) {
            Log.d("MoodInfoDialogFragment", "Arguments: " + args);
            String moodId = args.getString("moodId", "Unknown");
            String emotionalState = args.getString("emotionalState", "Unknown");
            EmotionalState state = EmotionalState.fromDescription(emotionalState);
            this.moodEvent = (MoodEvent) args.getSerializable("moodEvent");
            int backgroundColor = state.getColor(requireContext());
            String trigger = args.getString("trigger", "No Trigger");
            String socialSituation = args.getString("socialSituation", "No Situation");
            String feedType = args.getString("feedType");
            // Handle image loading
            String imagePath = args.getString("imagePath");
            if (imagePath != null && !imagePath.isEmpty()) {
                loadImage(imagePath);
            } else {
                // Hide image view if no image
                binding.moodInfoImageView.setVisibility(View.GONE);
            }

            // Populate the UI with initial values
            setupEmotionalStateDropdown(emotionalState);
            setupSocialSituationDropdown(socialSituation);
            binding.moodinfoTriggerText.setText(trigger);
            binding.getRoot().setBackgroundColor(backgroundColor);
            binding.saveButton.setTextColor(backgroundColor);
            binding.buttonViewComments.setTextColor(backgroundColor);
            if (!feedType.equals("History")) {
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

                ConstraintLayout layout = binding.getRoot();
                ConstraintSet set = new ConstraintSet();
                set.clone(layout);

                int marginInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        getResources().getDisplayMetrics()
                );

                // Remove old top constraint (if any)
                set.clear(R.id.mood_info_image_view, ConstraintSet.TOP);

                // Apply new constraint: position below moodinfo_situation_readonly_layout
                set.connect(
                        R.id.mood_info_image_view,
                        ConstraintSet.TOP,
                        R.id.moodinfo_situation_readonly_layout,
                        ConstraintSet.BOTTOM,
                        marginInPx
                );

                // Apply the changes to the layout
                set.applyTo(layout);

            }
            if(!moodEvent.getIsPublic()){
                binding.buttonViewComments.setVisibility(View.GONE);
            }
        }

        // view comments button logic
        binding.buttonViewComments.setOnClickListener(v -> {
            Log.d("MoodInfoFragment", "viewcomments button clicked");
            if (moodEvent != null) {
                CommentFragment commentFragment = new CommentFragment();
                Bundle bundle = new Bundle();
                bundle.putString("moodEventId", moodEvent.getId());
                bundle.putSerializable("moodEvent",moodEvent);
                commentFragment.setArguments(bundle);

                NavController navController = NavHostFragment.findNavController(
                        requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main)
                );
                dismiss();
                navController.navigate(R.id.commentFragment, bundle);
            }
        });

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

    private void loadImage(String imagePath) {
        StorageReference imageRef = FirebaseStorage.getInstance().getReference(imagePath);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // Use Glide to load the image
            Glide.with(requireContext())
                    .load(uri)
                    .centerCrop()
                    .error(R.drawable.ic_broken_image_80dp)
                    .into(binding.moodInfoImageView);

            // Make image visible
            binding.moodInfoImageView.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {
            binding.moodInfoImageView.setVisibility(View.GONE);
            Log.e("MoodInfoDialogFragment", "Error loading image", e);
        });
    }
}
