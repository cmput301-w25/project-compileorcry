package ca.ualberta.compileorcry.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

        // Handle spinner selections
        feedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) return;
                feedOrHistory.setImageResource(R.drawable.txt_feed);
                String selectedType = parent.getItemAtPosition(position).toString();

                switch (selectedType) {
                    case "History":
                        feedOrHistory.setImageResource(R.drawable.txt_history);
                        break;
                    default:
                        feedOrHistory.setImageResource(R.drawable.txt_feed);
                }
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
                // Handle actual selection here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        // Initialize RecyclerView with empty list
        adapter = new MoodEventAdapter(new ArrayList<>());
        binding.recyclerViewMoodHistory.setLayoutManager(
                new LinearLayoutManager(requireContext())  // Use requireContext()
        );
        binding.recyclerViewMoodHistory.setAdapter(adapter);

         // Setup FAB with null safety
        if (binding.fabAddMood != null) {
            binding.fabAddMood.setOnClickListener(v -> {
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.navigation_new);
                }
            });
        }

        // Initialize ViewModel
        feedViewModel = new ViewModelProvider(requireActivity()).get(FeedViewModel.class);

        // Observe LiveData with lifecycle awareness
        feedViewModel.getMoodEvents().observe(getViewLifecycleOwner(), moodEvents -> {
            if (moodEvents != null && adapter != null) {
                adapter.updateData(moodEvents);
                binding.recyclerViewMoodHistory.smoothScrollToPosition(0);
            }
        });

        // Handle feed queries from spinner values
        loadFeed();
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
        String feedType = (String) binding.feedSpinner.getSelectedItem();
        String filter = (String) binding.filterSpinner.getSelectedItem();
        if (User.getActiveUser() != null) {
            QueryType selectedQueryType = QueryType.FOLLOWING; // Unfiltered following posts is default value

            if (feedType.equals("History")) {
                switch (filter) {
                    case "None":
                        selectedQueryType = QueryType.HISTORY_MODIFIABLE;
                        break;
                    case "Recent":
                        selectedQueryType = QueryType.HISTORY_RECENT;
                        break;
                    case "State":
                        selectedQueryType = QueryType.HISTORY_STATE;
                        break;
                    case "Reason":
                        selectedQueryType = QueryType.HISTORY_REASON;
                        break;
                }
            } else if (feedType.equals("Following")) {
                switch (filter) {
                    case "None":
                        selectedQueryType = QueryType.FOLLOWING;
                        break;
                    case "Recent":
                        selectedQueryType = QueryType.FOLLOWING_RECENT;
                        break;
                    case "State":
                        selectedQueryType = QueryType.FOLLOWING_STATE;
                        break;
                    case "Reason":
                        selectedQueryType = QueryType.FOLLOWING_REASON;
                        break;
                }
            }

            MoodList.createMoodList(User.getActiveUser(), selectedQueryType,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            if (feedViewModel != null) {
                                feedViewModel.setMoodEvents(moodList.getMoodEvents());
                            }
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
    }
}