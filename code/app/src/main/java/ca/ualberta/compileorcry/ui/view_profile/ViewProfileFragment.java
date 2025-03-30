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
import ca.ualberta.compileorcry.features.mood.model.FollowHelper;


public class ViewProfileFragment extends Fragment {


    private FragmentViewProfileBinding binding;

    private User displayUser;

    /*
    Refreshes the labels on the follow/unfollow/requested button
     */
    private void refreshFollowStatus(){
        try {
            if(FollowHelper.isUserFollowing(User.getActiveUser().getUsername(), displayUser.getUsername())){ // User is following user
                binding.followButtonText.setText(R.string.button_unfollow);
                binding.followButton.setIconResource(R.drawable.ic_friends_40dp);
            } else if (FollowHelper.hasUserRequestedFollow(User.getActiveUser().getUsername(), displayUser.getUsername())) { // User has requested to follow
                binding.followButtonText.setText(R.string.button_requested);
                binding.followButton.setIconResource(R.drawable.ic_history_40dp);
            } else { // User is not following and has not requested
                binding.followButtonText.setText(R.string.button_follow);
                binding.followButton.setIconResource(R.drawable.ic_person_24dp);
            }
        } catch (InterruptedException e){
            Log.e("ViewProfile", "Error while checking follow status");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initially disable buttons until user is loaded
        binding.followButton.setEnabled(false);

        String displayProfileUsername = ViewProfileFragmentArgs.fromBundle(getArguments()).getProfileUsername();
        Log.d("ViewProfile", ("Displaying: " + displayProfileUsername));
        User.get_user(displayProfileUsername, (user, error) -> {
            if(error != null){ // Display error message on error fetching user
                binding.viewProfileName.setText(R.string.error_loading_user);
                binding.viewProfileUsername.setText(null);
                return;
            }
            displayUser = user;
            // Update UI with user info
            binding.viewProfileUsername.setText(displayUser.getUsername());
            binding.viewProfileName.setText(displayUser.getName());
            binding.followButton.setEnabled(true);

            refreshFollowStatus();
        });


        // Create button event handlers
        binding.followButton.setOnClickListener((l) -> {
            if(displayUser != null){
                try {
                    if(FollowHelper.isUserFollowing(User.getActiveUser().getUsername(), displayUser.getUsername())){ // User is following user
                        FollowHelper.unfollowUser(User.getActiveUser().getUsername(), displayUser.getUsername());
                    } else if (FollowHelper.hasUserRequestedFollow(User.getActiveUser().getUsername(), displayUser.getUsername())) { // User has requested to follow
                        FollowHelper.handleFollowRequest(displayUser, User.getActiveUser().getUsername(), false); // Deny follow request to cancel it
                    } else { // User is not following and has not requested
                        FollowHelper.createFollowRequest(User.getActiveUser(), displayUser.getUsername());
                    }
                } catch (InterruptedException e) {
                    Log.e("ViewProfile", "Error while handling follow button press");
                }
                refreshFollowStatus();
            }
        });



        return root;
    }

}
