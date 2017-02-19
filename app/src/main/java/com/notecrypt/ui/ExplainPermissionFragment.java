package com.notecrypt.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.notecryptpro.R;

public class ExplainPermissionFragment extends DialogFragment {
    private final static String APP_TITLE = "NoteCrypt";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(APP_TITLE)
                .setMessage(R.string.explain_external_permission)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).create();
    }
}
