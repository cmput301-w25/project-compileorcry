package ca.ualberta.compileorcry.ui.view_profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.databinding.FragmentViewProfileBinding;
import ca.ualberta.compileorcry.domain.models.User;


public class ViewProfileFragment extends Fragment {


    private FragmentViewProfileBinding binding;

    private User displayUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initially disable buttons until user is loaded
        binding.followButton.setEnabled(false);

        String displayProfileUsername = ViewProfileFragmentArgs.fromBundle(getArguments()).getProfileUsername();
        Log.d("ViewProfile", ("Displaying: " + displayProfileUsername));
        User.get_user(displayProfileUsername, (user, error) -> {
            if(error != null){
                binding.viewProfileName.setText(R.string.error_loading_user);
                binding.viewProfileUsername.setText(null);
                return;
            }
            displayUser = user;
            binding.viewProfileUsername.setText(user.getUsername());
            binding.viewProfileName.setText(user.getName());

            binding.followButton.setEnabled(true);
            //TODO: Add displaying profile photo
        });


        // Create button event handlers
        binding.followButton.setOnClickListener((l) -> {
            if(displayUser != null){
                //TODO: Add functionality
            }
        });



        return root;
    }

}
