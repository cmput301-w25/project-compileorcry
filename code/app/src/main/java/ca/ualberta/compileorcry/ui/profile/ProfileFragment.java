package ca.ualberta.compileorcry.ui.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.ListenerRegistration;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentProfileBinding;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Fragment that displays and manages the user's profile information.
 * This class shows the user's username and display name, and provides buttons
 * for various profile-related actions such as editing the name, viewing friend
 * requests, viewing mood history, and managing friends.
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

        // Check if there's an active user before trying to access properties
        User activeUser = User.getActiveUser();
        if (activeUser != null) {
            binding.profileUsername.setText("@" + activeUser.getUsername());
            binding.profileName.setText(activeUser.getName());

            // Listener to update account Name if changes
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
            // Handle the case when no user is logged in
            binding.profileUsername.setText("@username");
            binding.profileName.setText("Not logged in");

            // Navigate back to login if no user is logged in
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.navigation_login);
        }

        return root;
    }

    /**
     * Sets up button click listeners for the profile actions.
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Only set up button listeners if there's an active user
        User activeUser = User.getActiveUser();
        if (activeUser == null) {
            return; // Don't set up listeners if no user is logged in
        }

        binding.requestsButton.setOnClickListener((View v) -> {
            RequestsBottomSheet requestsBottomSheet = new RequestsBottomSheet();
            requestsBottomSheet.show(getChildFragmentManager(), "requestsBottomSheet");
        });

        binding.editButton.setOnClickListener((View v) -> { // Edit Name Dialog
            DialogFragment editNameDialog = new ChangeNameDialog();
            editNameDialog.show(getActivity().getSupportFragmentManager(), "editName");
        });

        binding.friendsButton.setOnClickListener((View v) -> {
            // Navigate to the friends fragment
            Navigation.findNavController(view).navigate(R.id.navigation_friends);
        });

        binding.logoutButton.setOnClickListener((View v) -> {
            User.logoutUser(getActivity());
        });

        binding.profileImage.setOnClickListener((View v) -> {
            DialogFragment qrCodeFragment = new QrCodeDialog();
            qrCodeFragment.show(getActivity().getSupportFragmentManager(), "userQrCode");
        });

    }

    /**
     * Cleans up resources when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up listener only if it was created
        if (nameListenerRegistration != null) {
            nameListenerRegistration.remove();
        }
        binding = null;
    }
}