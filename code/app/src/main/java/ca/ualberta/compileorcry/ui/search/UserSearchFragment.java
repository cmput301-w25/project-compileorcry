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

public class UserSearchFragment extends Fragment {
    private FragmentUserSearchBinding binding;
    private ca.ualberta.compileorcry.ui.search.UserSearchAdapter adapter;
    private ArrayList<String> searchResults;

    public UserSearchFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchResults = new ArrayList<>();
        adapter = new ca.ualberta.compileorcry.ui.search.UserSearchAdapter(requireContext(), searchResults);

        // Setup RecyclerView
        binding.searchResultsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.searchResultsList.setAdapter(adapter);
/*
        // Add text watcher for search input
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.searchIcon.setImageResource(R.drawable.ic_baseline_search_24dp); // Change to "X" icon
                    binding.searchResultsList.setVisibility(View.VISIBLE); // Show results
                } else {
                    binding.searchIcon.setImageResource(R.drawable.ic_baseline_search_24dp); // Change back to search icon
                    binding.searchResultsList.setVisibility(View.GONE); // Hide results
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    performSearch(s.toString());
                } else {
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });*/

        // Handle clear button click
        binding.searchIcon.setOnClickListener(v -> {
            if (binding.searchInput.getText().length() > 0) {
                binding.searchInput.setText("");  // Clear input
                binding.searchIcon.setImageResource(R.drawable.ic_baseline_search_24dp); // Reset icon
                binding.searchResultsList.setVisibility(View.GONE); // Hide results
            }
        });
    }
    /*
    private void performSearch(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                ArrayList<String> results = UserSearch.findUser(query);
                requireActivity().runOnUiThread(() -> {
                    if (results != null) {
                        searchResults.clear();
                        searchResults.addAll(results);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (InterruptedException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show());
            }
        });
        executor.shutdown();
    } */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
