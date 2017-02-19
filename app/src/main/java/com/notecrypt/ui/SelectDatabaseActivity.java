package com.notecrypt.ui;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.notecrypt.utils.DatabaseForNotes;
import com.notecrypt.utils.StringMethods;
import com.notecryptpro.R;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Primary Activity with the ability of open an existing database or creating a new one.
 * @author Ludovico de Nittis
 *
 */
public class SelectDatabaseActivity extends AppCompatActivity implements AsyncDelegate {

	private static final String DEFAULT_PATH = "SecretNotes.ncf";
	private static final String PATH = "path";
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
	private EditText editText;
	private Toast toast0;
	private Set<String> setRecent;
	private ListView listView;
	private List<String> listRecent;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_database);
		toast0 = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_LONG);
		if (getResources().getBoolean(R.bool.portrait_only)) { //Portrait only on phone (also landscape on tablet)
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		listView = (ListView) findViewById(android.R.id.list);
		setRecent = new HashSet<>();
		setRecent = getPreferences(MODE_PRIVATE).getStringSet("recent", null);
		if (setRecent != null) {
			listRecent = new LinkedList<>(setRecent);
		} else {
			listRecent = new LinkedList<>();
		}
		listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listRecent));
		updateDBDefaultFolder();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
				if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showInsertPasswordDialog((String) parent.getItemAtPosition(position));
                }
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
		    @Override
		    public boolean onItemLongClick(final AdapterView<?> av, View v, final int pos, long id) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(SelectDatabaseActivity.this)
                            .setTitle(R.string.action_delete)
                            .setMessage(R.string.deleteRecent_message)
                            .setPositiveButton(R.string.action_delete,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateRecentList((String) av.getItemAtPosition(pos), true);
                                        }
                                    })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).create().show();
                }
                return true;
		    }
		});
		editText = (EditText) findViewById(R.id.editTextPath);
		editText.setText(getPreferences(MODE_PRIVATE).getString("path", DEFAULT_PATH));
		final ImageButton buttonStorage = (ImageButton) findViewById(R.id.buttonStorage);
		buttonStorage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    final Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath());
                    intent.setDataAndType(uri, "file/*");
                    startActivityForResult(Intent.createChooser(intent, "Open folder"), 1);
                }
			}
		});

		final Button buttonOpen = (Button) findViewById(R.id.buttonOpen);
		buttonOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    final String path = editText.getText().toString();
                    if (path.equals("")) {
                        toast0.setText(R.string.toast_wrongPath);
                        toast0.show();
                    } else {
                        final SharedPreferences settings = getPreferences(MODE_PRIVATE);
                        final SharedPreferences.Editor editor = settings.edit();
                        //use as default the last path used
                        editor.putString(PATH, path);
                        editor.apply();
                        updateRecentList(path, false);
                        showInsertPasswordDialog(path);
                    }
                }
			}
		});

		final Button buttonCreate = (Button) findViewById(R.id.buttonCreate);
		buttonCreate.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(final View v) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    String path = editText.getText().toString();
                    path = StringMethods.getInstance().checkExtension(path);
                    // if the path has been corrected this reflect the change
                    editText.setText(path);
                    path = StringMethods.getInstance().fixPath(path);
                    showPasswordDialog(path);
                }
			}
		});
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

    @TargetApi(23)
    private boolean isPermissionGranted(String permission) {
        return android.os.Build.VERSION.SDK_INT < 23 || getBaseContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
    @TargetApi(23)
    private boolean checkPermission(String permission) {
        if (!isPermissionGranted(permission)) {
            requestPermissions(new String[]{permission}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateDBDefaultFolder();
                } else {
                    new ExplainPermissionFragment().show(getFragmentManager(), "explain");
                }
            }
        }
    }

	/**
	 * Show the custom insert password dialog.
	 * @param path the path of the file
	 */
	private void showInsertPasswordDialog(final String path) {
		final FragmentManager fm = getFragmentManager();
		final InsertPasswordDialogFragment insertPasswordDialog = new InsertPasswordDialogFragment();
		final Bundle args = new Bundle();
		args.putString(PATH, path);
		args.putString("Context", Context.INPUT_METHOD_SERVICE);
		insertPasswordDialog.setArguments(args);
		insertPasswordDialog.show(fm, "insert_password");
	}

	/**
	 * Show the custom choose password dialog.
	 * @param path the path of the file
	 */
	private void showPasswordDialog(final String path) {
		if (new File(path).exists()) {
			new AlertDialog.Builder(SelectDatabaseActivity.this)
			.setTitle(R.string.action_overwrite)
			.setMessage(R.string.overwrite_message)
			.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					final FragmentManager fm = getFragmentManager();
					//final ChoosePasswordDialogFragment choosePasswordDialog = new ChoosePasswordDialogFragment(SelectDatabaseActivity.this);
                    final ChoosePasswordDialogFragment choosePasswordDialog = new ChoosePasswordDialogFragment();
					final Bundle args = new Bundle();
					args.putInt("toastOK", R.string.toast_success_newDB);
					args.putString(PATH, path);
					args.putSerializable("db", new DatabaseForNotes());
					args.putString("Context", Context.INPUT_METHOD_SERVICE);
                    args.putString("caller", "SelectDatabaseActivity");
					choosePasswordDialog.setArguments(args);
					choosePasswordDialog.show(fm, "new_password");
				}
			})
			.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) { //do nothing
				}
			}).create().show();
		} else { //duplicated code, need to think to a better implementation
			final FragmentManager fm = getFragmentManager();
            final ChoosePasswordDialogFragment choosePasswordDialog = new ChoosePasswordDialogFragment();
			final Bundle args = new Bundle();
			args.putInt("toastOK", R.string.toast_success_newDB);
			args.putString(PATH, path);
			args.putSerializable("db", new DatabaseForNotes());
			args.putString("Context", Context.INPUT_METHOD_SERVICE);
            args.putString("caller", "SelectDatabaseActivity");
			choosePasswordDialog.setArguments(args);
			choosePasswordDialog.show(fm, "new_password");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity((new Intent(SelectDatabaseActivity.this, SettingsActivity.class)));
			return true;
		case R.id.action_about:
			AboutToast.getInstance().createAboutToast(getPackageManager(), getPackageName(), toast0, getApplicationContext());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
	{
		if (resultCode == RESULT_OK) {
			final Uri uri = intent.getData();
			final String path = convertMediaUriToPath(uri);
			final EditText editText = (EditText) findViewById(R.id.editTextPath);
			editText.setText(path);
		}
	}
	/**
	 * Cut the media uri (content:// or file://) to only the path of the file
	 * @param uri the uri of the media
	 * @return path of the file
	 */
	private String convertMediaUriToPath(final Uri uri) {
		if (uri.toString().substring(0, 4).equals("file")) {
			//remove "file://" from the uri
			return uri.toString().substring(7);
		} else if (uri.toString().substring(0, 7).equals("content")) {
			final String [] project = {MediaStore.Files.FileColumns.DATA};
			final Cursor cursor = getContentResolver().query(uri, project,  null, null, null);
			final int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			final String path = cursor.getString(columnIndex); 
			cursor.close();
			return path;
		}
		//uri will always begin with file or content, but with this is handled even if uri have something weird 
		return "";
	}
	
	private void updateRecentList(String item, boolean isDelete) {
		if (setRecent == null) {
			setRecent = new HashSet<>();
		}
		if (isDelete) {
			setRecent.remove(item);
		} else {
			setRecent.add(item);
		}
		final SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putStringSet("recent", null);
		editor.apply();
		editor.putStringSet("recent", setRecent);
		editor.apply();
		//remove and add for avoid double times the same item in list
		listRecent.remove(item);
		if (isDelete) {
			File file = new File(StringMethods.getInstance().fixPath(item));
			file.delete();
		} else {
			listRecent.add(item);
		}
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
	}
	
	private void updateDBDefaultFolder() {
		String path = Environment.getExternalStorageDirectory().toString() + "/" + StringMethods.DEFAULT_FOLDER;
		File f = new File(path);
		File file[] = f.listFiles();
		listRecent.clear();
		if(file != null) {
            for (File aFile : file) {
                if (!aFile.getName().startsWith(".")) {
                    listRecent.add(aFile.getName());
                }
            }
		}
		if (setRecent != null) {
			listRecent.removeAll(setRecent); //avoid double entries
			listRecent.addAll(setRecent);
		}
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void asyncComplete(int request) {
		updateDBDefaultFolder();
	}
}