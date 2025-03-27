package ca.ualberta.compileorcry.ui.feed;

import static androidx.navigation.Navigation.findNavController;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentFeedBinding;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

/**
 * The FeedFragment class displays a feed of mood events, either from the user's
 * history or from followed users. It implements filtering capabilities based on
 * recency, emotional state, and reason.
 *
 * The fragment handles:
 * - RecyclerView setup for displaying mood events
 * - Spinner controls for selecting feed type and filters
 * - Navigation to create new mood events
 * - Querying mood events based on selected filters
 *
 * Outstanding issues:
 * - Some filter combinations may not be properly handled
 * - Error handling could be improved for empty result sets
 * - UI feedback during data loading could be enhanced
 */
public class FeedFragment extends Fragment {
    private FragmentFeedBinding binding;
    private FeedViewModel feedViewModel;
    private MoodEventAdapter adapter;
    // Feed type options
    private static final String[] FEED_TYPES = {"Following", "History"};
    // Filter options
    private static final String[] FILTER_OPTIONS = {"None", "Recent", "State", "Reason", "Nearby"};


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getParentFragmentManager().setFragmentResultListener("moodEventUpdated", this, (requestKey, result) -> {
            Log.d("FeedFragment", "Mood event was updated, reloading feed...");
            adapter.notifyDataSetChanged();
            loadFeed();

        });
        super.onViewCreated(view, savedInstanceState);
        setupUI(view);
        setupViewModel();
    }

    private void setupUI(View view) {
        // Initalize feed spinner
        ArrayAdapter<String> feedAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.custom_spinner, FEED_TYPES);
        feedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.feedSpinner.setAdapter(feedAdapter);
        binding.feedSpinner.post(() -> binding.feedSpinner.setSelection(0, false));

        // Initialize filter spinner
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.custom_spinner, FILTER_OPTIONS);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.filterSpinner.setAdapter(filterAdapter);
        binding.filterSpinner.post(() -> binding.filterSpinner.setSelection(0, false));

        // Initialize RecyclerView
        adapter = new MoodEventAdapter(new ArrayList<>(), this::onMoodEventClick);
        binding.recyclerViewMoodHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewMoodHistory.setAdapter(adapter);

        // Setup map FAB
        binding.fabMap.setOnClickListener(v -> navigateToMap());

        // Setup spinner listeners
        binding.feedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.imageView.setImageResource(
                        parent.getItemAtPosition(position).toString().equals("History")
                                ? R.drawable.txt_history
                                : R.drawable.txt_feed
                );
                loadFeed();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFeed();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupViewModel() {
        feedViewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);
        feedViewModel.getMoodEvents().observe(getViewLifecycleOwner(), moodEvents -> {
            Log.d("RecyclerView", "Updating RecyclerView with " + moodEvents.size() + " moods.");
            boolean isFollowing = binding.feedSpinner.getSelectedItem().equals("Following");
            adapter.updateData(moodEvents, isFollowing);
            binding.recyclerViewMoodHistory.smoothScrollToPosition(0);
        });
    }

    private void onMoodEventClick(MoodEvent clickedEvent) {
        if (clickedEvent == null) {
            Log.e("FeedFragment", "Clicked MoodEvent is null!");
            return;
        }
        
        Log.d("FeedFragment", "Mood Event Clicked: " + clickedEvent.getId());
        
        Bundle args = new Bundle();
        args.putString("moodId", clickedEvent.getId());
        args.putString("emotionalState", clickedEvent.getEmotionalState().getDescription());
        args.putString("trigger", clickedEvent.getTrigger());
        args.putString("socialSituation", clickedEvent.getSocialSituation());
        args.putString("imagePath", clickedEvent.getPicture());

        MoodInfoDialogFragment dialog = new MoodInfoDialogFragment();
        dialog.setArguments(args);
        dialog.show(requireActivity().getSupportFragmentManager(), "ViewMoodEvent");
    }

    /**
     * Loads mood events based on selected feed type and filter options.
     * Creates a MoodList with the appropriate QueryType and handles the response
     * through the MoodListListener.
     *
     * The method determines the correct QueryType based on combinations of:
     * - Feed type (History or Following)
     * - Filter type (None, Recent, State, or Reason)
     */
    private void loadFeed() {
        if (User.getActiveUser() == null) return;

        String feedType = (String) binding.feedSpinner.getSelectedItem();
        String filter = (String) binding.filterSpinner.getSelectedItem();
        boolean isFollowing = feedType.equals("Following");

        switch (filter) {
            case "None":
                fetchMoodEvents(isFollowing ? QueryType.FOLLOWING : QueryType.HISTORY_MODIFIABLE, null);
                break;
            case "Recent":
                fetchMoodEvents(isFollowing ? QueryType.FOLLOWING_RECENT : QueryType.HISTORY_RECENT, null);
                break;
            case "Nearby":
                getCurrentLocation(geoHash -> {
                    if (geoHash != null) {
                        fetchMoodEvents(isFollowing ? QueryType.MAP_CLOSE : QueryType.MAP_PERSONAL_CLOSE, geoHash);
                    } else {
                        Toast.makeText(requireContext(), "Location unavailable", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case "State":
                showEmotionalStateDialog(isFollowing);
                break;
            case "Reason":
                showReasonInputDialog(isFollowing);
                break;
            default:
                fetchMoodEvents(isFollowing ? QueryType.FOLLOWING : QueryType.HISTORY_MODIFIABLE, null);
        }
    }


    private void fetchMoodEvents(QueryType queryType, Object filterValue) {
        MoodList.createMoodList(User.getActiveUser(), queryType, new MoodList.MoodListListener() {
            @Override
            public void returnMoodList(MoodList moodList) {
                moodList.clearMoodEvents();
                feedViewModel.setMoodEvents(moodList.getMoodEvents());
            }

            @Override
            public void onError(Exception e) {
                Log.e("DataList", e.getMessage());
            }

            @Override
            public void updatedMoodList() {
                // Handled automatically
            }
        }, filterValue);
    }

    private void showReasonInputDialog(boolean isFollowing) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(requireContext())
                .setTitle("Enter Reason to Filter")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String reasonKeyword = input.getText().toString().trim();
                    if (!reasonKeyword.isEmpty()) {
                        fetchMoodEvents(
                                isFollowing ? QueryType.FOLLOWING_REASON : QueryType.HISTORY_REASON,
                                reasonKeyword
                        );
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showEmotionalStateDialog(boolean isFollowing) {
        EmotionalState[] states = EmotionalState.values();
        String[] stateNames = new String[states.length];
        for (int i = 0; i < states.length; i++) {
            stateNames[i] = states[i].getDescription();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Emotional State")
                .setItems(stateNames, (dialog, which) -> {
                    fetchMoodEvents(
                            isFollowing ? QueryType.FOLLOWING_STATE : QueryType.HISTORY_STATE,
                            states[which]
                    );
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void applySelectedFilter(QueryType queryType, Object filterValue) {
        Log.d("FeedFragment", "Applying filter: " + queryType + " | " + filterValue);

        MoodList.createMoodList(User.getActiveUser(), queryType, new MoodList.MoodListListener() {
            @Override
            public void returnMoodList(MoodList initializedMoodList) {
                Log.d("FeedFragment", "returnMoodList() called");
                if (feedViewModel != null) {
                    feedViewModel.setMoodEvents(initializedMoodList.getMoodEvents());
                }
            }

            @Override
            public void updatedMoodList() {
                Log.d("FeedFragment", "updatedMoodList() called");
            }

            @Override
            public void onError(Exception e) {
                Log.e("DataList",e.getMessage());
            }
        }, filterValue);
    }

    private void getCurrentLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            callback.onLocationReceived(null);
            return;
        }

        LocationServices.getFusedLocationProviderClient(requireActivity())
                .getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        callback.onLocationReceived(new GeoHash(loc.getLatitude(), loc.getLongitude()));
                    } else {
                        Toast.makeText(getContext(), "Couldn't get location", Toast.LENGTH_SHORT).show();
                        callback.onLocationReceived(null);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadFeed();
        }
    }

    interface LocationCallback {
        void onLocationReceived(GeoHash geoHash);
    }

    private void navigateToMap() {
        List<MoodEvent> moodEvents = feedViewModel.getMoodEvents().getValue();
        if (moodEvents == null || moodEvents.isEmpty()) {
            Toast.makeText(requireContext(), "No locatable events", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<MoodEvent> moodEventsWithGeoHash = new ArrayList<>();
        for (MoodEvent mood : moodEvents) {
            if (mood.getLocation() != null) {
                moodEventsWithGeoHash.add(mood);
            }
        }

        if (!moodEventsWithGeoHash.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("moodEvents", moodEventsWithGeoHash);
            findNavController(requireView()).navigate(R.id.navigation_map, bundle);
        } else {
            Toast.makeText(requireContext(), "No locatable events", Toast.LENGTH_SHORT).show();
        }
    }
}