package com.example.calyx.mykeep.Main;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.developer.filepicker.controller.DialogSelectionListener;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.example.calyx.mykeep.About.AboutActivity;
import com.example.calyx.mykeep.AppDefault.AppDefaultActivity;
import com.example.calyx.mykeep.R;
import com.example.calyx.mykeep.Settings.SettingsActivity;
import com.example.calyx.mykeep.Utility.Permissions;
import com.example.calyx.mykeep.Utility.StoreRetrieveData;
import com.example.calyx.mykeep.Utility.ToDoItem;
import com.example.calyx.mykeep.Utility.Utils;
import com.example.calyx.mykeep.Utility.dataBaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppDefaultActivity {

    public static final int REQUEST_CODE_PERMISSIONS = 2;


    public static final String FILENAME = "todoitems.json";
    public static final String BCK_FILENAME = "MyKeep_Bak.txt";
    public String NOTE_SESSION = "MyKeep_Session";
    public StoreRetrieveData storeRetrieveData;
    public ArrayList<ToDoItem> mToDoItemsArrayList;
    private static final int READ_REQUEST_CODE = 42;
    public Utils utls = new Utils();
    public FilePickerDialog dialog;
    ProgressDialog pd;
    Uri uri = null;

    //    String theme;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        storeRetrieveData = new StoreRetrieveData(this, FILENAME);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected int contentViewLayoutRes() {
        return R.layout.activity_main;
    }

    @NonNull
    @Override
    protected Fragment createInitialFragment() {
        return MainFragment.newInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        dataBaseHelper db = new dataBaseHelper(getApplicationContext());
        switch (item.getItemId()) {
            case R.id.aboutMeMenuItem:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);

                return true;
            case R.id.backupaboutMeMenuItem:
                String outFileName = Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name) + File.separator;
                performBackup(db, outFileName);
                return true;
            case R.id.restoreMeMenuItem:

                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
//                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                properties.root= new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name) + File.separator);

                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;
                properties.show_hidden_files = false;

                dialog = new FilePickerDialog(MainActivity.this,properties,2);
                dialog.setTitle("Select a File");


                dialog.setDialogSelectionListener(files -> {
                    String path = "";
                    if(files.length>0){
                        path=files[0];
                    }

                    db.importDB(path);
                    pd = new ProgressDialog(MainActivity.this);
                    pd.setMessage("loading");
                    pd.show();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(NOTE_SESSION, "Restored");
                    editor.commit();


                    new Handler().postDelayed(
                            () -> {

                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);
                            },
                            1000);
                    //files is the array of the paths of files selected by the Application User.
                });
                Permissions.verifyStoragePermissions(this);
                int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

                if(dialog!=null)
                {   //Show dialog if the read permission has been granted.
                    dialog.show();
                }
                //Add this method to show Dialog when the required permission has been granted to the app.
               return true;

            case R.id.preferences:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    public void performBackup(final dataBaseHelper db, final String outFileName) {


        Permissions.verifyStoragePermissions(this);

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + this.getResources().getString(R.string.app_name));

        boolean success = true;
        if (!folder.exists())
            success = folder.mkdirs();
        if (success) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Backup your notes?");
//            final EditText input = new EditText(this);
//            input.setInputType(InputType.TYPE_CLASS_TEXT);
//            builder.setView(input);
            builder.setPositiveButton("Yes", (dialog, which) -> {
//                String m_Text = input.getText().toString();
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
                String dateStr = dateFormat.format(cal.getTime());
                String m_Text = "glofora_" + dateStr;
                String out = outFileName + m_Text + ".db";

                db.backup(out);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

            builder.show();
        } else
            Toast.makeText(this, "Unable to create directory. Retry", Toast.LENGTH_SHORT).show();
    }

}


