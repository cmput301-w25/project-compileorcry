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

import ca.ualberta.compileorcry.databinding.FragmentMoodInfoDialogBinding;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;



import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodInfoDialogFragment extends DialogFragment {




    private MoodEvent moodEvent;




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
        }


        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setTitle("Mood Event Details")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create();
    }

}
