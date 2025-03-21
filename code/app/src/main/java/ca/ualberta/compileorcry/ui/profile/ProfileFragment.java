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
 * - History viewing functionality is not implemented
 * - Friends management functionality is not implemented
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

        binding.profileUsername.setText("@" + User.getActiveUser().getUsername());
        binding.profileName.setText(User.getActiveUser().getName());

        // Listener to update account Name if changes
        this.nameListenerRegistration = User.getActiveUser().getUserDocRef().addSnapshotListener((documentSnapshot, error) -> {
           if(error != null){
               Log.e("ProfileFragment", "Error Registering Name Listener");
               return;
           }
           if(documentSnapshot != null && documentSnapshot.exists()){
               binding.profileName.setText(documentSnapshot.getString("name"));
           }
        });

        return root;
    }

    /**
     * Sets up button click listeners for the profile actions.
     * This method configures:
     * 1. Request button (for viewing friend requests - not implemented)
     * 2. History button (for viewing mood history - not implemented)
     * 3. Edit button (for changing display name)
     * 4. Friends button (for managing friends - not implemented)
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
            //TODO: Implement History Function
        });

        binding.editButton.setOnClickListener((View v) -> { // Edit Name Dialog
            DialogFragment editNameDialog = new ChangeNameDialog();
            editNameDialog.show(getActivity().getSupportFragmentManager(), "editName");
        });

        binding.friendsButton.setOnClickListener((View v) -> {
            //TODO: Implement Friends Function
        });

        binding.logoutButton.setOnClickListener((View v) -> {
            User.logoutUser(getActivity());
        });


        binding.friendsButton.setOnClickListener((View v) -> {
            // Since you're using Navigation in your app (based on the imports),
            // let's use the Navigation component for fragment navigation
            Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_friendsFragment);
        });
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
        binding = null;
        this.nameListenerRegistration.remove(); // Remove listener on destroy
    }
}