package ca.ualberta.compileorcry.ui.search;

import android.os.Bundle;
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
    private UserSearchAdapter adapter;
    private ArrayList<String> searchResults;

    public UserSearchFragment() {}

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
        adapter = new UserSearchAdapter(requireContext(), searchResults);
        binding.searchResultsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.searchResultsList.setAdapter(adapter);

        // Search only when user taps the icon
        binding.searchIcon.setOnClickListener(v -> {
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
                binding.searchResultsList.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(requireContext(), "Enter a username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                ArrayList<String> results = UserSearch.findUser(query);
                requireActivity().runOnUiThread(() -> {
                    searchResults.clear();
                    if (results != null && !results.isEmpty()) {
                        searchResults.addAll(results);
                    } else {
                        Toast.makeText(requireContext(), "No users found", Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                });
            } catch (InterruptedException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show());
            }
        });
        executor.shutdown();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
