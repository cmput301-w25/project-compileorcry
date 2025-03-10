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

/**
 * Dialog fragment that allows users to change their display name.
 * This class displays a simple dialog with an EditText field pre-populated with
 * the user's current name and provides options to save or cancel the change.
 *
 * Features:
 * - Pre-populates the input field with the current user name
 * - Provides Save and Cancel buttons
 * - Updates the user's name in the database upon confirmation
 * - Follows Android's dialog design patterns
 */
public class ChangeNameDialog extends DialogFragment {

    /**
     * Creates and configures the dialog to change the user's name.
     * This method:
     * 1. Inflates the dialog layout
     * 2. Sets up the EditText with the current user name
     * 3. Configures Save and Cancel buttons with appropriate actions
     *
     * @param savedInstanceState If non-null, this dialog is being re-constructed from a
     *                             previous saved state
     * @return The configured AlertDialog instance
     */
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
