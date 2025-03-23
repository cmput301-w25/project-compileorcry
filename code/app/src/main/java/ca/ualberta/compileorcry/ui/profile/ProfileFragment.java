package ca.ualberta.compileorcry.ui.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentProfileBinding;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Fragment that displays and manages the user's profile information.
 * This class shows the user's username and display name, and provides buttons
 * for various profile-related actions such as editing the name, viewing friend
 * requests, viewing mood history, and managing friends.
 *
 * Features:
 * - Displays user profile information (username and display name)
 * - Real-time updates to profile information using Firestore listeners
 * - Dialog for editing the display name
 * - Navigation to various user-related features
 *
 * Outstanding issues:
 * - Friend request functionality is not implemented
 * - History button now navigates to feed
 * - Friends management functionality is implemented
 */
public class ProfileFragment extends Fragment {
    /** View binding for accessing UI elements */
    private FragmentProfileBinding binding;
    /** Registration for the Firestore listener that monitors name changes */
    private ListenerRegistration nameListenerRegistration;

    /**
     * Inflates the fragment layout using view binding and initializes UI elements.
     * This method also sets up a Firestore listener to detect changes to the user's name.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI
     */
    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        User activeUser = User.getActiveUser();
        if (activeUser != null) {
            binding.profileUsername.setText("@" + activeUser.getUsername());
            binding.profileName.setText(activeUser.getName());

            // Listener to update account Name if changes
            if (activeUser.getUserDocRef() != null) {
                this.nameListenerRegistration = activeUser.getUserDocRef().addSnapshotListener((documentSnapshot, error) -> {
                    if(error != null){
                        Log.e("ProfileFragment", "Error Registering Name Listener");
                        return;
                    }
                    if(documentSnapshot != null && documentSnapshot.exists()){
                        binding.profileName.setText(documentSnapshot.getString("name"));
                    }
                });
            } else {
                Log.e("ProfileFragment", "Active user has no document reference");
            }
        } else {
            // Handle the case where there is no active user
            Log.e("ProfileFragment", "No active user found");
            binding.profileUsername.setText("@");
            binding.profileName.setText("Not logged in");

            // Redirect to login screen or handle appropriately
            // For example:
            // User.redirectToLogin(getActivity());
        }

        return root;
    }

    /**
     * Sets up button click listeners for the profile actions.
     * This method configures:
     * 1. Request button (for viewing friend requests - not implemented)
     * 2. History button (navigates to the feed page)
     * 3. Edit button (for changing display name)
     * 4. Friends button (for managing friends)
     * 5. Logout button
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.requestsButton.setOnClickListener((View v) -> {
            //TODO: Implement Request Function
        });

        binding.historyButton.setOnClickListener((View v) -> {
            // Navigate to the feed page
            navigateToFeed();
        });

        binding.editButton.setOnClickListener((View v) -> { // Edit Name Dialog
            DialogFragment editNameDialog = new ChangeNameDialog();
            editNameDialog.show(getActivity().getSupportFragmentManager(), "editName");
        });

        binding.logoutButton.setOnClickListener((View v) -> {
            User.logoutUser(getActivity());
        });

        binding.friendsButton.setOnClickListener((View v) -> {
            // Navigate to the friends page
            Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_friendsFragment);
        });
    }

    /**
     * Navigates to the feed page and updates the selected item in the bottom navigation.
     */
    private void navigateToFeed() {
        if (getActivity() != null) {
            // Navigate to the feed fragment
            Navigation.findNavController(getView()).navigate(R.id.navigation_feed);

            // Update the selected item in the bottom navigation
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
            bottomNavigationView.setSelectedItemId(R.id.navigation_feed);
        }
    }

    /**
     * Cleans up resources when the view is destroyed.
     * This method:
     * 1. Removes the Firestore listener to prevent memory leaks
     * 2. Nullifies the view binding reference
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (this.nameListenerRegistration != null) {
            this.nameListenerRegistration.remove(); // Remove listener on destroy
        }

        binding = null;
    }
}