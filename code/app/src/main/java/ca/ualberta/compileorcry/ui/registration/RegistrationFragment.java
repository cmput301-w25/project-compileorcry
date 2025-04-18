package ca.ualberta.compileorcry.ui.registration;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentRegistrationBinding;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Fragment that handles user registration functionality.
 * This class provides a user interface for new users to create an account
 * by entering a unique username and display name.
 *
 * Features:
 * - Input validation for username and display name
 * - Error indication with color change
 * - Disabling UI controls during registration process
 * - Navigation back to login screen
 * - Navigation to main app content upon successful registration
 *
 * Outstanding issues:
 * - Local storage of username for persistent login is not implemented (marked with TODO)
 */
public class RegistrationFragment extends Fragment {
    /** View binding for accessing UI elements */
    private FragmentRegistrationBinding binding;
    private String passedUsername;

    /**
     * Inflates the fragment layout using view binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Bundle args = getArguments();
        if(args.containsKey("passed_username")){
            passedUsername = args.getString("passed_username");
        }
        return root;
    }

    /**
     * Disables all UI input controls.
     * This method is called during the registration process to prevent
     * multiple simultaneous registration attempts.
     */
    public void disableUI(){
        binding.registrationUsernameLayout.setEnabled(false);
        binding.registrationNameLayout.setEnabled(false);
        binding.backButton.setEnabled(false);
        binding.doneButton.setEnabled(false);
    }
    /**
     * Enables all UI input controls.
     * This method is called when registration fails to allow the user to retry.
     */
    public void enableUI(){
        binding.registrationUsernameLayout.setEnabled(true);
        binding.registrationNameLayout.setEnabled(true);
        binding.backButton.setEnabled(true);
        binding.doneButton.setEnabled(true);
    }

    /**
     * Sets up UI element listeners after the view is created.
     * This method configures:
     * 1. The back button to navigate to the login screen
     * 2. The done button to validate inputs and register the user
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(passedUsername != null && !(passedUsername.isEmpty())){
            binding.registrationUsernameText.setText(passedUsername);
        }
        binding.backButton.setOnClickListener((View v) -> {
            findNavController(view).navigate(R.id.navigation_login);
        });

        binding.doneButton.setOnClickListener((View v) -> { // Attempt to register user
            boolean isValid = true;
            String username = binding.registrationUsernameText.getText().toString();
            String name = binding.registrationNameText.getText().toString();
            if(username.isEmpty()){
                binding.registrationUsernameLayout.setError(getString(R.string.username_required));
                binding.registrationUsernameLayout.setStartIconTintList(
                        getResources().getColorStateList(R.color.anger, getContext().getTheme())
                );
                isValid = false;
            }
            if(name.isEmpty()){
                binding.registrationNameLayout.setError(getString(R.string.name_required));
                binding.registrationNameLayout.setStartIconTintList(
                        getResources().getColorStateList(R.color.anger, getContext().getTheme())
                );
                isValid = false;
            }

            if (!isValid) { return; }

            disableUI();

            User.register_user(username, name, (user, error) -> {
                if(error != null || user == null){
                    binding.registrationUsernameLayout.setError(error);
                    binding.registrationUsernameLayout.setStartIconTintList(
                            getResources().getColorStateList(R.color.anger, getContext().getTheme())
                    );
                    enableUI();
                    return;
                }

                User.setActiveUserPersist(user, getActivity());

                // Navigate to Main App
                findNavController(view).navigate(R.id.navigation_feed);
                getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
            });
        });
    }
    /**
     * Cleans up resources when the view is destroyed.
     * Specifically, this method nullifies the view binding to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
