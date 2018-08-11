package com.notecrypt.ui;

import com.notecrypt.utils.CryptoSave;
import com.notecrypt.utils.IDatabaseForNotes;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Objects;


/**
 * Display the custom choose password dialog.
 *
 * @author Ludovico de Nittis
 */
public class ChoosePasswordDialogFragment extends DialogFragment {

    private AsyncDelegate delegate;
    private EditText fieldEditText;
    private EditText confFieldEditText;
    private ProgressBar spinner;
    private String path;
    private IDatabaseForNotes db;
    private String toastOK;

    /*
     * 	int toastOK: id of the toast to display if the dialog have a positive result
     * 	(optional) Boolean viewTextWarning: if true the dialog also display a warning message
     *
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.new_password, (ViewGroup) getView());
        fieldEditText = view.findViewById(R.id.field_password);
        confFieldEditText = view.findViewById(R.id.conf_field_password);
        spinner = view.findViewById(R.id.progressBar1);
        path = getArguments().getString("path");
        final String caller = getArguments().getString("caller");
        if (caller != null && caller.equals("SelectDatabaseActivity")) {
            delegate = (SelectDatabaseActivity) getActivity();
        } else {
            delegate = (MainActivity) getActivity();
        }
        db = (IDatabaseForNotes) getArguments().getSerializable("db");
        toastOK = getString(getArguments().getInt("toastOK"));
        final AlertDialog passwordDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.title_password)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                final String key = checkPasswords();
                                if (key != null && path != null) {
                                    path = StringMethods.getInstance().fixPath(path);
                                    new CryptoSave.Builder(db, path, key, getActivity())
                                            .toastOK(toastOK)
                                            .toastError(getString(R.string.toast_errorCreatingDB))
                                            .spinner(spinner)
                                            .delegate(delegate, caller)
                                            .build().execute();
                                }
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                try {
                                    InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Objects.requireNonNull(getArguments().getString("Context")));
                                    Objects.requireNonNull(im).hideSoftInputFromWindow(fieldEditText.getWindowToken(), 0);
                                } catch (NullPointerException npe) {
                                    // Something bad happened but preventing the app from crashing
                                    // should be fine. Maybe we should log this event.
                                }
                            }
                        }
                )
                .create();
        try {
            Objects.requireNonNull(passwordDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } catch (NullPointerException npe) {
            // Something bad happened but preventing the app from crashing
            // should be fine. Maybe we should log this event.
        }
        return passwordDialog;
    }

    /**
     * Check the two passwords inserted.
     *
     * @return the password inserted or null if there is an error
     */
    private String checkPasswords() {
        if (fieldEditText.getText().toString().equals("") || confFieldEditText.getText().toString().equals("")) {
            Toast.makeText(getActivity(), R.string.toast_fieldsNotFilled, Toast.LENGTH_LONG).show();
            return null;
        } else if (fieldEditText.getText().toString().equals(confFieldEditText.getText().toString())) {
            return fieldEditText.getText().toString();
        }
        Toast.makeText(getActivity(), R.string.toast_wrongNewPassword, Toast.LENGTH_LONG).show();
        return null;
    }
}