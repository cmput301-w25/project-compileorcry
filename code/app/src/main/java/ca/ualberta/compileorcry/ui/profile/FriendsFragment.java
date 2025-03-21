package ca.ualberta.compileorcry.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import ca.ualberta.compileorcry.databinding.FragmentFriendsBinding;

/**
 * Fragment that displays and manages the user's friends.
 * This class shows a list of the user's friends and provides functionality
 * for managing friend relationships.
 * Features:
 * - Displays a list of the user's friends
 * - Allows navigation back to the profile page
 * - (Future) Add/remove friends functionality
 */
public class FriendsFragment extends Fragment {
    /** View binding for accessing UI elements */
    private FragmentFriendsBinding binding;

    /**
     * Inflates the fragment layout using view binding and initializes UI elements.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    /**
     * Sets up button click listeners for the friends page actions.
     * This method configures:
     * 1. Back button (to return to the profile page)
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButton.setOnClickListener((View v) -> {
            // Navigate back using Navigation component
            Navigation.findNavController(view).navigateUp();
            // Alternative if you defined a specific action
            // Navigation.findNavController(view).navigate(R.id.action_friendsFragment_to_navigation_profile);
        });
    }

    /**
     * Cleans up resources when the view is destroyed.
     * This method nullifies the view binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}