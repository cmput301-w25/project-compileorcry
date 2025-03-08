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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import ca.ualberta.compileorcry.databinding.FragmentProfileBinding;
import ca.ualberta.compileorcry.domain.models.User;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ListenerRegistration nameListenerRegistration;


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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        this.nameListenerRegistration.remove(); // Remove listener on destroy
    }
}