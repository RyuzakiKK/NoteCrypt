package com.notecrypt.ui;

import com.notecrypt.utils.CryptoLoad;
import com.notecrypt.utils.StringMethods;
import com.notecryptpro.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


/**
 * Display the custom insert password dialog.
 *
 * @author Ludovico de Nittis
 */
public class InsertPasswordDialogFragment extends DialogFragment {

    private EditText fieldEditText;
    private String path;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.insert_password, (ViewGroup) getView());
        fieldEditText = (EditText) view.findViewById(R.id.field_password);
        path = getArguments().getString("path");
        fieldEditText.requestFocus();

        final AlertDialog passwordDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.title_password)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                final String key = fieldEditText.getText().toString();
                                if (key.equals("")) {
                                    Toast.makeText(getActivity(), R.string.toast_emptyPassword, Toast.LENGTH_LONG).show();
                                } else {
                                    path = StringMethods.getInstance().fixPath(path);
                                    new CryptoLoad.Builder(path, key, getActivity())
                                            .build().execute();
                                }
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                InputMethodManager im = (InputMethodManager) getActivity().getSystemService(getArguments().getString("Context"));
                                im.hideSoftInputFromWindow(fieldEditText.getWindowToken(), 0);
                            }
                        }
                )
                .create();
        passwordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return passwordDialog;
    }
}