package ca.ualberta.compileorcry.ui.feed;

import static androidx.navigation.Navigation.findNavController;

import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

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
    private ImageView feedOrHistory;
    private FeedViewModel feedViewModel;
    private MoodEventAdapter adapter;
    private Spinner filterSpinner; // filter by recency, state, reason, etc
    private Spinner feedSpinner; // feed type personal history, following, or map
    // Feed type options
    private static final String[] FEED_TYPES = {"Feed...","History", "Following"};
    // Filter options
    private static final String[] FILTER_OPTIONS = {"Filter...", "Recent", "State", "Reason"};


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView feedOrHistory = binding.imageView;

        // Find Spinners
        Spinner feedSpinner = binding.feedSpinner;
        Spinner filterSpinner = binding.filterSpinner;

        // Create Adapters using Java Array
        ArrayAdapter<String> feedAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.custom_spinner,
                FEED_TYPES
        );
        feedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedSpinner.setAdapter(feedAdapter);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.custom_spinner,
                FILTER_OPTIONS
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        // Prevent onItemSelected from triggering when setting default selection
        feedSpinner.post(() -> feedSpinner.setSelection(0, false));
        filterSpinner.post(() -> filterSpinner.setSelection(0, false));

        // Initialize RecyclerView with empty list
        adapter = new MoodEventAdapter(new ArrayList<>(), this::onMoodEventClick);
        binding.recyclerViewMoodHistory.setLayoutManager(
                new LinearLayoutManager(requireContext())  // Use requireContext()
        );
        binding.recyclerViewMoodHistory.setAdapter(adapter);

        // Setup map FAB
        binding.fabMap.setOnClickListener(v -> {
            findNavController(view).navigate(R.id.navigation_map);
        });

        // Initialize ViewModel
        feedViewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);

        // Observe LiveData with lifecycle awareness
        feedViewModel.getMoodEvents().observe(getViewLifecycleOwner(), moodEvents -> {
            Log.d("RecyclerView", "Observer triggered, updating RecyclerView with " + moodEvents.size() + " moods.");
            if (moodEvents != null && adapter != null) {
                adapter.updateData(moodEvents);
                binding.recyclerViewMoodHistory.smoothScrollToPosition(0);
            }
        });

        // Handle feed queries from spinner values and update feed as they change
        feedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return; // Ignore placeholder selection
                feedOrHistory.setImageResource(R.drawable.txt_feed);
                String selectedType = parent.getItemAtPosition(position).toString();

                switch (selectedType) {
                    case "History":
                        feedOrHistory.setImageResource(R.drawable.txt_history);
                        break;
                    default:
                        feedOrHistory.setImageResource(R.drawable.txt_feed);
                }
                loadFeed();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                feedOrHistory.setImageResource(R.drawable.txt_feed);
            }
        });

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return; // Ignore placeholder selection
                loadFeed();
                // Handle actual selection here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void onMoodEventClick(MoodEvent clickedEvent) {
        if (clickedEvent != null) {
            Log.d("FeedFragment", "Mood Event Clicked: " + clickedEvent.getId());
            // Create a Bundle to pass data
            Bundle args = new Bundle();
            args.putString("moodId", clickedEvent.getId());
            args.putString("emotionalState", clickedEvent.getEmotionalState().getDescription());
            args.putString("trigger", clickedEvent.getTrigger());
            args.putString("socialSituation", clickedEvent.getSocialSituation());

            // Create an instance of MoodInfoDialogFragment and pass the arguments
            MoodInfoDialogFragment dialog = new MoodInfoDialogFragment();
            dialog.setArguments(args);


            // Show the dialog
            dialog.show(requireActivity().getSupportFragmentManager(), "ViewMoodEvent");
        } else {
            Log.e("FeedFragment", "Clicked MoodEvent is null!"); // Debugging log
        }
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
        Log.d("FeedFragment", "loadFeed() triggered");
        String feedType = (String) binding.feedSpinner.getSelectedItem();
        String filter = (String) binding.filterSpinner.getSelectedItem();
        Log.d("FeedFragment", "Selected Feed Type: " + feedType + ", Filter: " + filter);
        if (User.getActiveUser() != null) {
            Object filterValue = null;
            QueryType selectedQueryType ;// = QueryType.FOLLOWING; // Unfiltered following posts is default value

            // Determine the base QueryType (History or Following)
            boolean isFollowing = feedType.equals("Following");

            switch (filter) {
                case "Filter...":  // Reset to default based on feed type
                    selectedQueryType = isFollowing ? QueryType.FOLLOWING : QueryType.HISTORY_MODIFIABLE;
                    if (feedViewModel != null) {
                        feedViewModel.setMoodEvents(new ArrayList<>());
                    }
                    break;
                case "Recent":
                    selectedQueryType = isFollowing ? QueryType.FOLLOWING_RECENT : QueryType.HISTORY_RECENT;
                    break;
                case "State":
                    showEmotionalStateDialog(isFollowing);  // Open state selection dialog
                    return;
                case "Reason":
                    showReasonInputDialog(isFollowing);  // Open reason input dialog
                    return;
                default:
                    selectedQueryType = isFollowing ? QueryType.FOLLOWING : QueryType.HISTORY_MODIFIABLE;
            }

            Log.d("FeedFragment", "Selected QueryType: " + selectedQueryType);
            MoodList.createMoodList(User.getActiveUser(), selectedQueryType,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            Log.d("FeedFragment", "returnMoodList() called");
                            moodList.clearMoodEvents(); // Clear old moods and then also refetch

                            if (feedViewModel != null) {
                                Log.d("FeedFragment", "Setting MoodEvents in ViewModel");
                                feedViewModel.setMoodEvents(moodList.getMoodEvents());
                            }
                        }
                      
                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList",e.getMessage());
                        }

                        @Override
                        public void updatedMoodList() {
                            Log.d("FeedFragment", "updatedMoodList() called");
                            // Handled automatically
                        }
                    }, filterValue);
        }
    }
    private void showReasonInputDialog(boolean isFollowing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Reason to Filter");

        // Create input field
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String reasonKeyword = input.getText().toString().trim();
            Log.d("FeedFragment", "User entered reason: " + reasonKeyword);

            if (!reasonKeyword.isEmpty()) {
                applySelectedFilter(isFollowing ? QueryType.FOLLOWING_REASON : QueryType.HISTORY_REASON, reasonKeyword);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEmotionalStateDialog(boolean isFollowing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Emotional State");

        // Convert enum values to a list of strings
        EmotionalState[] states = EmotionalState.values();
        String[] stateNames = new String[states.length];
        for (int i = 0; i < states.length; i++) {
            stateNames[i] = states[i].getDescription();  // Use user-friendly names
        }

        builder.setItems(stateNames, (dialog, which) -> {
            EmotionalState selectedState = states[which];  // Get selected state
            Log.d("FeedFragment", "User selected state: " + selectedState);

            // Now trigger loadFeed() with the selected state
            applySelectedFilter(isFollowing ? QueryType.FOLLOWING_STATE : QueryType.HISTORY_STATE, selectedState);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
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

}