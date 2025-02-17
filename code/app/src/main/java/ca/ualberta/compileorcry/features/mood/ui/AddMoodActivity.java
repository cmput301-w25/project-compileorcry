package ca.ualberta.compileorcry.features.mood.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ca.ualberta.compileorcry.R; // Adjust this import based on your package structure
import ca.ualberta.compileorcry.features.mood.data.MoodRepository;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class AddMoodActivity extends AppCompatActivity {

    private Spinner spinnerEmotionalState;
    private EditText editTextTrigger;
    private Spinner spinnerSocialSituation;
    private Button buttonSaveMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mood);  // Create this layout in res/layout

        // Bind views
        spinnerEmotionalState = findViewById(R.id.spinner_emotional_state);
        editTextTrigger = findViewById(R.id.edittext_trigger);
        spinnerSocialSituation = findViewById(R.id.spinner_social_situation);
        buttonSaveMood = findViewById(R.id.button_save_mood);

        buttonSaveMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve selected emotional state
                String emotionalStateStr = spinnerEmotionalState.getSelectedItem().toString();
                if (emotionalStateStr == null || emotionalStateStr.isEmpty() || "Select".equals(emotionalStateStr)) {
                    Toast.makeText(AddMoodActivity.this, "Please select an emotional state", Toast.LENGTH_SHORT).show();
                    return;
                }

                EmotionalState state;
                try {
                    state = EmotionalState.valueOf(emotionalStateStr.toUpperCase());
                } catch (Exception e) {
                    Toast.makeText(AddMoodActivity.this, "Invalid emotional state", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get optional trigger and social situation
                String trigger = editTextTrigger.getText().toString().trim();
                String socialSituation = spinnerSocialSituation.getSelectedItem().toString();
                if ("Select".equals(socialSituation)) {
                    socialSituation = null;
                }

                // Create the mood event
                MoodEvent moodEvent = new MoodEvent(state, trigger, socialSituation);

                // Add the event to the repository
                MoodRepository.getInstance().addMoodEvent(moodEvent);

                // Confirmation feedback
                Toast.makeText(AddMoodActivity.this, "Mood event added", Toast.LENGTH_SHORT).show();

                // Close the activity (return to history)
                finish();
            }
        });
    }
}