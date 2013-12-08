package net.cs50.recipes;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;

public class CreateDialog extends DialogFragment implements OnClickListener {

    public static final String TAG = "create_dialog";

    public enum Action {
        IMAGE_CAPTURE, IMAGE_SELECT
    }

    private Action selected = Action.IMAGE_CAPTURE;

    private File imageFile;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_create_title)
                .setSingleChoiceItems(R.array.dialog_create_actions, 0, this)
                .setPositiveButton(R.string.ok, this).setNegativeButton(R.string.cancel, this);

        Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            onOK();
            break;
        default:
            selected = Action.values()[which];
            break;
        }
    }

    private void onOK() {
        switch (selected) {
        case IMAGE_CAPTURE:
            showCamera();
            break;
        case IMAGE_SELECT:
            showGallery();
            break;
        }
    }

    private static File getCameraDir() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File cameraDir = new File(dcim, String.format("100NOM/%s", "Recipes"));
            if (!cameraDir.exists()) {
                cameraDir.mkdirs();
            }
            return cameraDir;
        }
        return null;
    }

    private void showCamera() {
        File cameraDir = getCameraDir();
        long time = System.currentTimeMillis();
        imageFile = new File(cameraDir, String.format("IMG-%d.jpg", time));

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, selected.ordinal());
    }

    private void showGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, selected.ordinal());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getActivity(), CreateActivity.class);
            switch (CreateDialog.Action.values()[requestCode]) {
            case IMAGE_CAPTURE:
                intent.setData(Uri.fromFile(imageFile));
                break;
            case IMAGE_SELECT:
                intent.setData(data.getData());
                break;
            }
            startActivity(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
