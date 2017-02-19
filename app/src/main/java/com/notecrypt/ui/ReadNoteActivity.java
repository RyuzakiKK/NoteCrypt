package com.notecrypt.ui;

import com.notecrypt.app.App;
import com.notecrypt.utils.DatabaseForNotesAsync;
import com.notecrypt.utils.IDatabaseForNotes;
import com.notecryptpro.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Activity that display the selected note.
 * @author Ludovico de Nittis
 *
 */
public class ReadNoteActivity extends AppCompatActivity {

	private int listViewLocation;
	private long location;
	private IDatabaseForNotes db;
	private Handler mHandler;
	private Runnable r;
	private boolean isReadNoteActivityForeground = true;
	private boolean thisActivityStartedCount = false;
	private MenuItem starred;
	private boolean isStarred;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_note);
		//enable the ability to press the title as back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final Intent intent = getIntent();
		listViewLocation = intent.getIntExtra("listViewLocation", -1);
		location = Long.parseLong(intent.getStringExtra("location"));
        db = App.getDatabase();
		setTitle(db.getNotes().get(location).getTitle());
		setSize();
		final String textNote = db.getNotes().get(location).getNote();
		isStarred = db.getNotes().get(location).isStarred();
		//If the text of the note is empty a message will be displayed
		if (textNote.equals("")) {
			final TextView textViewNoNote = (TextView) findViewById(R.id.textViewNoNote);
			textViewNoNote.setText(getString(R.string.no_textNote));
		} else {
			final TextView textViewNote = (TextView) findViewById(R.id.textViewNote);
			textViewNote.setText(db.getNotes().get(location).getNote());
		}
		mHandler = new Handler();
		r = new Runnable() {
			@Override
			public void run() {
				if (App.isInForeground()) {
					if (thisActivityStartedCount) {
						App.setIsAnActivityCounting(false);
						thisActivityStartedCount = false;
					}
				} else if (!App.isInForeground() && ((!App.isAnActivityCounting()) || thisActivityStartedCount)) {
					App.setIsAnActivityCounting(true);
					thisActivityStartedCount = true;
					App.incTimesInBackground();
				}
				if (App.getTimesInBackground() >= MainActivity.MAX_TIMES_BACKGROUND + 1) {
					ReadNoteActivity.this.finish();
				} else if (!isReadNoteActivityForeground) { //Check in loop until the user change app or return to this activity
					mHandler.postDelayed(r, MainActivity.TIMEOUT_SPLITTED);
				}
			}
		};
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		// if is pressed the back button on the top left corner
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_starred:
			final Intent intent = getIntent();
			if (isStarred) {
				intent.putExtra(IDatabaseForNotes.STAR, false);
				starred.setIcon(R.drawable.ic_star_outline_white_24dp);
			} else {
				intent.putExtra(IDatabaseForNotes.STAR, true);
				starred.setIcon(R.drawable.ic_star_white_24dp);
			}
            isStarred = !isStarred;
			intent.putExtra(IDatabaseForNotes.TITLE, db.getNotes().get(location).getTitle());
			intent.putExtra(IDatabaseForNotes.TAGS, db.getNotes().get(location).getTags());
            App.setNote(db.getNotes().get(location).getNote());
			intent.putExtra("location", location);
			intent.putExtra("listViewLocation", listViewLocation);
			setResult(MainActivity.RESULT_EDIT, intent);
			return true;
		case R.id.action_edit:
			editNote();
			return true;
		case R.id.action_delete:
			deleteNote();
			return true;
        case R.id.action_share:
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, db.getNotes().get(location).getTitle());
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, db.getNotes().get(location).getNote());
            startActivity(Intent.createChooser(sharingIntent, "Share"));
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.read_note, menu);
		starred = menu.findItem(R.id.action_starred);
		if (db.getNotes().get(location).isStarred()) {
			starred.setIcon(R.drawable.ic_star_white_24dp);
		} else {
			starred.setIcon(R.drawable.ic_star_outline_white_24dp);
		}
		return true;
	}

	/**
	 * Open the EditNoteActivity and save in the extra the current title, tags and note.
	 */
	private void editNote() {
		final Intent intent = new Intent(ReadNoteActivity.this, EditNoteActivity.class);
		intent.putExtra("Title", db.getNotes().get(location).getTitle());
		intent.putExtra("Tags", db.getNotes().get(location).getTags());
        App.setNote(db.getNotes().get(location).getNote());
		intent.putExtra("TagList", db.getMapTag().keySet().toArray(new String[db.getMapTag().keySet().size()]));
		startActivityForResult(intent, DatabaseForNotesAsync.REQUEST_EDIT_NOTE);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		//If the note was edited
		if (requestCode == DatabaseForNotesAsync.REQUEST_EDIT_NOTE && resultCode == RESULT_OK) {
			final Intent intent = getIntent();
			intent.putExtra(IDatabaseForNotes.TITLE, data.getStringExtra(IDatabaseForNotes.TITLE));
			intent.putExtra(IDatabaseForNotes.TAGS, data.getStringExtra(IDatabaseForNotes.TAGS));
            //App.setNote(data.getStringExtra(IDatabaseForNotes.NOTE));
			//intent.putExtra(IDatabaseForNotes.NOTE, data.getStringExtra(IDatabaseForNotes.NOTE));
			intent.putExtra("location", location);
			intent.putExtra("listViewLocation", listViewLocation);
			if (isStarred) {
				intent.putExtra(IDatabaseForNotes.STAR, true);
			} else {
				intent.putExtra(IDatabaseForNotes.STAR, false);
			}
			setResult(MainActivity.RESULT_EDIT, intent);
			finish();
		}
	}
	/**
	 * Display a confirmation dialog, and if the user press the positive button this activity will
	 * terminate with the result RESULT_DELETE. 
	 */
	private void deleteNote() {
		// display confirmation dialog
		new AlertDialog.Builder(this)
		.setTitle(R.string.title_deleteNote)
		.setMessage(R.string.dialog_deleteNote)
		.setCancelable(true)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				final Intent intent = getIntent();
				intent.putExtra("location", location);
				intent.putExtra("listViewLocation", listViewLocation);
				setResult(MainActivity.RESULT_DELETE, intent);
				finish();
			}
		})
		.setNegativeButton(R.string.no, null)
		.show();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		App.setIsInForeground(true);
	}
	
	@Override
	protected void onPause() {
		App.setIsInForeground(false);
		isReadNoteActivityForeground = false;
		mHandler.postDelayed(r, MainActivity.TIMEOUT_SPLITTED);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		App.setIsInForeground(true);
		isReadNoteActivityForeground = true;
	}
	
	private void setSize() {
		int prefDrawer = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_textSize", "20"));
		final TextView textViewNoNote = (TextView) findViewById(R.id.textViewNote);
		textViewNoNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefDrawer);
	}

	@Override
	protected void onDestroy() {
		mHandler.removeCallbacks(r);
		if (thisActivityStartedCount) {
			App.setIsAnActivityCounting(false);
		}
		super.onDestroy();
	}
}