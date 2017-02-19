package com.notecrypt.ui;

import com.notecryptpro.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

/**
 * Singleton class lazy initialization that provide a method for display the about toast.
 * @author Ludovico de Nittis
 *
 */
final class AboutToast {

	private AboutToast() {
	}
	
	private static class LazyHolder {
        private static final AboutToast INSTANCE = new AboutToast();
    }
	
	/**
	 * Returns the singleton.
	 * @return singleton of Crypto
	 */
    public static AboutToast getInstance() {
        return LazyHolder.INSTANCE;
    }
	
	/**
	 * Display a Toast with information about the application.
	 * @param packageManager usually retrieved by getPackageManager()
	 * @param packageName usually retrieved by getPackageName()
	 */
    void createAboutToast(final PackageManager packageManager, final String packageName, final Toast toast, final Context mContext) {
		try {
			final PackageInfo pInfo = packageManager.getPackageInfo(packageName, 0);
			toast.setText(mContext.getString(R.string.toast_version) + " " + pInfo.versionName
					+ mContext.getString(R.string.toast_createdBy));
			toast.show();
		} catch (NameNotFoundException e) {
			toast.setText(R.string.toast_errorVersion);
			toast.show();
		}
	}
}
