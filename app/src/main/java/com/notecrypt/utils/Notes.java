package com.notecrypt.utils;

/**
 * Defines a note.
 *
 * @author Ludovico de Nittis
 */
public class Notes implements java.io.Serializable, INotes {

    private static final long serialVersionUID = 1212L;
    private String title;
    private String note;
    private String tags;
    private boolean starred;

    /**
     * Creates a new note with the parameters provided in input.
     *
     * @param title title for the note
     * @param note  text of the note
     */
    Notes(final String title, final String note) {
        super();
        this.title = title;
        this.note = note;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public String getTags() {
        return tags;
    }

    @Override
    public boolean isStarred() {
        return starred;
    }

    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public void setNote(final String note) {
        this.note = note;
    }

    @Override
    public void setTags(final String tags) {
        this.tags = tags;
    }

    @Override
    public void setStarred(boolean starred) {
        this.starred = starred;
    }
}
