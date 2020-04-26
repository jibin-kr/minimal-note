package com.example.calyx.mykeep.Main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.calyx.mykeep.About.AboutActivity;
import com.example.calyx.mykeep.AppDefault.AppDefaultActivity;
import com.example.calyx.mykeep.R;
import com.example.calyx.mykeep.Settings.SettingsActivity;
import com.example.calyx.mykeep.Utility.Permissions;
import com.example.calyx.mykeep.Utility.StoreRetrieveData;
import com.example.calyx.mykeep.Utility.ToDoItem;
import com.example.calyx.mykeep.Utility.Utils;
import com.example.calyx.mykeep.Utility.dataBaseHelper;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.calyx.mykeep.BuildConfig.DEBUG;


public class MainActivity extends AppDefaultActivity {

    public static final int REQUEST_CODE_PERMISSIONS = 2;


    public static final String FILENAME = "todoitems.json";
    public static final String BCK_FILENAME = "MyKeep_Bak.txt";
    public String NOTE_SESSION = "MyKeep_Session";
    public StoreRetrieveData storeRetrieveData;
    public ArrayList<ToDoItem> mToDoItemsArrayList;
    private static final int READ_REQUEST_CODE = 42;
    public Utils utls = new Utils();
    ProgressDialog pd;
    Uri uri = null;

    //    String theme;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
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

                Permissions.verifyStoragePermissions(this);
                Intent f_intent = new Intent(Intent.ACTION_GET_CONTENT);
                f_intent.setType("*/*");
                startActivityForResult(f_intent, READ_REQUEST_CODE);


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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        dataBaseHelper db = new dataBaseHelper(getApplicationContext());
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            if (resultData != null) {
                uri = resultData.getData();
//                Get Path From UR
                String path="";
                if(android.os.Build.VERSION.SDK_INT==24||android.os.Build.VERSION.SDK_INT==25)
                {
                    path=  getFilePathForN(uri,getApplicationContext());
                }else{
                    path=  getRealPathFromURI(getApplicationContext(),uri);
                }

                db.importDB(path);
                pd = new ProgressDialog(this);
                pd.setMessage("loading");
                pd.show();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(NOTE_SESSION, "Restored");
                editor.commit();


                new android.os.Handler().postDelayed(
                        () -> {

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);
                        },
                        1000);
            }
        }
    }
//    Get Extention from URI
public String getRealPathFromURI(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
        String[] proj = { MediaStore.Images.Media.DATA };
        cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    } finally {
        if (cursor != null) {
            cursor.close();
        }
    }
}
    private static String getFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
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


