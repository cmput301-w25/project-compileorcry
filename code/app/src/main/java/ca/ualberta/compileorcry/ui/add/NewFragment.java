package ca.ualberta.compileorcry.ui.add;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Objects;

import ca.ualberta.compileorcry.R;

public class NewFragment extends Fragment {

    private TextInputEditText emotionalStateEditText;
    private TextInputEditText dateEditText;
    private TextInputEditText reasonEditText;
    private TextInputEditText triggerEditText;
    private TextInputEditText socialSituationEditText;
    private MaterialButton uploadImageButton;
    private MaterialButton backButton;
    private MaterialButton createButton;

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
        emotionalStateEditText = view.findViewById(R.id.new_event_emotional_state_text);
        dateEditText = view.findViewById(R.id.new_event_date_text);
        reasonEditText = view.findViewById(R.id.new_event_reason_text);
        triggerEditText = view.findViewById(R.id.new_event_trigger_text);
        socialSituationEditText = view.findViewById(R.id.new_event_social_situation_text);
        uploadImageButton = view.findViewById(R.id.image_upload_button);
        backButton = view.findViewById(R.id.login_button);
        createButton = view.findViewById(R.id.register_button);

        // Handle date picker dialog
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Handle image upload (Placeholder action)
        uploadImageButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Image upload feature coming soon!", Toast.LENGTH_SHORT).show()
        );

        // Handle back button
        backButton.setOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // Handle create button (Validates input before submission)
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
        String emotionalState = emotionalStateEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String reason = reasonEditText.getText().toString().trim();
        String trigger = triggerEditText.getText().toString().trim();
        String socialSituation = socialSituationEditText.getText().toString().trim();

        if (emotionalState.isEmpty() || date.isEmpty() || reason.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement data submission (e.g., Firebase, SQLite)
        Toast.makeText(getContext(), "New event created!", Toast.LENGTH_SHORT).show();
    }
}
