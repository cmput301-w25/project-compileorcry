package ca.ualberta.compileorcry.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;

public class QrCodeDialog extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Prepare Dialog
        View newDialogView = inflater.inflate(R.layout.dialog_viewqrcode, null);
        ImageView qrCodeImage = newDialogView.findViewById(R.id.qrcode_imageview);

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(User.getActiveUser().getUsername(), BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e){
            Log.e("QR", "Error Generating Profile QRCode");
        }

        builder.setView(newDialogView)
                .setPositiveButton("Dismiss", (dialogInterface, i) -> {

                })
                .setTitle("Profile QR Code");
        return builder.create();
    }

}
