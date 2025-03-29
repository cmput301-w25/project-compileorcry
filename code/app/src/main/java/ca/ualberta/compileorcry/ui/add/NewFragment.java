package ca.ualberta.compileorcry.ui.add;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ca.ualberta.compileorcry.BuildConfig;
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

    private static final long MAX_FILE_SIZE_BYTES = 65536;
    private MaterialSwitch visibilitySwitch;
    private AutoCompleteTextView emotionalStateAutoCompleteText;
    private TextInputEditText dateEditText;
    private TextInputEditText triggerEditText;
    private TextInputEditText locationEditText;
    private MaterialButton myLocationButton;
    private AutoCompleteTextView  socialSituationAutoCompleteText;
    private MaterialButton uploadImageButton;
    private TextView imagePathText;
    private MaterialButton backButton;
    private MaterialButton createButton;
    private TextInputLayout emotionalStateLayout;
    private TextInputLayout dateLayout;
    private Uri imagePath;
    private String uploadedImagePath;
    private GeoHash location;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Const for image upload
    private static final int PICK_IMAGE_REQUEST = 71;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;


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
        visibilitySwitch = view.findViewById(R.id.visibility_switch);
        emotionalStateAutoCompleteText = view.findViewById(R.id.new_event_emotional_state_autocomplete);
        dateEditText = view.findViewById(R.id.new_event_date_text);
        triggerEditText = view.findViewById(R.id.new_event_trigger_text);
        locationEditText = view.findViewById(R.id.new_event_location_text);
        myLocationButton = view.findViewById(R.id.my_location_button);
        socialSituationAutoCompleteText = view.findViewById(R.id.new_event_social_situation_autocomplete);
        uploadImageButton = view.findViewById(R.id.image_upload_button);
        imagePathText = view.findViewById(R.id.image_path_text);
        backButton = view.findViewById(R.id.back_button);
        createButton = view.findViewById(R.id.create_button);

        // Get AutoComplete references
        emotionalStateLayout = view.findViewById(R.id.new_event_emotional_state_layout);
        dateLayout = view.findViewById(R.id.new_event_date_layout);

        // Initialize emotional state dropdown
        setupEmotionalStateDropdown();

        // Initialize social situation dropdown
        setupSocialSituationDropdown();

        // Handle date picker dialog
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(requireContext(), BuildConfig.MAPS_API_KEY);
        }

        // Handle location autocomplete
        locationEditText.setOnClickListener(v -> {
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG))
                    .setLocationBias(RectangularBounds.newInstance(
                            new LatLng(50.7, -114.6),
                            new LatLng(53.8, -113.3)))
                    .setCountries(List.of("CA"))
                    .build(requireContext());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        myLocationButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.getFusedLocationProviderClient(requireActivity())
                        .getLastLocation()
                        .addOnSuccessListener(loc -> {
                            if (loc != null) {
                                locationEditText.setText(R.string.current_location);
                                this.location = new GeoHash(loc.getLatitude(), loc.getLongitude());
                            } else {
                                Toast.makeText(getContext(), "Couldn't get location", Toast.LENGTH_SHORT).show();
                                locationEditText.setText("");
                                locationEditText.clearFocus();
                            }
                        });
            } else {
                // Request permission if not granted
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            }
        });

        // Handle image upload (TODO)
        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

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

        Boolean isPublic = visibilitySwitch.isChecked();
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

        // TODO: Pass in visibility boolean during event creation. isPublic is already defined above.
        MoodEvent event = new MoodEvent(EmotionalState.valueOf(emotionalState.toUpperCase()),
                timestamp, trigger, socialSituation, uploadedImagePath, isPublic, location);

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

        // Clear the location text field
        locationEditText.setText("");
        locationEditText.clearFocus();

        // Clear the social situation dropdown
        socialSituationAutoCompleteText.setText("", false);
        socialSituationAutoCompleteText.clearFocus();

        // Clear the image path
        imagePathText.setText("");
        imagePathText.setVisibility(View.GONE);

        // Clear any error states if they exist
        emotionalStateLayout.setError(null);

        dateLayout.setError(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationEditText.setText(place.getAddress());
                if (place.getLatLng() != null) {
                    LatLng latLng = place.getLatLng();
                    location = new GeoHash(latLng.latitude, latLng.longitude);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(getContext(), "Invalid location.",
                        Toast.LENGTH_SHORT).show();
                location = null;
                locationEditText.setText("");
                locationEditText.clearFocus();
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imagePath = data.getData();

            // Check file size before proceeding
            if (isFileSizeValid(imagePath)) {
                imagePathText.setVisibility(View.VISIBLE);
                imagePathText.setText(imagePath.toString());
                uploadImage();
            } else {
                Toast.makeText(getContext(), "File size exceeds the limit of 64KB.",
                        Toast.LENGTH_LONG).show();
                imagePath = null;
                uploadedImagePath = null;
                imagePathText.setText("");
                imagePathText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted - trigger location fetch again
            myLocationButton.performClick();
        }
    }

    private void uploadImage() {
        if (imagePath != null) {
            // Create a reference to the file location in Firebase Storage
            String fileName = UUID.randomUUID().toString();
            StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child(User.getActiveUser().getUsername() + "/" + fileName);

            ref.putFile(imagePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();

                        // Save image path
                        uploadedImagePath = ref.getPath();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private boolean isFileSizeValid(Uri uri) {
        try {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                long fileSize = cursor.getLong(sizeIndex);
                cursor.close();

                Log.d("FileSize", "Selected file size: " + fileSize + " bytes");
                return fileSize <= MAX_FILE_SIZE_BYTES;
            }
            return true;
        } catch (Exception e) {
            Log.e("FileSize", "Error checking file size: " + e.getMessage());
            return true;
        }
    }
}
