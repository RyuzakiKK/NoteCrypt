package com.notecrypt.ui;

import com.notecrypt.app.App;
import com.notecrypt.utils.IDatabaseForNotes;
import com.notecryptpro.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

/**
 * Activity with the ability of create or edit notes.
 *
 * @author Ludovico de Nittis
 */
public class EditNoteActivity extends AppCompatActivity {

    private Handler mHandler;
    private Runnable r;
    private boolean isEditNoteActivityForeground = true;
    private boolean thisActivityStartedCount = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);
        final Intent intent = getIntent();
        final String title = intent.getStringExtra(IDatabaseForNotes.TITLE);
        final String tags = intent.getStringExtra(IDatabaseForNotes.TAGS);
        final String note = App.getNote();
        final TextView textViewTitle = (TextView) findViewById(R.id.editTextTitle);
        final MultiAutoCompleteTextView macTextViewTags = (MultiAutoCompleteTextView) findViewById(R.id.editTextTags);
        final TextView textViewNote = (TextView) findViewById(R.id.editTextNote);
        textViewTitle.setText(title);
        macTextViewTags.setText(tags);
        textViewNote.setText(note);
        final String[] tagList = intent.getStringArrayExtra("TagList");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tagList);
        macTextViewTags.setAdapter(adapter);
        macTextViewTags.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {

            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;
                while (i > 0 && text.charAt(i - 1) != ',') {
                    i--;
                }
                while (i < cursor && text.charAt(i) == ' ') {
                    i++;
                }
                return i;
            }

            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();
                while (i < len) {
                    if (text.charAt(i) == ',') {
                        return i;
                    } else {
                        i++;
                    }
                }
                return len;
            }

            public CharSequence terminateToken(CharSequence text) {
                int i = text.length();
                while (i > 0 && text.charAt(i - 1) == ' ') {
                    i--;
                }
                if (i > 0 && text.charAt(i - 1) == ',') {
                    return text;
                } else {
                    if (text instanceof Spanned) {
                        SpannableString sp = new SpannableString(text + ", ");
                        TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                                Object.class, sp, 0);
                        return sp;
                    } else {
                        return text + ", ";
                    }
                }
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
                } else if (!App.isInForeground() && ((!App.isAnActivityCounting()) || thisActivityStartedCount)) {
                    App.setIsAnActivityCounting(true);
                    thisActivityStartedCount = true;
                    App.incTimesInBackground();
                }
                if (App.getTimesInBackground() >= MainActivity.MAX_TIMES_BACKGROUND) {
                    EditNoteActivity.this.finish();
                } else if (!isEditNoteActivityForeground) { //Check in loop until the user change app or return to this activity
                    mHandler.postDelayed(r, MainActivity.TIMEOUT_SPLITTED);
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_change, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Create an AlertDialog if the user press the back button.
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_backNoSave)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        final Intent intent = getIntent();
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Save in the extra the fields filled by the user and set the result to RESULT_OK.
     */
    private void saveNote() {
        final EditText fieldTitle = (EditText) findViewById(R.id.editTextTitle);
        final EditText fieldTags = (EditText) findViewById(R.id.editTextTags);
        final EditText fieldNote = (EditText) findViewById(R.id.editTextNote);
        final Intent intent = getIntent();
        intent.putExtra(IDatabaseForNotes.TITLE, fieldTitle.getText().toString());
        intent.putExtra(IDatabaseForNotes.TAGS, fieldTags.getText().toString());
        App.setNote(fieldNote.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
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
        isEditNoteActivityForeground = false;
        mHandler.postDelayed(r, MainActivity.TIMEOUT_SPLITTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.setIsInForeground(true);
        isEditNoteActivityForeground = true;
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
