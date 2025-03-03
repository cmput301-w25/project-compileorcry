package ca.ualberta.compileorcry.ui.registration;

import static androidx.navigation.Navigation.findNavController;

import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentRegistrationBinding;
import ca.ualberta.compileorcry.domain.models.User;

public class RegistrationFragment extends Fragment {
    private FragmentRegistrationBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    public void disableUI(){
        binding.registrationUsernameLayout.setEnabled(false);
        binding.registrationNameLayout.setEnabled(false);
        binding.backButton.setEnabled(false);
        binding.doneButton.setEnabled(false);
    }
    public void enableUI(){
        binding.registrationUsernameLayout.setEnabled(true);
        binding.registrationNameLayout.setEnabled(true);
        binding.backButton.setEnabled(true);
        binding.doneButton.setEnabled(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButton.setOnClickListener((View v) -> {
            findNavController(view).navigate(R.id.navigation_login);
        });

        binding.doneButton.setOnClickListener((View v) -> { // Attempt to register user
            String username = binding.registrationUsernameText.getText().toString();
            String name = binding.registrationNameText.getText().toString();
            if(username.isEmpty()){
                binding.registrationUsernameLayout.setError(getString(R.string.username_required));
                binding.registrationUsernameLayout.setStartIconTintList(
                        getResources().getColorStateList(R.color.anger, getContext().getTheme())
                );
            }
            if(name.isEmpty()){
                binding.registrationNameLayout.setError(getString(R.string.name_required));
                binding.registrationNameLayout.setStartIconTintList(
                        getResources().getColorStateList(R.color.anger, getContext().getTheme())
                );
            }
            disableUI();

            User.register_user(username, name, (user, error) -> {
                if(error != null || user == null){
                    binding.registrationUsernameLayout.setError(error);
                    binding.registrationUsernameLayout.setStartIconTintList(
                            getResources().getColorStateList(R.color.anger, getContext().getTheme())
                    );
                }
                //TODO: Implement saving locally username
                User.setActiveUser(user);

                // Navigate to Main App
                findNavController(view).navigate(R.id.navigation_profile);
                getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
