package ca.ualberta.compileorcry.ui.feed;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentFeedBinding;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;
import ca.ualberta.compileorcry.ui.feed.MoodInfoDialogFragment;

public class FeedFragment extends Fragment {
    private FragmentFeedBinding binding;
    private FeedViewModel feedViewModel;
    private MoodEventAdapter adapter;
    private Spinner filterSpinner; // filter by recency, state, reason, etc
    private Spinner feedSpinner; // feed type personal history, following, or map
    // Feed type options
    private static final String[] FEED_TYPES = {"History", "Following"};
    // Filter options
    private static final String[] FILTER_OPTIONS = {"None", "Recent", "State", "Reason"};

    private MoodList moodList;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("moodEventUpdated", this, (requestKey, result) -> {
            refreshMoodList();
        });

        // Find Spinners
        Spinner feedSpinner = binding.feedSpinner;
        Spinner filterSpinner = binding.filterSpinner;

        // Create Adapter using Java Array
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
        // Prevent selection of the first item (Sort or Filter)
        feedSpinner.setSelection(0, false);
        filterSpinner.setSelection(0, false);


        // Initialize RecyclerView with empty list
        adapter = new MoodEventAdapter(new ArrayList<>(), this::onMoodEventClick);
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
    private void onMoodEventClick(MoodEvent clickedEvent) {
        if (clickedEvent != null) {
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

    private void refreshMoodList() {
        if (User.getActiveUser() != null) {
            QueryType selectedQueryType = QueryType.HISTORY_MODIFIABLE; // Default to modifiable history

            MoodList.createMoodList(User.getActiveUser(), selectedQueryType, new MoodList.MoodListListener() {
                @Override
                public void returnMoodList(MoodList initializedMoodList) {
                    FeedFragment.this.moodList = initializedMoodList;

                    if (feedViewModel != null) {
                        feedViewModel.setMoodEvents(moodList.getMoodEvents());
                    }
                }

                @Override
                public void updatedMoodList() {
                    if (feedViewModel != null) {
                        feedViewModel.setMoodEvents(moodList.getMoodEvents());
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("FeedFragment", "Error refreshing mood list: " + e.getMessage());
                }
            }, null);
        }
    }









    private void loadFeed() {
        String feedType = (String) binding.feedSpinner.getSelectedItem();
        String filter = (String) binding.filterSpinner.getSelectedItem();
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

            MoodList.createMoodList(User.getActiveUser(), selectedQueryType,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            FeedFragment.this.moodList = moodList;
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

                        }
                    }, null);
        }
    }
}