package com.notecrypt.utils;

import java.io.File;

import android.os.Environment;

/**
 * Singleton class eager initialization that provide common methods for database.
 * @author Ludovico de Nittis
 *
 */
public final class StringMethods {

	public static final String DEFAULT_FOLDER = "NoteCrypt";
	private static final String EXTENSION = ".ncf";
	private static StringMethods singleton = new StringMethods();
	
	private StringMethods() {

	}
	
	/**
	 * Returns the singleton.
	 * @return singleton of StringMethods
	 */
	public static StringMethods getInstance() {
		return singleton;
	}

	/**
	 * If the parameter is a relative path (eg. with only the name of the db) will be returned the absolute path with a start point in the sd card.
	 * @param path relative or absolute path
	 * @return the absolute path
	 */
	public String fixPath(final String path) {
		if (path.length() > 0 && path.charAt(0) != '/') {
			final String extStore = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DEFAULT_FOLDER;
			final File f = new File(extStore);
			if (!f.exists() || !f.isDirectory()) {
				f.mkdirs();
			}
			return extStore + '/' + path;
		}
		return path;
	}
	/**
	 * If the path do not end with 'EXTENSION' will be returned 'path + EXTENSION'.
	 * @param path the path of the object
	 * @return path with the EXTENSION
	 */
	public String checkExtension(final String path) {
		if (path.equals("") || path.length() < EXTENSION.length() ||
				!path.substring(path.length() - EXTENSION.length(), path.length()).equals(EXTENSION)) {
			return path + EXTENSION;
		}
		return path;
	}
	/**
	 * Returns the name of the database from the absolute path.
	 * @param path absolute path of the database
	 * @return the name of the database without the EXTENSION
	 */
	public String getNameDB(final String path) {
		if (path.length() < EXTENSION.length() ||
				!path.substring(path.length() - EXTENSION.length(), path.length()).equals(EXTENSION)) {
			return path.substring(path.lastIndexOf('/') + 1, path.length());
		}
		return path.substring(path.lastIndexOf('/') + 1, path.length() - EXTENSION.length());
	}

    public static boolean containsIgnoreCase(String str, String searchStr) {
        if(str != null && searchStr != null) {
            int len = searchStr.length();
            int max = str.length() - len;
            for(int i = 0; i <= max; ++i) {
                if(str.regionMatches(true, i, searchStr, 0, len)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
}
