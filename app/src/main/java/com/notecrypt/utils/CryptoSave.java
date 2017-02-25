package com.notecrypt.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.notecrypt.ui.AsyncDelegate;
import com.notecryptpro.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Asynchronously save an encrypted file. Use the builder pattern.
 *
 * @author Ludovico de Nittis
 */
public class CryptoSave extends AsyncTask<Void, Integer, Boolean> {

    private final IDatabaseForNotes dbNotes;
    private final String path;
    private final String key;
    private Context mContext;
    private ProgressBar spinner;
    private String toastOK;
    private String toastError;
    private AsyncDelegate delegate;
    private String caller;
    private boolean isInvalidKeyError;

    /**
     * Builder pattern with toast and spinner optional.
     */
    private CryptoSave(final IDatabaseForNotes dbNotes, final String path, final String key, Context mContext) {
        super();
        this.dbNotes = dbNotes;
        this.path = path;
        this.key = key;
        this.mContext = mContext;
    }

    /**
     * Provide a new instance of CryptoSave.
     *
     * @author Ludovico de Nittis
     */
    public static class Builder {
        private final IDatabaseForNotes dbNotes;
        private final String path;
        private final String key;
        private Context mContext;
        private ProgressBar spinner; //null
        private String toastOK; //null
        private String toastError; //null
        private AsyncDelegate delegate; //null
        private String caller;

        /**
         * Build the required fields.
         *
         * @param dbNotes the DatabaseForNotes that need to be saved
         * @param path    where the database need to be saved
         * @param key     password
         */
        public Builder(final IDatabaseForNotes dbNotes, final String path, final String key, Context mContext) {
            this.dbNotes = dbNotes;
            this.path = path;
            this.key = key;
            this.mContext = mContext;
        }

        /**
         * Provide a new instance of CryptoSave.
         *
         * @return instance of CryptoSave
         */
        public CryptoSave build() {
            final CryptoSave result = new CryptoSave(dbNotes, path, key, mContext);
            result.spinner = spinner;
            result.toastOK = toastOK;
            result.toastError = toastError;
            result.delegate = delegate;
            result.caller = caller;
            return result;
        }

        /**
         * Optional builder method for display a toast is there aren't errors.
         *
         * @param toastOK string to display in the toast
         * @return Builder itself
         */
        public Builder toastOK(final String toastOK) {
            this.toastOK = toastOK;
            return this;
        }

        /**
         * Optional builder method for display a custom error toast is there are some errors.
         *
         * @param toastError string to display in the toast
         * @return Builder itself
         */
        public Builder toastError(final String toastError) {
            this.toastError = toastError;
            return this;
        }

        /**
         * Optional builder method for display a spinner until the end of the operations.
         *
         * @param spinner The ProgressBar to display
         * @return Builder itself
         */
        public Builder spinner(final ProgressBar spinner) {
            this.spinner = spinner;
            return this;
        }

        public Builder delegate(final AsyncDelegate delegate, final String caller) {
            this.delegate = delegate;
            this.caller = caller;
            return this;
        }
    }

    @Override
    protected void onPreExecute() {
        if (spinner != null && spinner.getVisibility() != View.VISIBLE) {
            spinner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected Boolean doInBackground(final Void... params) {
        ObjectOutputStream oos = null;
        try {
            // File (or directory) with old name
            File file = new File(path);
            if (file.exists()) {
                // File (or directory) with new name
                String backupPath = path.substring(0, path.lastIndexOf('/') + 1) + "." + path.substring(path.lastIndexOf('/') + 1);
                File file2 = new File(backupPath);
                copyFileUsingFileChannels(file, file2);
                String weeklyBackupPath = path.substring(0, path.lastIndexOf('/') + 1) + ".weekly." + path.substring(path.lastIndexOf('/') + 1);
                File file3 = new File(weeklyBackupPath);
                if (!file3.exists() || (System.currentTimeMillis() - (file3.lastModified()) > 1000 * 3600 * 24 * 7)) {
                    copyFileUsingFileChannels(file, file3);
                }
            }
            byte[] salt = new byte[64];
            try {
                PRNGFixes.apply();
            } catch (Exception e) {
                Log.w("NoteCrypt", e.getMessage());
            }
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(salt);
            oos = new ObjectOutputStream(new FileOutputStream(path));
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream dos = new ObjectOutputStream(baos);
            dos.writeObject(dbNotes);
            final byte[] data = baos.toByteArray();
            byte[] byteKey = Cryptox.getInstance().deriveKey(key, salt);
            final SecretKeySpec secretKey = new SecretKeySpec(byteKey, "AES");
            //Creation of Cipher objects
            final byte[] iv = Cryptox.getInstance().generateIv();
            final IvParameterSpec ivspec = new IvParameterSpec(iv);
            final Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            final byte[] stringbyte = encrypt.doFinal(data);
            //Write the iv on clear text on the file
            oos.writeObject(salt);
            oos.writeObject(iv);
            oos.writeObject(stringbyte);
        } catch (Exception e) {
            if (e instanceof InvalidKeyException) {
                isInvalidKeyError = true;
            }
            return false;
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    try {
                        Toast.makeText(this.mContext, R.string.toast_IOError, Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Log.e("NoteCrypt", "Error with toast");
                    }
                }
            }

        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (spinner != null) {
            spinner.setVisibility(View.GONE);
        }
        if (result) {
            if (toastOK != null) {
                Toast.makeText(this.mContext, toastOK, Toast.LENGTH_LONG).show();
            }
            if (delegate != null && caller.equals("SelectDatabaseActivity")) {
                delegate.asyncComplete(0);
            } else if (delegate != null && caller.equals(("MainActivity"))) {
                delegate.asyncComplete(DatabaseForNotesAsync.REQUEST_CHANGE_PASSWORD);
            }
        } else if (isInvalidKeyError) {
            Toast.makeText(this.mContext, R.string.toast_errorInvalidKey, Toast.LENGTH_LONG).show();
        } else {
            if (toastError == null) {
                Toast.makeText(this.mContext, R.string.toast_IOError, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this.mContext, toastError, Toast.LENGTH_LONG).show();
            }
        }
    }

    private static void copyFileUsingFileChannels(File source, File dest)
            throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }
}