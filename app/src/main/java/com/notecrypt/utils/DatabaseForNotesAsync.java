package com.notecrypt.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.notecrypt.ui.AsyncDelegate;

import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * Update DatabaseForNotes with asynchronous process.
 * @author Ludovico de Nittis
 *
 */
public class DatabaseForNotesAsync extends AsyncTask<Void, Integer, Void> {
	
	/**
	 * Constant that represent the build request.
	 */
	public static final int REQUEST_BUILD_LISTS = 1;
	/**
	 * Constant that represent the new note request.
	 */
	public static final int REQUEST_NEW_NOTE = 2;
	/**
	 * Constant that represent the delete request.
	 */
	public static final int REQUEST_DELETE_NOTE = 3;
	/**
	 * Constant that represent the edit request.
	 */
	public static final int REQUEST_EDIT_NOTE = 4;
    /**
     * Constant that represent the change password request.
     */
    public static final int REQUEST_CHANGE_PASSWORD = 5;
	
	private final AsyncDelegate delegate;
	private boolean isOnlyTag;
	private String filter;
	private final int request;
	private String title;
	private String tags;
	private String note;
	private long location;
	private int listViewLocation;
	private final IDatabaseForNotes db;
	private boolean star;
	private ListView listview;
	private ListView mDrawerList;
	private ArrayList<String> arrayTagList;

	/**
	 * Builder pattern
	 */
	private DatabaseForNotesAsync(final AsyncDelegate delegate, final int request, final IDatabaseForNotes db) {
		super();
		this.delegate = delegate;
		this.request = request;
		this.db = db;
	}

	/**
	 * Provide a new instance of DatabaseForNotesAsync.
	 * @author Ludovico de Nittis
	 */
	public static class Builder {
		private final AsyncDelegate delegate;
		private String filter;
		private final int request;
		private String title;
		private String tags;
		private String note;
		private long location;
		private int listViewLocation;
		private final IDatabaseForNotes db;
		private boolean star;
		private ListView listview;
		private ListView mDrawerList;
		private ArrayList<String> arrayTagList;

		/**
		 * Build the required fields.
		 * @param delegate AsyncDelegate for call a specific method (eg. at the end of the async process)
		 * @param request int representing the request made. The possible requests are from the static fields of DatabaseForNotesAsync
		 * @param db the DatabaseForNotes that need to be manipulated
		 */
		public Builder(final AsyncDelegate delegate, final int request, final IDatabaseForNotes db, ArrayList<String> arrayTagList,
				ListView listview, ListView mDrawerList) {
			this.delegate = delegate;
			this.request = request;
			this.db = db;
			this.arrayTagList = arrayTagList;
			this.listview = listview;
			this.mDrawerList = mDrawerList;
		}

		/**
		 * Provide a new instance of DatabaseForNotesAsync.
		 * @return instance of DatabaseForNotesAsync
		 */
		public DatabaseForNotesAsync build() {
			final DatabaseForNotesAsync result = new DatabaseForNotesAsync(delegate, request, db);
			result.filter = filter;
			result.title = title;
			result.tags = tags;
			result.note = note;
			result.location = location;
			result.listViewLocation = listViewLocation;
			result.star = star;
			result.listview = listview;
			result.mDrawerList = mDrawerList;
			result.arrayTagList = arrayTagList;
			return result;
		}

		/**
		 * Builder method needed by REQUEST_NEW_NOTE and REQUEST_EDIT_NOTE.
		 * @param title title of the note
		 * @param tags tags of the note
		 * @param note text of the note
		 * @return Builder itself
		 */
		public Builder strings(final String title, final String tags, final String note, final boolean star) {
			this.title = title;
			this.tags = tags;
			this.note = note;
			this.star = star;
			return this;
		}

		/**
		 * Builder method needed by REQUEST_DELETE_NOTE and REQUEST_EDIT_NOTE.
		 * @param location id of the location of the note on the notes Map of DatabaseForNotes
		 * @param listViewLocation id of the location of the note on 'list' of DatabaseForNotes representing the current listview
		 * @return Builder itself
		 */
		public Builder locations(final long location, final int listViewLocation) {
			this.location = location;
			this.listViewLocation = listViewLocation;
			return this;
		}

		/**
		 * Optional builder method. Used only if the current listview is filtered by tag.
		 * @param filter the tag filter current used
		 * @return Builder itself
		 */
		public Builder filter(final String filter) {
			this.filter = filter;
			return this;
		}
	}

	@Override
	protected Void doInBackground(final Void... params) {
		if (request == REQUEST_NEW_NOTE) {
			db.addNote(title, note, tags, "", db.getPosition(), false);
			if (filter == null || DatabaseForNotes.isNoteVisible(filter, tags)) {
				// position - 1 because the position number was already incremented with the add
				db.getList().add(DatabaseForNotes.putData(Long.toString(db.getPosition() - 1), title, false));
			}
			isOnlyTag = true; //update the tag list
		} else if (request == REQUEST_DELETE_NOTE) {
			db.deleteNote(location, db.getNotes().get(location).getTags());
			db.getList().remove(listViewLocation);
			isOnlyTag = true; //update the tag list
		} else if (request == REQUEST_EDIT_NOTE) {
			final String oldTags = db.getNotes().get(location).getTags();
			db.addNote(title, note, tags, oldTags, location, star);
			if (filter == null || DatabaseForNotes.isNoteVisible(filter, tags)) { //if is visible
				db.getList().set(listViewLocation, DatabaseForNotes.putData(Long.toString(location), title, star));
			} else { //if the note is no more visible in the current filter
				db.getList().remove(listViewLocation);
			}
			isOnlyTag = true; //update the tag list
		}

		//if need to update also the notes list
		if (!isOnlyTag) {
			db.getList().clear();
			publishProgress(1);
            Map<Long, Notes> tempTag;
			if (filter == null) { //no params = the whole db 
				tempTag = db.getNotes();
			} else {
				tempTag = db.findTag(filter);
			}
			List<Map<String, String>> listNotesTemp = new LinkedList<>();
			for (final Long key : tempTag.keySet()) {
				listNotesTemp.add(DatabaseForNotes.putData(Long.toString(key), tempTag.get(key).getTitle(), tempTag.get(key).isStarred()));
			}
			db.getList().addAll(listNotesTemp);
			publishProgress(1);
		}
		arrayTagList.clear();
		publishProgress(2);
		List<String> tagListTemp = new LinkedList<>();
		for (final String tag : db.getMapTag().keySet()) {
			tagListTemp.add(tag + " (" + db.getMapTag().get(tag).size() + ")");
		}
		Collections.sort(tagListTemp);
		tagListTemp.add(0, IDatabaseForNotes.ALL_ITEMS + " (" + db.getNotes().size() + ")");
		arrayTagList.addAll(tagListTemp);
		publishProgress(2);
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] == 1) {
		((BaseAdapter) listview.getAdapter()).notifyDataSetChanged();
		} else {
		((BaseAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
		}
	}

	/* OnPostExecute tell with delegate that the operations ended
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(final Void result) {
		delegate.asyncComplete(request);
		super.onPostExecute(result);
	}
}