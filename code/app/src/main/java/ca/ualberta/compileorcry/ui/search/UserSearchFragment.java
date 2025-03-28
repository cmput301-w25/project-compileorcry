package ca.ualberta.compileorcry.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentUserSearchBinding;
import ca.ualberta.compileorcry.domain.models.UserSearch;

/**
 * Fragment that provides a UI for searching users by their username.
 * Displays a search bar, a results list, and context-aware empty state views.
 */
public class UserSearchFragment extends Fragment {

    private FragmentUserSearchBinding binding;
    private UserSearchAdapter adapter;
    private ArrayList<String> searchResults;
    private boolean isClearIconVisible = false;

    /**
     * Required empty public constructor
     */
    public UserSearchFragment() {}

    /**
     * Inflates the view for this fragment using ViewBinding.
     *
     * @param inflater           the LayoutInflater object
     * @param container          the parent view that this fragment's UI should be attached to
     * @param savedInstanceState saved instance state bundle
     * @return the root view for this fragment's layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes UI components, listeners, and sets default views.
     *
     * @param view               the root view
     * @param savedInstanceState saved instance state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchResults = new ArrayList<>();
        adapter = new UserSearchAdapter(requireContext(), searchResults);
        binding.searchResultsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.searchResultsList.setAdapter(adapter);

        // Show default empty state
        showEmptyState(R.drawable.ic_search_big,
                "Search your friends' username to view their profile!");

        // Watch for user input to update icon state
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isClearIconVisible && s.length() > 0) {
                    binding.searchIcon.setImageResource(R.drawable.ic_baseline_search_24dp);
                    isClearIconVisible = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle search or clear icon click
        binding.searchIcon.setOnClickListener(v -> {
            if (isClearIconVisible) {
                clearSearch();
            } else {
                String query = binding.searchInput.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                    binding.searchIcon.setImageResource(R.drawable.ic_clear_24dp);
                    isClearIconVisible = true;
                } else {
                    Toast.makeText(requireContext(), "Enter a username", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Executes a user search on a background thread and updates UI with the results.
     *
     * @param query the username to search for
     */
    private void performSearch(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                ArrayList<String> results = UserSearch.findUser(query);
                requireActivity().runOnUiThread(() -> {
                    searchResults.clear();
                    if (results != null && !results.isEmpty()) {
                        searchResults.addAll(results);
                        adapter.notifyDataSetChanged();
                        binding.searchResultsList.setVisibility(View.VISIBLE);
                        binding.emptyStateContainer.setVisibility(View.GONE);
                    } else {
                        adapter.notifyDataSetChanged();
                        binding.searchResultsList.setVisibility(View.GONE);
                        showEmptyState(R.drawable.ic_clear_big, "No users with this username");
                    }
                });
            } catch (InterruptedException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show());
            }
        });
        executor.shutdown();
    }

    /**
     * Resets the search input, results, and UI state to default.
     */
    private void clearSearch() {
        binding.searchInput.setText("");
        searchResults.clear();
        adapter.notifyDataSetChanged();
        binding.searchResultsList.setVisibility(View.GONE);
        showEmptyState(R.drawable.ic_search_big,
                "Search your friends' username to view their profile!");
        binding.searchIcon.setImageResource(R.drawable.ic_baseline_search_24dp);
        isClearIconVisible = false;
    }

    /**
     * Shows the empty state view with the given icon and message.
     *
     * @param iconResId the drawable resource ID for the icon
     * @param message   the message to display below the icon
     */
    private void showEmptyState(int iconResId, String message) {
        binding.emptyStateContainer.setVisibility(View.VISIBLE);
        binding.emptyStateIcon.setImageResource(iconResId);
        binding.emptyStateText.setText(message);
    }

    /**
     * Cleans up ViewBinding reference when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
