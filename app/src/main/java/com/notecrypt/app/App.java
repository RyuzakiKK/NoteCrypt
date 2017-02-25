package com.notecrypt.app;

import android.app.Application;

import com.notecrypt.utils.DatabaseForNotes;


public class App extends Application {

    private static boolean isInForeground = true;
    private static int timesInBackground; //0 by default
    private static boolean isAnActivityCounting; //false by default
    private static DatabaseForNotes db;
    private static String singleNote;

    public static int getTimesInBackground() {
        return timesInBackground;
    }

    public static boolean isInForeground() {
        return isInForeground;
    }

    public static boolean isAnActivityCounting() {
        return isAnActivityCounting;
    }

    public static void setIsAnActivityCounting(final boolean newState) {
        isAnActivityCounting = newState;
    }

    public static void setIsInForeground(final boolean newState) {
        isInForeground = newState;
        timesInBackground = 0;
    }

    public static void incTimesInBackground() {
        timesInBackground++;
    }

    public static void setDatabase(DatabaseForNotes database) {
        db = database;
    }

    public static DatabaseForNotes getDatabase() {
        return db;
    }

    public static void setNote(String note) {
        singleNote = note;
    }

    public static String getNote() {
        return singleNote;
    }
}