package ca.ualberta.compileorcry.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
 * A dialog fragment that allows users to change their display name in the application.
 *
 * <p>This dialog presents users with a form containing:
 * <ul>
 *   <li>A text input field pre-populated with the user's current display name</li>
 *   <li>Save button to confirm the name change</li>
 *   <li>Cancel button to dismiss the dialog without changes</li>
 * </ul>
 * </p>
 *
 * <p>The dialog validates that the name is not empty before allowing the save operation.</p>
 *
 * <p>Typical usage:
 * <pre>
 * DialogFragment editNameDialog = new ChangeNameDialog();
 * editNameDialog.show(getActivity().getSupportFragmentManager(), "editName");
 * </pre>
 * </p>
 *
 * @see DialogFragment
 * @see User
 */
public class ChangeNameDialog extends DialogFragment {

    private TextInputEditText editNameText;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    /**
     * Initializes the dialog's style.
     * <p>
     * Sets the dialog to use STYLE_NO_FRAME for a borderless appearance.
     * </p>
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use STYLE_NO_FRAME to get a borderless dialog
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }

    /**
     * Creates the dialog instance.
     * <p>
     * Removes the title bar from the dialog for a cleaner look.
     * </p>
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return A Dialog instance with no title
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Remove the title before setting content
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /**
     * Inflates the dialog layout and sets up UI components.
     * <p>
     * This method:
     * <ul>
     *   <li>Inflates the dialog layout</li>
     *   <li>Initializes UI references</li>
     *   <li>Populates the name field with the current user's name</li>
     *   <li>Sets up button event handlers</li>
     * </ul>
     * </p>
     *
     * @param inflater The LayoutInflater object for inflating views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The root View for the fragment's UI
     */
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
                User.getActiveUser().setName(newName);
                dismiss();
            } else {
                editNameText.setError("Name cannot be empty");
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * Adjusts the dialog size when it starts.
     * <p>
     * Resizes the dialog to 85% of the screen width while maintaining wrap content for height.
     * </p>
     */
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