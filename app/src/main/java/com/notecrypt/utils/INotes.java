package com.notecrypt.utils;

interface INotes {

    /**
     * Returns the title of the note.
     *
     * @return
     */
    String getTitle();

    /**
     * Returns the text of the note.
     *
     * @return
     */
    String getNote();

    /**
     * Returns the tags of the note.
     *
     * @return
     */
    String getTags();

    /**
     * Returns if the note is starred.
     *
     * @return
     */
    boolean isStarred();

    /**
     * Sets the title of the note.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Sets the text of the note.
     *
     * @param note
     */
    void setNote(String note);

    /**
     * Sets the tags of the note.
     *
     * @param tags
     */
    void setTags(String tags);

    /**
     * Sets the starred value of the note.
     *
     * @param starred
     */
    void setStarred(boolean starred);
}