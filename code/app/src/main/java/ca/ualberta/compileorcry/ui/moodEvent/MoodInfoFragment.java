package ca.ualberta.compileorcry.ui.moodEvent;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentMoodInfoBinding;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;
import ca.ualberta.compileorcry.ui.feed.CommentFragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class MoodInfoFragment extends Fragment {

    private FragmentMoodInfoBinding binding;
    private MoodEvent moodEvent;
    private MoodList moodList;
    private String moodEventId;

    public MoodInfoFragment() {}

    public static MoodInfoFragment newInstance(String moodEventId) {
        MoodInfoFragment fragment = new MoodInfoFragment();
        Bundle args = new Bundle();
        args.putString("moodEventId", moodEventId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
            Log.d("MoodInfoFragment", "Received moodEventId: " + moodEventId);
        }
    }

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


        // Retrieve moodEventId from bundle
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
        }

        // Add button listeners
        binding.saveButton.setOnClickListener(v -> editMoodEvent());
        binding.deleteButton.setOnClickListener(v -> deleteMoodEvent());
        binding.buttonViewComments.setOnClickListener(v -> viewComments());
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

    private void viewComments() {
        Log.d("MoodInfoFragment", "viewcomments button clicked");
        if (moodEventId != null) {
            Log.d("MoodInfoFragment", "MoodEventID not null");
            Bundle bundle = new Bundle();
            bundle.putString("moodEventId", moodEventId);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.navigation_feed);
            navController.navigate(R.id.commentFragment, bundle);

        } else {
            Toast.makeText(getContext(), "Error: Mood event ID is missing.", Toast.LENGTH_SHORT).show();
        }
    }
}
