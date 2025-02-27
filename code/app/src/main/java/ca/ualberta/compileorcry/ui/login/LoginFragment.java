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

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    public void disableUI(){
        binding.loginUsernameText.setEnabled(false);
        binding.loginButton.setEnabled(false);
        binding.registerButton.setEnabled(false);
    }

    public void enableUI(){
        binding.loginUsernameText.setEnabled(true);
        binding.loginButton.setEnabled(true);
        binding.registerButton.setEnabled(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loginButton.setOnClickListener((View v) -> {
            String username = binding.loginUsernameText.getText().toString();
            if(username.isEmpty()){
                Toast errorToast = Toast.makeText(
                        getActivity(),
                        "Username must be filled.",
                        Toast.LENGTH_SHORT
                );
                errorToast.show();
                return;
            }
            disableUI();

            User.get_user(username, (user, error) -> { // Handle Firebase Response
                if(error != null || user == null) { // Error Trap
                    Toast errorToast = Toast.makeText(
                            getActivity(),
                            error,
                            Toast.LENGTH_SHORT
                    );
                    errorToast.show();
                    enableUI();
                    return;
                }


                findNavController(view).navigate(R.id.navigation_profile);
                getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
                //TODO: Save username locally
                User.setActiveUser(user);
            });
        });

        binding.registerButton.setOnClickListener((View v) -> {
            findNavController(view).navigate(R.id.navigation_registration);
            //TODO: Add passing typed username to registration fragment
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
