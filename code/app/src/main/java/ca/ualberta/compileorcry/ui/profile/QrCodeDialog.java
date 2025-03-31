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

public class QrCodeDialog extends DialogFragment {

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
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("QR Code");
        return dialog;
    }

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
