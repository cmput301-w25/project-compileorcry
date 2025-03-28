package ca.ualberta.compileorcry.ui.profile;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Dialog fragment that allows users to change their display name.
 * This class displays a custom styled dialog with a TextInputEditText field pre-populated with
 * the user's current name and provides options to save or cancel the change.
 */
public class ChangeNameDialog extends DialogFragment {

    private TextInputEditText editNameText;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use STYLE_NO_FRAME to get a borderless dialog
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Remove the title before setting content
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_changename, container, false);

        // Find views
        editNameText = view.findViewById(R.id.editname_text);
        saveButton = view.findViewById(R.id.save_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        // Pre-populate the name field with current user name
        User activeUser = User.getActiveUser();
        if (activeUser != null) {
            editNameText.setText(activeUser.getName());
            // Set cursor position to end of text
            editNameText.setSelection(editNameText.getText().length());
        }

        // Set up button listeners
        saveButton.setOnClickListener(v -> {
            String newName = editNameText.getText().toString().trim();
            if (!newName.isEmpty()) {
                // This line needs to be corrected - we need to use the updateName method
                User.getActiveUser().updateName(newName);
                dismiss();
            } else {
                editNameText.setError("Name cannot be empty");
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {

            int displayWidth = getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = (int) (displayWidth * 0.85);

            // Make dialog width 85% of screen width
            dialog.getWindow().setLayout(
                    dialogWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}