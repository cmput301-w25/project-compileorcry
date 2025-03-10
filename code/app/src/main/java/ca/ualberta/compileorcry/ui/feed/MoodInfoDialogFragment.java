package ca.ualberta.compileorcry.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ca.ualberta.compileorcry.databinding.FragmentMoodInfoDialogBinding;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;



import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodInfoDialogFragment extends DialogFragment {

    public interface MoodDialogListener {
        void updateMoodEvent(MoodEvent event, String emotionalState, String trigger, String socialSituation);
        void deleteMoodEvent(MoodEvent event);
    }

    private MoodDialogListener listener;
    private MoodEvent moodEvent;
    private EditText editMoodState, editDescription, editTrigger, editSocialSituation;

    public static MoodInfoDialogFragment newInstance(MoodEvent event) {
        Bundle args = new Bundle();
        args.putString("moodId", event.getId());
        args.putString("emotionalState", event.getEmotionalState().getDescription());
        args.putString("description", event.getTrigger());
        args.putString("trigger", event.getTrigger());
        args.putString("socialSituation", event.getSocialSituation());

        MoodInfoDialogFragment fragment = new MoodInfoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (getParentFragment() instanceof MoodDialogListener) {
                listener = (MoodDialogListener) getParentFragment();
            } else if (context instanceof MoodDialogListener) {
                listener = (MoodDialogListener) context;
            } else {
                throw new RuntimeException("Implement MoodDialogListener in your activity or fragment.");
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MoodDialogListener");
        }
    }
    private FragmentMoodInfoDialogBinding binding;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = FragmentMoodInfoDialogBinding.inflate(getLayoutInflater());

        // Retrieve the mood event passed to the dialog
        if (getArguments() != null) {
            moodEvent = (MoodEvent) getArguments().getSerializable("MoodEvent");
            if (moodEvent != null) {
                binding.moodinfoStateText.setText(moodEvent.getEmotionalState().getDescription());
                binding.moodinfoTriggerText.setText(moodEvent.getTrigger());
                binding.moodinfoSituationText.setText(moodEvent.getSocialSituation());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(binding.getRoot())
                .setTitle("Edit Mood Event")
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete", (dialog, which) -> {
                    if (listener != null && moodEvent != null) {
                        listener.deleteMoodEvent(moodEvent);
                    }
                })
                .setPositiveButton("Save", (dialog, which) -> {
                    String emotionalState = binding.moodinfoStateText.getText().toString();
                    String trigger = binding.moodinfoTriggerText.getText().toString();
                    String socialSituation = binding.moodinfoSituationText.getText().toString();

                    // Modify the existing MoodEvent directly
                    moodEvent.setEmotionalState(EmotionalState.fromDescription(emotionalState));

                    moodEvent.setTrigger(trigger);
                    moodEvent.setSocialSituation(socialSituation);

                    if (listener != null && moodEvent != null) {
                        listener.updateMoodEvent(moodEvent, emotionalState, trigger, socialSituation);
                    }
                })
                .create();
    }
}
