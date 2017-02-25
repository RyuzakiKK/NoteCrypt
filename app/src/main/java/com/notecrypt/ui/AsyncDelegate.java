package com.notecrypt.ui;

/**
 * Interface for help to synchronize asynchronous processes.
 *
 * @author Ludovico de Nittis
 */
public interface AsyncDelegate {

    /**
     * Called when an asynchronous process need to notify the finish to another process.
     *
     * @param request int representing the request made. The possible requests are from the static fields of DatabaseForNotesAsync
     */
    void asyncComplete(int request); //default is public

}
