package ca.ualberta.compileorcry.ui.add;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;

public class NewFragment extends Fragment {

    private AutoCompleteTextView emotionalStateAutoCompleteText;
    private TextInputEditText dateEditText;
    private TextInputEditText triggerEditText;
    private AutoCompleteTextView  socialSituationAutoCompleteText;
    private MaterialButton uploadImageButton;
    private MaterialButton backButton;
    private MaterialButton createButton;

    TextInputLayout emotionalStateLayout;

    TextInputLayout dateLayout;

    public NewFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        emotionalStateAutoCompleteText = view.findViewById(R.id.new_event_emotional_state_autocomplete);
        dateEditText = view.findViewById(R.id.new_event_date_text);
        triggerEditText = view.findViewById(R.id.new_event_trigger_text);
        socialSituationAutoCompleteText = view.findViewById(R.id.new_event_social_situation_text);
        uploadImageButton = view.findViewById(R.id.image_upload_button);
        backButton = view.findViewById(R.id.login_button);
        createButton = view.findViewById(R.id.register_button);

        // Get AutoComplete references
        emotionalStateLayout = getView().findViewById(R.id.new_event_emotional_state_layout);
        dateLayout = getView().findViewById(R.id.new_event_date_layout);

        // Initialize emotional state dropdown
        setupEmotionalStateDropdown();

        // Initialize social situation dropdown
        setupSocialSituationDropdown();

        // Handle date picker dialog
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Handle image upload (TODO)
        uploadImageButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Image upload feature coming soon!", Toast.LENGTH_SHORT).show()
        );

        // Handle back button
        backButton.setOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // Handle create button (w/ validation)
        createButton.setOnClickListener(v -> submitNewEvent());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    dateEditText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void submitNewEvent() {
        boolean isValid = true;

        String emotionalState = emotionalStateAutoCompleteText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        // Validate Emotional State
        if (emotionalState.isEmpty()) {
            emotionalStateLayout.setError("This field is required");
            isValid = false;
        } else {
            emotionalStateLayout.setError(null);
        }

        // Validate Date
        if (date.isEmpty()) {
            dateLayout.setError("Please select a date");
            isValid = false;
        } else {
            dateLayout.setError(null);
        }

        if (!isValid) {
            return;
        }

        // TODO: Implement data submission (e.g., Firebase, SQLite)
        Toast.makeText(getContext(), "New event created!", Toast.LENGTH_SHORT).show();
    }

    private void setupEmotionalStateDropdown() {
        // Get emotional states from arrays resource
        String[] emotionalStates = getResources().getStringArray(R.array.emotional_states);

        // Create the adapter using the resource array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                emotionalStates
        );

        // Set the adapter to the AutoCompleteTextView
        emotionalStateAutoCompleteText.setAdapter(adapter);

        // Handle selection
        emotionalStateAutoCompleteText.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = (String) parent.getItemAtPosition(position);
            // Do something with the selected state
        });
    }

    private void setupSocialSituationDropdown() {
        // Get social situations from arrays resource
        String[] socialSituations = getResources().getStringArray(R.array.social_situations);

        // Create the adapter using the resource array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                socialSituations
        );

        // Set the adapter to the AutoCompleteTextView
        socialSituationAutoCompleteText.setAdapter(adapter);

        // Handle selection
        socialSituationAutoCompleteText.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = (String) parent.getItemAtPosition(position);
            // Do something with the selected state
        });
    }
}
