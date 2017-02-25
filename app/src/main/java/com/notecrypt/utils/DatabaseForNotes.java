package com.notecrypt.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Database with fields for store notes.
 *
 * @author Ludovico de Nittis
 */
public class DatabaseForNotes implements java.io.Serializable, IDatabaseForNotes {
    private static final int INITIAL_NOTES = 64;
    private static final int INITIAL_TAG = 32;
    private static final long serialVersionUID = 12321L;
    private transient List<Map<String, String>> list;
    private transient List<String> listTag; //DEPRECATED, need to be deleted?
    private long position; // 0 by default
    private Map<Long, Notes> notes = new HashMap<>(INITIAL_NOTES);
    private final Map<String, TreeSet<Long>> mapTag = new HashMap<>(INITIAL_TAG);

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#getNotes()
     */
    @Override
    public Map<Long, Notes> getNotes() {
        return this.notes;
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#getMapTag()
     */
    @Override
    public Map<String, TreeSet<Long>> getMapTag() {
        return this.mapTag;
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#getList()
     */
    @Override
    public List<Map<String, String>> getList() {
        return this.list;
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#getListTag()
     */
    @Override
    public List<String> getListTag() { //DEPRECATED, need to be removed?
        return this.listTag;
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#getPosition()
     */
    @Override
    public long getPosition() {
        return this.position;
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#setNotes(java.util.Map)
     */
    @Override
    public void setNotes(final Map<Long, Notes> notes) {
        this.notes = notes;
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#initializeLists()
     */
    @Override
    public void initializeLists() {
        if (list == null) {
            list = new ArrayList<>();
        }
        if (listTag == null) {
            listTag = new LinkedList<>();
        }
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#addNote(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public void addNote(final String title, final String note, final String tags, final String oldTags, final Long id, final boolean star) {
        notes.put(id, new Notes(title, note));
        notes.get(id).setStarred(star);
        String localOldTags = oldTags;
        notes.get(id).setTags(tags);
        // If there are some change
        if (!tags.equals(localOldTags)) {
            if (localOldTags == null) { //avoid nullPointer exception
                localOldTags = "";
            }
            String[] tagsArray = tags.split(",");
            String[] oldTagsArray = localOldTags.split(",");
            for (int i = 0; i < tagsArray.length; i++) {
                tagsArray[i] = tagsArray[i].trim().toLowerCase(Locale.ENGLISH);
            }
            for (int i = 0; i < oldTagsArray.length; i++) {
                oldTagsArray[i] = oldTagsArray[i].trim().toLowerCase(Locale.ENGLISH);
            }
            //Create a Set for avoid problems with duplicated tags (if there are 2 or more equals tags on the same note)
            final Set<String> tagsSet = new HashSet<>(Arrays.asList(tagsArray));
            final Set<String> oldTagsSet = new HashSet<>(Arrays.asList(oldTagsArray));
            // Delete all removed tags
            for (final String tag : oldTagsSet) {
                if (!localOldTags.equals("") && !tagsSet.contains(tag)) {
                    removeTag(id, tag);
                }
            }
            // Add all new tags
            for (final String string : tagsSet) {
                //if this is a new tag and isn't empty
                if (!oldTagsSet.contains(string) && !string.equals("")) {
                    TreeSet<Long> tempSet = mapTag.get(string);
                    if (tempSet == null) {
                        tempSet = new TreeSet<>();
                    }
                    tempSet.add(id);
                    mapTag.put(string, tempSet);
                }
            }
        }
        if (id == position) { //if this is a new add and not an edit of an existing note
            position++;
        }
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#deleteNote(java.lang.Long, java.lang.String)
     */
    @Override
    public void deleteNote(final Long id, final String oldTags) {
        notes.remove(id);
        if (oldTags != null) {
            final String[] oldTagsArray = oldTags.split(",");
            for (int i = 0; i < oldTagsArray.length; i++) {
                oldTagsArray[i] = oldTagsArray[i].trim().toLowerCase(Locale.ENGLISH);
            }
            final Set<String> oldTagsSet = new HashSet<>(Arrays.asList(oldTagsArray));
            for (final String tag : oldTagsSet) {
                if (!tag.equals("")) {
                    removeTag(id, tag);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.notecrypt.utils.IDatabaseForNotes#findTag(java.lang.String)
     */
    @Override
    public Map<Long, Notes> findTag(final String tag) {
        final TreeSet<Long> tempSet = mapTag.get(tag);
        final HashMap<Long, Notes> tempNotes = new HashMap<>(notes);
        tempNotes.keySet().retainAll(tempSet);
        return tempNotes;
    }

    /**
     * Check if the note is visible with the current filter.
     *
     * @param filter current filter on the view
     * @param tags   tags used on the current note
     * @return true if the note is visible on the current view
     */
    static boolean isNoteVisible(final String filter, final String tags) {
        final String[] tagsArray = tags.split(",");
        for (int i = 0; i < tagsArray.length; i++) {
            tagsArray[i] = tagsArray[i].trim().toLowerCase(Locale.ENGLISH);
        }
        for (final String string : tagsArray) {
            if (filter.equals(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Insert the position and title information.
     *
     * @param position position of the element to insert
     * @param title    title of the element
     * @return Map with the provided information
     */
    static Map<String, String> putData(final String position, final String title, final boolean star) {
        final HashMap<String, String> item = new HashMap<>();
        item.put(POSITION, position);
        item.put(TITLE, title);
        item.put(STAR, String.valueOf(star));
        return item;
    }

    /**
     * Remove a tag from 'mapTag'.
     *
     * @param id  id of the tag
     * @param tag String of the tag
     */
    private void removeTag(final Long id, final String tag) {
        final TreeSet<Long> tempSet = mapTag.get(tag);
        tempSet.remove(id);
        if (tempSet.isEmpty()) {
            mapTag.remove(tag);
        } else {
            mapTag.put(tag, tempSet);
        }
    }
}