package ca.ualberta.compileorcry.ui.login;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentLoginBinding;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Fragment that handles user login functionality.
 * This class provides a user interface for existing users to log in to the application
 * by entering their username.
 *
 * Features:
 * - Username validation
 * - Error indication with color change
 * - Disabling UI controls during authentication
 * - Navigation to main app content upon successful login
 * - Navigation to registration for new users
 *
 */
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    /**
     * Inflates the fragment layout using view binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container If non-null, this is the parent view that the fragment's UI should be
     *                  attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state
     * @return The View for the fragment's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    /**
     * Disables all UI input controls.
     * This method is called during the authentication process to prevent
     * multiple simultaneous login attempts.
     */
    public void disableUI(){
        binding.loginUsernameText.setEnabled(false);
        binding.loginButton.setEnabled(false);
        binding.registerButton.setEnabled(false);
    }

    /**
     * Enables all UI input controls.
     * This method is called when authentication fails to allow the user to retry.
     */
    public void enableUI(){
        binding.loginUsernameText.setEnabled(true);
        binding.loginButton.setEnabled(true);
        binding.registerButton.setEnabled(true);
    }

    /**
     * Sets up UI element listeners after the view is created.
     * This method configures:
     * 1. The login button to validate and submit credentials
     * 2. The register button to navigate to the registration screen
     *
     * @param view The View returned by onCreateView
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                          previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loginButton.setOnClickListener((View v) -> {
            String username = binding.loginUsernameText.getText().toString();
            if(username.isEmpty()){
                binding.loginUsernameLayout.setError(getString(R.string.username_required));
                binding.loginUsernameLayout.setStartIconTintList(
                        getResources().getColorStateList(R.color.anger, getContext().getTheme())
                );
                return;
            }
            disableUI();

            User.get_user(username, (user, error) -> { // Handle Firebase Response
                if(error != null || user == null) { // Error Trap
                    binding.loginUsernameLayout.setError(error);
                    binding.loginUsernameLayout.setStartIconTintList(
                            getResources().getColorStateList(R.color.anger, getContext().getTheme())
                    );
                    enableUI();
                    return;
                }


                findNavController(view).navigate(R.id.navigation_feed);
                getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);

                User.setActiveUser(user, getActivity());
            });
        });

        binding.registerButton.setOnClickListener((View v) -> {
            findNavController(view).navigate(R.id.navigation_registration);
            //TODO: Add passing typed username to registration fragment
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
