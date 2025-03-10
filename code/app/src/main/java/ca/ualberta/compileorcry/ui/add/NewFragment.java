package ca.ualberta.compileorcry.ui.add;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

/**
 * The NewFragment class provides the UI for creating new mood events.
 * It implements a form-based interface for users to input mood event details
 * including emotional state, date, description, trigger, and social situation.
 *
 * Key features:
 * - Form validation for required fields
 * - Date picker dialog for selecting event dates
 * - Support for future image upload functionality
 *
 */
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

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Default constructor required for fragments.
     */
    public NewFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container If non-null, this is the parent view that the fragment's UI should be
     *                 attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                           previous saved state
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new, container, false);
    }

    /**
     * Initialize UI components and set up event listeners after the view is created.
     * This method handles all the initialization of UI elements, dropdowns,
     * and button click listeners.
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                           previous saved state
     */
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

    /**
     * Displays a date picker dialog to allow the user to select a date for the mood event.
     * The selected date is displayed in the date input field.
     */
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String formattedMonth = (selectedMonth + 1) < 10 ? "0" + (selectedMonth + 1) : String.valueOf(selectedMonth + 1);
                    String formattedDay = selectedDay < 10 ? "0" + selectedDay : String.valueOf(selectedDay);
                    String selectedDate = selectedYear + "-" + formattedMonth + "-" + formattedDay;
                    dateEditText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Validates form inputs and submits the new mood event data if all required fields are valid.
     * Shows appropriate error messages for invalid or missing data.
     *
     * Note: The actual submission to the database is not yet implemented.
     */
    private void submitNewEvent() {
        boolean isValid = true;

        String emotionalState = emotionalStateAutoCompleteText.getText().toString().trim();

        // Parse date
        Date date = null;
        try {
            date = dateFormat.parse(dateEditText.getText().toString().trim());
            dateLayout.setError(null);
        } catch (ParseException e) {
            dateLayout.setError("Please enter a valid date");
            isValid = false;
        }

        String trigger = triggerEditText.getText().toString().trim();
        String socialSituation = socialSituationAutoCompleteText.getText().toString().trim();

        // Validate Emotional State
        if (emotionalState.isEmpty()) {
            emotionalStateLayout.setError("This field is required");
            isValid = false;
        } else {
            emotionalStateLayout.setError(null);
        }

        if (!isValid) {
            return;
        }

        Timestamp timestamp = new Timestamp(date);
        MoodEvent event = new MoodEvent(EmotionalState.valueOf(emotionalState.toUpperCase()),
                timestamp, trigger, socialSituation);

        MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE,
                new MoodList.MoodListListener() {
                    @Override
                    public void returnMoodList(MoodList moodList) {
                        moodList.addMoodEvent(event);
                        clear();
                        Toast.makeText(getContext(), "Mood event created successfully!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void updatedMoodList() {
                        // Handled automatically
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("DataList",e.getMessage());
                    }
                }, null);
    }

    /**
     * Sets up the emotional state dropdown with values from the string array resource.
     * This creates and attaches an adapter to display all possible emotional states
     * in a dropdown menu format.
     */
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
    }

    /**
     * Sets up the social situation dropdown with values from the string array resource.
     * This creates and attaches an adapter to display all possible social situations
     * in a dropdown menu format.
     */
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
    }

    /**
     * Clears all form fields and error states.
     * This method is called after successfully submitting a mood event
     * to prepare the form for a new entry.
     */
    public void clear() {
        // Clear the emotional state dropdown
        emotionalStateAutoCompleteText.setText("", false);
        emotionalStateAutoCompleteText.clearFocus();

        // Clear the date/time field
        dateEditText.setText("");
        dateEditText.clearFocus();

        // Clear the trigger text field
        triggerEditText.setText("");
        triggerEditText.clearFocus();

        // Clear the social situation dropdown
        socialSituationAutoCompleteText.setText("", false);
        socialSituationAutoCompleteText.clearFocus();

        // TODO: Clear image text

        // Clear any error states if they exist
        emotionalStateLayout.setError(null);

        dateLayout.setError(null);
    }
}
