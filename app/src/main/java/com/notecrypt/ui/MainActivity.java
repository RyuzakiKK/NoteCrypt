package com.notecrypt.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import com.notecrypt.app.App;
import com.notecrypt.utils.CryptoSave;
import com.notecrypt.utils.DatabaseForNotes;
import com.notecrypt.utils.DatabaseForNotesAsync;
import com.notecrypt.utils.IDatabaseForNotes;
import com.notecrypt.utils.StringMethods;
import com.notecryptpro.R;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

/**
 * Main Activity where the user is able to view, create and edit notes. 
 * @author Ludovico de Nittis
 *
 */
public class MainActivity extends AppCompatActivity implements AsyncDelegate {

	/**
	 * Constant that represent the delete action.
	 */
	public static final int RESULT_DELETE = -2;
	/**
	 * Constant that represent the edit action.
	 */
	public static final int RESULT_EDIT = -3;
	/**
	 * Timeout until close
	 */
	public static final int TIMEOUT_SPLITTED = 45000;
	/**
	 * Rounds of timeouts
	 */
	public static final int MAX_TIMES_BACKGROUND = 7;

	private DatabaseForNotes db;
	private String key;
	private String path;
	private ListView listview;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String filterSelected;
	private ProgressBar spinner;
	private Toast toast0; // This toast will prevent a long queue of toast
	private boolean isFirstBuild = true;
	private boolean isMainActivityForeground = true;
	private MenuItem searchMenuItem;
	private Handler mHandler;
	private Runnable r;
	private boolean thisActivityStartedCount = false;
	private int mDrawerListItemSelected = 0;
	private ArrayList<String> arrayTagList;

    @Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toast0 = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_LONG);
		spinner = (ProgressBar) findViewById(R.id.progressBar2);
		spinner.setVisibility(View.VISIBLE);
		final Intent intent = getIntent();
		path = intent.getStringExtra("path");
		setTitle(StringMethods.getInstance().getNameDB(path));
		key = intent.getStringExtra("key");
        db = App.getDatabase();
		listview = (ListView) findViewById(android.R.id.list);
		final String[] from = {IDatabaseForNotes.TITLE, IDatabaseForNotes.STAR};
		final int[] to = {R.id.txt, R.id.star};
		db.initializeLists();
		final CustomAdapter adapter = new CustomAdapter(this, db.getList(), R.layout.listview_layout, from, to);
		listview.setAdapter(adapter);
		((BaseAdapter) listview.getAdapter()).notifyDataSetChanged();
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				final Intent intent = new Intent(MainActivity.this, ReadNoteActivity.class);
				intent.putExtra("listViewLocation", position);
				intent.putExtra("location", db.getList().get(position).get(IDatabaseForNotes.POSITION));
                App.setDatabase(db);
				intent.putExtra("TagList", db.getMapTag().keySet().toArray(new String[db.getMapTag().keySet().size()]));
				startActivityForResult(intent, DatabaseForNotesAsync.REQUEST_EDIT_NOTE);
			}
		});
		listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.title_deleteNote)
				.setMessage(R.string.dialog_deleteNote)
				.setCancelable(true)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						final Intent intent = getIntent();
						intent.putExtra("listViewLocation", position);
						intent.putExtra("location", Long.parseLong(db.getList().get(position).get(IDatabaseForNotes.POSITION)));
						onActivityResult(DatabaseForNotesAsync.REQUEST_EDIT_NOTE, MainActivity.RESULT_DELETE, intent);
					}
				})
				.setNegativeButton(R.string.no, null)
				.show();
				return true;
			}
		});
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		arrayTagList = new ArrayList<>();
		mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, arrayTagList));
		((BaseAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
		new DatabaseForNotesAsync.Builder(this, DatabaseForNotesAsync.REQUEST_BUILD_LISTS, db, arrayTagList, listview, mDrawerList).build().execute();
		//select the filter 'ALL'
		mDrawerList.setItemChecked(0, true);
		((BaseAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
		final AsyncDelegate asyncDelegate = this;
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				if (position == 0) { //if is ALL_ITEMS
					filterSelected = null;
					setTitle(StringMethods.getInstance().getNameDB(path));
				} else {
					filterSelected = (String) parent.getItemAtPosition(position);
					filterSelected = filterSelected.substring(0, filterSelected.lastIndexOf('(') - 1);
					setTitle(filterSelected);
				}
				mDrawerListItemSelected = position;
				spinner.setVisibility(View.VISIBLE);
				new DatabaseForNotesAsync.Builder(asyncDelegate, DatabaseForNotesAsync.REQUEST_BUILD_LISTS, db, arrayTagList, listview, mDrawerList)
				.filter(filterSelected)
				.build().execute();
				mDrawerLayout.closeDrawer(GravityCompat.END);
			}
		});
		mHandler = new Handler();
		r = new Runnable() {
			@Override
			public void run() { 
				if (App.isInForeground()) {
					if (thisActivityStartedCount) {
						App.setIsAnActivityCounting(false);
						thisActivityStartedCount = false;
					}
				} else if (!App.isInForeground() && (!App.isAnActivityCounting() || thisActivityStartedCount)) {
					App.setIsAnActivityCounting(true);
					thisActivityStartedCount = true;
					App.incTimesInBackground();
				}
				if (App.getTimesInBackground() >= MainActivity.MAX_TIMES_BACKGROUND + 3) {
					MainActivity.this.finish();
				} else if (!isMainActivityForeground) { //Check in loop until the user change app or return to this activity
					mHandler.postDelayed(r, MainActivity.TIMEOUT_SPLITTED);
				}
			}
		};
	}

	/*
	 * When the back button is pressed close the drawer layout if it was open or display an alert dialog
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
			mDrawerLayout.closeDrawer(GravityCompat.END);
		} else if (mDrawerListItemSelected != 0) {
			mDrawerList.setSoundEffectsEnabled(false);
			mDrawerList.performItemClick(mDrawerList, 0, mDrawerList.getItemIdAtPosition(0));
			mDrawerList.setSoundEffectsEnabled(true);
		} else {
			new AlertDialog.Builder(this)
			.setMessage(R.string.dialog_backClose)
			.setTitle(R.string.title_backClose)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					finish();
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.database_opened, menu);
		getMenuInflater().inflate(R.menu.main, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchMenuItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchMenuItem.getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocusFromTouch();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_filter:
			if (db.getList().size() != 0) {
				actionDrawer();
			}
			return true;
		case R.id.action_sort:
			if (getPreferences(MODE_PRIVATE).getBoolean("isAlphabeticalOrder", false)) {
				getPreferences(MODE_PRIVATE).edit().putBoolean("isAlphabeticalOrder", false).apply();
			} else {
				getPreferences(MODE_PRIVATE).edit().putBoolean("isAlphabeticalOrder", true).apply();
			}
			actionSort(true);
			return true;
		case R.id.action_changePassword:
			changePassword();
			return true;
		case R.id.action_settings:
			startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			return true;
		case R.id.action_about:
			AboutToast.getInstance().createAboutToast(getPackageManager(), getPackageName(), toast0, getApplicationContext());
			return true;
        case R.id.action_search:
            InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(listview.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Open the drawer if it was close or vice versa.
	 */
	private void actionDrawer() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
			mDrawerLayout.closeDrawer(GravityCompat.END);
		} else {
			mDrawerLayout.openDrawer(GravityCompat.END);
		}
	}
	/**
	 * Sort the list of notes by alphabet or creation.
	 * @param displayToast if you need to display a toast
	 */
	private void actionSort(boolean displayToast) {
		spinner.setVisibility(View.VISIBLE);
		if (getPreferences(MODE_PRIVATE).getBoolean("isAlphabeticalOrder", false)) {
			Collections.sort(db.getList(), new Comparator<Map<String, String>>() {

				@Override
				public int compare(final Map<String, String> lhs, final Map<String, String> rhs) {
					final int a = lhs.get(IDatabaseForNotes.STAR).compareTo(rhs.get(IDatabaseForNotes.STAR));
					if (a == 0) {
						return lhs.get(IDatabaseForNotes.TITLE).compareTo(rhs.get(IDatabaseForNotes.TITLE));
					} else if (a > 0) {
						return lhs.get(IDatabaseForNotes.TITLE).compareTo(rhs.get(IDatabaseForNotes.TITLE)) - 1000;
					} else {
						return lhs.get(IDatabaseForNotes.TITLE).compareTo(rhs.get(IDatabaseForNotes.TITLE)) + 1000;
					}
				}
			});
			if (displayToast) {
				toast0.setText(R.string.toast_sort_alphabet);
				toast0.show();
			}
		} else {
			Collections.sort(db.getList(), new Comparator<Map<String, String>>() {

				@Override
				public int compare(final Map<String, String> lhs, final Map<String, String> rhs) {
					int a = lhs.get(IDatabaseForNotes.STAR).compareTo(rhs.get(IDatabaseForNotes.STAR));
					if (a == 0) {
						return Long.valueOf(lhs.get(IDatabaseForNotes.POSITION)).compareTo(Long.valueOf(rhs.get(IDatabaseForNotes.POSITION)));
					} else if (a > 0) {
						return Long.valueOf(lhs.get(IDatabaseForNotes.POSITION)).compareTo(Long.valueOf(rhs.get(IDatabaseForNotes.POSITION))) - 1000;
					} else {
						return Long.valueOf(lhs.get(IDatabaseForNotes.POSITION)).compareTo(Long.valueOf(rhs.get(IDatabaseForNotes.POSITION))) + 1000;
					}
				}
			});
			if (displayToast) {
				toast0.setText(R.string.toast_sort_creation);
				toast0.show();
			}
		}
		((BaseAdapter) listview.getAdapter()).notifyDataSetChanged();
		spinner.setVisibility(View.GONE);
	}

	/**
	 * Create a choose password dialog for change the database master password.
	 */
	private void changePassword() {
		final FragmentManager fm = getFragmentManager();
		final ChoosePasswordDialogFragment choosePasswordDialog = new ChoosePasswordDialogFragment();
		final Bundle args = new Bundle();
		args.putInt("toastOK", R.string.toast_success_changePsw);
		args.putString("path", path);
		args.putSerializable("db", db);
		args.putString("Context", Context.INPUT_METHOD_SERVICE);
        args.putString("caller", "MainActivity");
		choosePasswordDialog.setArguments(args);
		choosePasswordDialog.show(fm, "new_password");
	}

    /**
     * Start a new activity EditNoteActivity for add a new note.
     */
    public void onFabClicked(View view) {
        final Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
        intent.putExtra(IDatabaseForNotes.TITLE, "");
        intent.putExtra(IDatabaseForNotes.TAGS, "");
        App.setNote("");
        intent.putExtra("TagList", db.getMapTag().keySet().toArray(new String[db.getMapTag().keySet().size()]));
        startActivityForResult(intent, DatabaseForNotesAsync.REQUEST_NEW_NOTE);
    }

	/*
	 * Call the DatabaseForNotesAsync for update the db according to the request.
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 * @param requestCode the action that need to be performed
	 * @param resultCode if is RESULT_CANCELED no action need to be performed.
	 *        Can also assume the value: RESULT_OK, RESULT_DELETE, RESULT_EDIT
	 * @param data the intent with the ability of retrieve the extra information bundled
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		searchMenuItem.collapseActionView();
		if (requestCode == DatabaseForNotesAsync.REQUEST_NEW_NOTE) {
			if (resultCode == RESULT_OK) {
				spinner.setVisibility(View.VISIBLE);
				new DatabaseForNotesAsync.Builder(this, DatabaseForNotesAsync.REQUEST_NEW_NOTE, db, arrayTagList, listview, mDrawerList)
				.strings(data.getStringExtra(IDatabaseForNotes.TITLE), data.getStringExtra(IDatabaseForNotes.TAGS),
						App.getNote(), false)
						.filter(filterSelected)
						.build().execute();
			}
		} else if (requestCode == DatabaseForNotesAsync.REQUEST_EDIT_NOTE) {
			if (resultCode == RESULT_DELETE) {
				spinner.setVisibility(View.VISIBLE);
				new DatabaseForNotesAsync.Builder(this, DatabaseForNotesAsync.REQUEST_DELETE_NOTE, db, arrayTagList, listview, mDrawerList)
				.locations(data.getLongExtra("location", -1), data.getIntExtra("listViewLocation", -1))
				.build().execute();
			}
			else if (resultCode == RESULT_EDIT) { //if the note changed
				spinner.setVisibility(View.VISIBLE);
				new DatabaseForNotesAsync.Builder(this, DatabaseForNotesAsync.REQUEST_EDIT_NOTE, db, arrayTagList, listview, mDrawerList)
				.strings(data.getStringExtra(IDatabaseForNotes.TITLE), data.getStringExtra(IDatabaseForNotes.TAGS),
						App.getNote(), data.getBooleanExtra(IDatabaseForNotes.STAR, false))
						.locations(data.getLongExtra("location", -1), data.getIntExtra("listViewLocation", -1))
						.filter(filterSelected)
						.build().execute();
			}
		}
	}

	@Override
	public void asyncComplete(final int request) {
		((BaseAdapter) listview.getAdapter()).notifyDataSetChanged();
		actionSort(false);
		((BaseAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
		if (db.getList().size() == 0) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            findViewById(R.id.empty).setVisibility(View.VISIBLE);
		} else {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            findViewById(R.id.empty).setVisibility(View.INVISIBLE);
		}
		if (request == DatabaseForNotesAsync.REQUEST_BUILD_LISTS) {
            spinner.setVisibility(View.GONE);
            boolean prefDrawer = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_drawer", true);
            if (prefDrawer && isFirstBuild && arrayTagList.size() > 1) {
                mDrawerLayout.openDrawer(GravityCompat.END);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawer(GravityCompat.END);
                    }
                }, 1500);
            }
            isFirstBuild = false;
        } else if(request == DatabaseForNotesAsync.REQUEST_CHANGE_PASSWORD) {
            finish();
        } else {
			if (request == DatabaseForNotesAsync.REQUEST_DELETE_NOTE) {
				new CryptoSave.Builder(db, path, key, getApplicationContext())
				.toastOK(getString(R.string.toast_success_deleteNote))
				.spinner(spinner)
				.build().execute();
				if (mDrawerListItemSelected != 0 && db.getList().size() == 0) { //If a filter is selected and for this tag there isn't more notes
					mDrawerList.setSoundEffectsEnabled(false);
					mDrawerList.performItemClick(mDrawerList, 0, mDrawerList.getItemIdAtPosition(0));
					mDrawerList.setSoundEffectsEnabled(true);
				}
			} else {
				new CryptoSave.Builder(db, path, key, getApplicationContext())
				.spinner(spinner)
				.build().execute();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		App.setIsInForeground(true);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		App.setIsInForeground(false);
		isMainActivityForeground = false;
		mHandler.postDelayed(r, MainActivity.TIMEOUT_SPLITTED);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		App.setIsInForeground(true);
		isMainActivityForeground = true;
	}

	@Override
	protected void onDestroy() {
		mHandler.removeCallbacks(r);
		if (thisActivityStartedCount) {
			App.setIsAnActivityCounting(false);
		}
		super.onDestroy();
	}

	@Override
	public void startActivity(Intent intent) {      
		// check if is search intent
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final Intent myIntent = new Intent(MainActivity.this, SearchResultsActivity.class);
			myIntent.putExtra("path", path);
            App.setDatabase(db);
			myIntent.putExtra("list", (ArrayList<Map<String,String>>) db.getList());
			myIntent.putExtra(SearchManager.QUERY, intent.getStringExtra(SearchManager.QUERY));
			startActivityForResult(myIntent, DatabaseForNotesAsync.REQUEST_EDIT_NOTE);
		} else {
			super.startActivity(intent);
		}
	}
}