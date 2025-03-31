package ca.ualberta.compileorcry.ui.profile;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * A dialog fragment that generates and displays a QR code for the user's profile.
 *
 * <p>This dialog creates a QR code containing a deep link to the current user's profile,
 * allowing other users to quickly access or share profiles by scanning the code with
 * a compatible application. The QR code uses the format "compileorcry://profile/[username]",
 * which can be handled by the application's deep linking system.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Generates a QR code with transparent background that matches app theme</li>
 *   <li>Provides a dismiss button to close the dialog</li>
 *   <li>Automatically scales to 85% of screen width for optimal visibility</li>
 *   <li>Uses a borderless dialog style for modern UI appearance</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * DialogFragment qrCodeFragment = new QrCodeDialog();
 * qrCodeFragment.show(getActivity().getSupportFragmentManager(), "userQrCode");
 * </pre>
 *
 * @see DialogFragment
 * @see BarcodeEncoder
 * @see User
 */
public class QrCodeDialog extends DialogFragment {

    /**
     * Initializes the dialog's style.
     * <p>
     * Sets the dialog to use STYLE_NO_FRAME for a borderless appearance
     * that better matches the application's visual style.
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
     * Creates the dialog instance with a title.
     * <p>
     * Sets the dialog title to "QR Code" to inform the user of the dialog's purpose.
     * </p>
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return A configured Dialog instance
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("QR Code");
        return dialog;
    }

    /**
     * Utility method to convert a bitmap's white pixels to transparent.
     * <p>
     * This improves the visual appearance of the QR code
     * </p>
     *
     * @param bitmap The source bitmap with white background
     * @return A new bitmap with transparent background
     */
    public static Bitmap makeWhiteTransparent(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);

                if (pixel == Color.WHITE) { // If pixel is white
                    newBitmap.setPixel(x, y, Color.TRANSPARENT);
                } else {
                    newBitmap.setPixel(x, y, pixel);
                }
            }
        }
        return newBitmap;
    }

    /**
     * Inflates the dialog layout and generates the QR code.
     * <p>
     * This method:
     * <ul>
     *   <li>Inflates the dialog layout</li>
     *   <li>Sets up the dismiss button</li>
     *   <li>Generates a QR code for the current user's profile</li>
     *   <li>Converts the QR code background to transparent</li>
     *   <li>Displays the QR code in the ImageView</li>
     *   <li>Handles any errors during QR code generation</li>
     * </ul>
     *
     * @param inflater The LayoutInflater object for creating views
     * @param container The parent view that will contain the dialog's UI
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The root View for the dialog's UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_viewqrcode, container, false);
        ImageView qrCodeImage = view.findViewById(R.id.qrcode_imageview);

        MaterialButton dismissButton = view.findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener((l)->{
            dismiss();
        });

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            String qrCodeData = "compileorcry://profile/" + User.getActiveUser().getUsername();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 400, 400);
            bitmap = makeWhiteTransparent(bitmap);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e){
            Log.e("QR", "Error Generating Profile QRCode");
        }

        return view;
    }

    /**
     * Adjusts the dialog size when it starts.
     * <p>
     * Resizes the dialog to 85% of the screen width while maintaining wrap content for height,
     * ensuring the QR code is large enough to be easily scanned while still fitting on screen.
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
