package ca.ualberta.compileorcry.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;

public class ChangeNameDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Prepare Dialog
        View newDialogView = inflater.inflate(R.layout.dialog_changename, null);
        EditText editNameText = newDialogView.findViewById(R.id.editname_text);
        editNameText.setText(User.getActiveUser().getName());

        builder.setView(newDialogView)
                .setPositiveButton("Save", (dialogInterface, i) -> { // Save New Name
                    User.getActiveUser().setName(editNameText.getText().toString());
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    // Cancel
                })
                .setTitle("Change Name");
        return builder.create();
    }
}
