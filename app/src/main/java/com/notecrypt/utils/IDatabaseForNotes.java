package com.notecrypt.utils;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface IDatabaseForNotes {

    /**
     * First string to display in the tag filter 'listTag'.
     */
    String ALL_ITEMS = "ALL";
    /**
     * The key in the 'list' for the title.
     */
    String TITLE = "Title";
    /**
     * The key in the 'list' for the position.
     */
    String POSITION = "Position";
    /**
     * Keyword Tags.
     */
    String TAGS = "Tags";
    /**
     * Keyword Note.
     */
    String NOTE = "Note";
    String STAR = "Star";

    /**
     * Returns the notes.
     *
     * @return Map of notes
     */
    Map<Long, Notes> getNotes();

    /**
     * Returns the map with tags and the id of the notes that use them.
     *
     * @return map of tags
     */
    Map<String, TreeSet<Long>> getMapTag();

    /**
     * Returns the 'list' of notes.
     *
     * @return List of id and title for the listview
     */
    List<Map<String, String>> getList();

    /**
     * Returns the 'listTag' of used tags.
     *
     * @return List of tags used for the listview
     */
    List<String> getListTag();

    /**
     * Returns the current position.
     *
     * @return the free position where add a new note
     */
    long getPosition();

    /**
     * Sets the notes.
     *
     * @param notes new notes that need to be set
     */
    void setNotes(Map<Long, Notes> notes);

    /**
     * Need to be called the first time the database is created or when is loaded from a file.
     */
    void initializeLists();

    /**
     * Add a note or edit an existing one at the position 'id'.
     *
     * @param title   title of the note
     * @param note    text of the note
     * @param tags    new tags of the note
     * @param oldTags old tags before the edit
     * @param id      position of the add or edit
     */
    void addNote(String title, String note, String tags, String oldTags, Long id, boolean star);

    /**
     * Delete the note at the position 'id'.
     *
     * @param id      position of the note
     * @param oldTags tags existing in the current note
     */
    void deleteNote(Long id, String oldTags);

    /**
     * Find all notes with the specific tag.
     *
     * @param tag string to search
     * @return Map with all notes that have the specific tag
     */
    Map<Long, Notes> findTag(String tag);
}