package com.notecrypt.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.notecrypt.app.App;
import com.notecrypt.utils.DatabaseForNotes;
import com.notecrypt.utils.DatabaseForNotesAsync;
import com.notecrypt.utils.IDatabaseForNotes;
import com.notecrypt.utils.StringMethods;
import com.notecryptpro.R;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SearchResultsActivity extends AppCompatActivity {

    /**
     * Constant that represent the delete action.
     */
    public static final int RESULT_DELETE = -2;
    /**
     * Constant that represent the edit action.
     */
    public static final int RESULT_EDIT = -3;

    private DatabaseForNotes db;
    private List<Map<String, String>> list;
    private boolean isSearchResultsActivityForeground = true;
    private Handler mHandler;
    private Runnable r;
    private boolean thisActivityStartedCount; //false by default

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //enable the ability to press the title as back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        final Intent intent = getIntent();
        final String query = intent.getStringExtra(SearchManager.QUERY);
        setTitle("\"" + query + "\"");
        db = App.getDatabase();
        list = (List<Map<String, String>>) intent.getSerializableExtra("list");
        final ListView listview = findViewById(android.R.id.list);
        final String[] from = {IDatabaseForNotes.TITLE, IDatabaseForNotes.STAR};
        final int[] to = {R.id.txt, R.id.star};
        findViewById(R.id.empty).setVisibility(View.INVISIBLE);
        findViewById(R.id.fab).setVisibility(View.INVISIBLE);
        searchList(query);
        final CustomAdapter adapter = new CustomAdapter(this, list, R.layout.listview_layout, from, to);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                final Intent intent = new Intent(SearchResultsActivity.this, ReadNoteActivity.class);
                intent.putExtra("listViewLocation", position);
                intent.putExtra("location", list.get(position).get(IDatabaseForNotes.POSITION));
                App.setDatabase(db);
                startActivityForResult(intent, DatabaseForNotesAsync.REQUEST_EDIT_NOTE);
            }
        });
        final DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
                if (App.getTimesInBackground() >= MainActivity.MAX_TIMES_BACKGROUND + 2) {
                    SearchResultsActivity.this.finish();
                } else if (!isSearchResultsActivityForeground) { //Check in loop until the user change app or return to this activity
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
            default:
                return super.onOptionsItemSelected(item);
        }
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
        if (requestCode == DatabaseForNotesAsync.REQUEST_EDIT_NOTE) {
            if (resultCode == RESULT_DELETE) {
                final Intent intent = getIntent();

                intent.putExtra("location", data.getLongExtra("location", -1));
                intent.putExtra("listViewLocation", data.getIntExtra("listViewLocation", -1));
                setResult(MainActivity.RESULT_DELETE, intent);
                finish();
            } else if (resultCode == RESULT_EDIT) { //if the note changed
                final Intent intent = getIntent();
                intent.putExtra(IDatabaseForNotes.TITLE, data.getStringExtra(IDatabaseForNotes.TITLE));
                intent.putExtra(IDatabaseForNotes.TAGS, data.getStringExtra(IDatabaseForNotes.TAGS));
                App.setNote(data.getStringExtra(IDatabaseForNotes.NOTE));
                //intent.putExtra(IDatabaseForNotes.NOTE, data.getStringExtra(IDatabaseForNotes.NOTE));
                intent.putExtra("location", data.getLongExtra("location", -1));
                intent.putExtra("listViewLocation", data.getIntExtra("listViewLocation", -1));
                setResult(MainActivity.RESULT_EDIT, intent);
                finish();
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
        App.setIsInForeground(false);
        isSearchResultsActivityForeground = false;
        mHandler.postDelayed(r, MainActivity.TIMEOUT_SPLITTED);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.setIsInForeground(true);
        isSearchResultsActivityForeground = true;
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(r);
        if (thisActivityStartedCount) {
            App.setIsAnActivityCounting(false);
        }
        super.onDestroy();
    }

    private void searchList(final String query) {
        final Iterator<Map<String, String>> iter = list.iterator();
        while (iter.hasNext()) {
            final Map<String, String> item = iter.next();
            if (!(StringMethods.containsIgnoreCase(item.get(IDatabaseForNotes.TITLE), query) ||
                    StringMethods.containsIgnoreCase(db.getNotes().get(Long.parseLong(item.get(IDatabaseForNotes.POSITION))).getNote(), query))) {
                iter.remove();
            }
        }
    }
}