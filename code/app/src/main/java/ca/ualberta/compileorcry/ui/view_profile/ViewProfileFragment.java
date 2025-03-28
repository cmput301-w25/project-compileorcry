package ca.ualberta.compileorcry.ui.view_profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ca.ualberta.compileorcry.databinding.FragmentViewProfileBinding;
import ca.ualberta.compileorcry.domain.models.User;


public class ViewProfileFragment extends Fragment {


    private FragmentViewProfileBinding binding;

    private User displayUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String displayProfileUsername = ViewProfileFragmentArgs.fromBundle(getArguments()).getProfileUsername();
        Log.d("ViewProfile", ("Displaying: " + displayProfileUsername));
        User.get_user(displayProfileUsername, (user, error) -> {
            if(error != null){
                //TODO: Add error handling
            }
            displayUser = user;
            binding.viewProfileUsername.setText(user.getUsername());
            binding.viewProfileName.setText(user.getName());
        });


        return root;
    }

}
