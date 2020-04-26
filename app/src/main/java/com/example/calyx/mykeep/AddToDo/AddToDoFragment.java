package com.example.calyx.mykeep.AddToDo;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calyx.mykeep.AppDefault.AppDefaultFragment;
import com.example.calyx.mykeep.Main.MainFragment;
import com.example.calyx.mykeep.R;
import com.example.calyx.mykeep.Utility.ToDoItem;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class AddToDoFragment extends AppDefaultFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "AddToDoFragment";
    public String SINGLE_NOTE_SESSION = "MyKeep_SINGLE_Session";


    private EditText mToDoTextBodyEditText;
    private EditText mToDoTextBodyDescription;

    private SwitchCompat mToDoDateSwitch;
    //    private TextView mLastSeenTextView;
    private LinearLayout mUserDateSpinnerContainingLinearLayout;
    private TextView mReminderTextView;

    private String CombinationText;

    private EditText mDateEditText;
    private EditText mTimeEditText;

    private ToDoItem mUserToDoItem;


    private String mUserEnteredText;
    private String mUserEnteredDescription;
    private boolean mUserHasReminder;
    private Toolbar mToolbar;
    private Date mUserReminderDate;
    private int mUserColor;
    private String mUsertodoImage = "";

    private LinearLayout mContainerLayout;
    private String theme;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public ImageView thumbnail;
    private static final int PICK_FROM_GALLARY = 2;


    private Uri outPutfileUri;
    private ScrollView scroll;
    private Bitmap bitmap = null;
    private int CAMREA_CODE = 112;
    ProgressDialog pd;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ImageButton reminderIconImageButton;
        TextView reminderRemindMeTextView;
        setHasOptionsMenu(true);

        theme = getActivity().getSharedPreferences(MainFragment.THEME_PREFERENCES, MODE_PRIVATE).getString(MainFragment.THEME_SAVED, MainFragment.LIGHTTHEME);
        if (theme.equals(MainFragment.LIGHTTHEME)) {
            getActivity().setTheme(R.style.CustomStyle_LightTheme);

        } else {
            getActivity().setTheme(R.style.CustomStyle_DarkTheme);
        }


        final Drawable cross = getResources().getDrawable(R.drawable.ic_clear_white_24dp);
        if (cross != null) {
            cross.setColorFilter(getResources().getColor(R.color.primary_light), PorterDuff.Mode.SRC_ATOP);
        }
        scroll = view.findViewById(R.id.scroll);
        mToolbar = view.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(cross);

        }


        mUserToDoItem = (ToDoItem) getActivity().getIntent().getSerializableExtra(MainFragment.TODOITEM);


        mUserEnteredText = mUserToDoItem.getToDoText();
        mUserEnteredDescription = mUserToDoItem.getmToDoDescription();
        mUserHasReminder = mUserToDoItem.hasReminder();
        mUserReminderDate = mUserToDoItem.getToDoDate();
        mUserColor = mUserToDoItem.getTodoColor();
        mUsertodoImage = mUserToDoItem.getTodoImage();


        reminderIconImageButton = view.findViewById(R.id.userToDoReminderIconImageButton);
        reminderRemindMeTextView = view.findViewById(R.id.userToDoRemindMeTextView);


        thumbnail = getView().findViewById(R.id.thumbnail);

        mContainerLayout = view.findViewById(R.id.todoReminderAndDateContainerLayout);
        mUserDateSpinnerContainingLinearLayout = view.findViewById(R.id.toDoEnterDateLinearLayout);
        mReminderTextView = view.findViewById(R.id.newToDoDateTimeReminderTextView);
        mToDoDateSwitch = view.findViewById(R.id.toDoHasDateSwitchCompat);
        mDateEditText = view.findViewById(R.id.newTodoDateEditText);
        mTimeEditText = view.findViewById(R.id.newTodoTimeEditText);


        mToDoTextBodyEditText = view.findViewById(R.id.userToDoEditText);

        mToDoTextBodyDescription = view.findViewById(R.id.userToDoDescription);


        mContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(mToDoTextBodyEditText);
                hideKeyboard(mToDoTextBodyDescription);
            }
        });


        if (mUserHasReminder && (mUserReminderDate != null)) {
            setReminderTextView();
            setEnterDateLayoutVisibleWithAnimations(true);
        }
        if (mUserReminderDate == null) {
            mToDoDateSwitch.setChecked(false);
            mReminderTextView.setVisibility(View.INVISIBLE);

        }


        if (!mUsertodoImage.isEmpty()) {



            thumbnail.setImageBitmap(StringToBitMap(mUsertodoImage));

            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            thumbnail.setLayoutParams(parms);
            setMargins(thumbnail, getDimenInt(getContext(), R.dimen.note_img_margin_left),
                    getDimenInt(getContext(), R.dimen.note_img_margin_top),
                    getDimenInt(getContext(), R.dimen.note_img_margin_right),
                    getDimenInt(getContext(), R.dimen.note_img_margin_bottom));

//            thumbnail.setAdjustViewBounds(false);
            PhotoViewAttacher pAttacher;
            pAttacher = new PhotoViewAttacher(thumbnail);
            pAttacher.update();

        }
        if (mUserEnteredText.isEmpty()) {
            mToDoTextBodyEditText.requestFocus();

        } else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }


        mToDoTextBodyEditText.setText(mUserEnteredText);
        mToDoTextBodyDescription.setText(mUserEnteredDescription);
        if (theme.equals(MainFragment.DARKTHEME)) {
            reminderIconImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_add_white_24dp));
            reminderRemindMeTextView.setTextColor(Color.WHITE);
            scroll.setBackgroundColor(getResources().getColor(R.color.mdtp_dark_gray));
            mToDoTextBodyEditText.setTextColor(Color.WHITE);
            mToDoTextBodyDescription.setTextColor(Color.WHITE);
        }

        mToDoTextBodyEditText.setSelection(mToDoTextBodyEditText.length());


        mToDoTextBodyEditText.addTextChangedListener(new TextWatcher() {
            @SuppressLint("RestrictedApi")
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mToolbar.canShowOverflowMenu();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUserEnteredText = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mToDoTextBodyDescription.setText(mUserEnteredDescription);
        mToDoTextBodyDescription.setSelection(mToDoTextBodyDescription.length());
        mToDoTextBodyDescription.addTextChangedListener(new TextWatcher() {
            @SuppressLint("RestrictedApi")
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mToolbar.canShowOverflowMenu();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUserEnteredDescription = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        setEnterDateLayoutVisible(mToDoDateSwitch.isChecked());

        mToDoDateSwitch.setChecked(mUserHasReminder && (mUserReminderDate != null));
        mToDoDateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {


            if (!isChecked) {
                mUserReminderDate = null;
            }
            mUserHasReminder = isChecked;
            setDateAndTimeEditText();
            setEnterDateLayoutVisibleWithAnimations(isChecked);
            hideKeyboard(mToDoTextBodyEditText);
            hideKeyboard(mToDoTextBodyDescription);
        });


        mDateEditText.setOnClickListener(v -> {

            Date date;
            hideKeyboard(mToDoTextBodyEditText);
            if (mUserToDoItem.getToDoDate() != null) {
//                    date = mUserToDoItem.getToDoDate();
                date = mUserReminderDate;
            } else {
                date = new Date();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(AddToDoFragment.this, year, month, day);
            if (theme.equals(MainFragment.DARKTHEME)) {
                datePickerDialog.setThemeDark(true);
            }
            datePickerDialog.show(getActivity().getFragmentManager(), "DateFragment");

        });


        mTimeEditText.setOnClickListener(v -> {

            Date date;
            hideKeyboard(mToDoTextBodyEditText);
            if (mUserToDoItem.getToDoDate() != null) {
//                    date = mUserToDoItem.getToDoDate();
                date = mUserReminderDate;
            } else {
                date = new Date();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(AddToDoFragment.this, hour, minute, DateFormat.is24HourFormat(getContext()));
            if (theme.equals(MainFragment.DARKTHEME)) {
                timePickerDialog.setThemeDark(true);
            }
            timePickerDialog.show(getActivity().getFragmentManager(), "TimeFragment");
        });

//Show time and date on reminder window
        setDateAndTimeEditText();


    }

    @Override

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // TODO Add your menu entries here

        inflater.inflate(R.menu.menu_notes, menu);
        MenuItem ocrItem = menu.findItem(R.id.ocr_reader);

        // show the button when some condition is true
        if (mUsertodoImage.isEmpty()) {
            ocrItem.setVisible(false);
        }


        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Capture image from camera
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //pic coming from camera

            try {
//                ----------------------------------------
//                Compress image for better performance
//                for best performance use max size :640
//                ----------------------------------------
                bitmap = getResizedBitmap(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), outPutfileUri), 640);

                mUsertodoImage = getFileToByte(bitmap);


                thumbnail.setImageBitmap(bitmap);
//                thumbnail.setAdjustViewBounds(true);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                thumbnail.setLayoutParams(parms);
                setMargins(thumbnail, getDimenInt(getContext(), R.dimen.note_img_margin_left),
                        getDimenInt(getContext(), R.dimen.note_img_margin_top),
                        getDimenInt(getContext(), R.dimen.note_img_margin_right),
                        getDimenInt(getContext(), R.dimen.note_img_margin_bottom));
                PhotoViewAttacher pAttacher;
                pAttacher = new PhotoViewAttacher(thumbnail);
                pAttacher.update();

                getActivity().invalidateOptionsMenu();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
//Pick image frm gallery
        if (requestCode == PICK_FROM_GALLARY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            if (isExternalStorageWritable()) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                bitmap = getResizedBitmap(BitmapFactory.decodeFile(imgDecodableString), 640);

                mUsertodoImage = getFileToByte(bitmap);

//                thumbnail = (ImageView) getView().findViewById(R.id.thumbnail);
                setMargins(thumbnail, getDimenInt(getContext(), R.dimen.note_img_margin_left),
                        getDimenInt(getContext(), R.dimen.note_img_margin_top),
                        getDimenInt(getContext(), R.dimen.note_img_margin_right),
                        getDimenInt(getContext(), R.dimen.note_img_margin_bottom));
                thumbnail.setImageBitmap(bitmap);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                thumbnail.setLayoutParams(parms);
                thumbnail.setAdjustViewBounds(true);
                PhotoViewAttacher pAttacher;
                pAttacher = new PhotoViewAttacher(thumbnail);
                pAttacher.update();
                getActivity().invalidateOptionsMenu();
            } else {
                Toast.makeText(getContext(), "You have no permission to access external storage", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {

                    makeResult(RESULT_CANCELED);
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                hideKeyboard(mToDoTextBodyEditText);
                return true;
            case R.id.action_camera:
//                Add camera image to note
                dispatchTakePictureIntent();
                return true;
            case R.id.ocr_reader:
//                extract text from image
                OCR_dialog();

                return true;
            case R.id.action_gallery:

                if (permissionAlreadyGranted()) {

                    galleryAddPic();
                    return true;
                }

                requestPermission();
                return true;

            case R.id.copy_to_clipboard:

                copy_to_clipboard();

                return true;
            case R.id.share_notes:
                share_note_with_image();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CAMREA_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (!showRationale) {
                    openSettingsDialog();
                }
            }
        }
    }


    private void openSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();

    }


    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
        setTime(hour, minute);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        setDate(year, month, day);
    }


    public void setEnterDateLayoutVisibleWithAnimations(boolean checked) {
        if (checked) {
            setReminderTextView();
            mUserDateSpinnerContainingLinearLayout.animate().alpha(1.0f).setDuration(500).setListener(
                    new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mUserDateSpinnerContainingLinearLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    }
            );
        } else {
            mUserDateSpinnerContainingLinearLayout.animate().alpha(0.0f).setDuration(500).setListener(
                    new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mUserDateSpinnerContainingLinearLayout.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }
            );
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        if (mToDoTextBodyEditText.length() <= 0 && mToDoTextBodyDescription.length() <= 0) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(SINGLE_NOTE_SESSION, "EMPTY");
            editor.commit();
        } else if (mUserReminderDate != null && mUserReminderDate.before(new Date())) {
            makeResult(RESULT_CANCELED);
        } else {

            makeResult(RESULT_OK);
//            getActivity().finish();
        }
        hideKeyboard(mToDoTextBodyEditText);
        hideKeyboard(mToDoTextBodyDescription);
    }

    @Override
    protected int layoutRes() {
        return R.layout.fragment_add_to_do;
    }

    //    =========================================================
// Methods for Add Todo Fragments
//    =========================================================
    public static AddToDoFragment newInstance() {
        return new AddToDoFragment();
    }

    private void dispatchTakePictureIntent() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (captureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            File file = new File(getContext().getExternalFilesDir(null), "MyKeep_Image.jpg");
            outPutfileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), "ibas.provider", file);


            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri);
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(captureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    private String startCameraSource(Bitmap bitmap) {

        //Create the TextRecognizer
        String imageText = "";
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {


            Frame imageFrame = new Frame.Builder()

                    .setBitmap(bitmap)                 // your image bitmap
                    .build();


            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                imageText = imageText + " \n " + textBlock.getValue();                   // return string
            }


        }
        pd.hide();
        return imageText;
    }


    public int getDimenInt(Context context, @DimenRes int dimenRes) {
        return context.getResources().getDimensionPixelSize(dimenRes);
    }

    public void setEnterDateLayoutVisible(boolean checked) {
        if (checked) {
            mUserDateSpinnerContainingLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mUserDateSpinnerContainingLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    public static String formatDate(String formatString, Date dateToFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
        return simpleDateFormat.format(dateToFormat);
    }

    private void OCR_dialog() {
        if (!mUsertodoImage.isEmpty()) {
            pd = new ProgressDialog(getContext());
            pd.setMessage("loading");
            pd.show();
            mToDoTextBodyDescription.setText(startCameraSource(StringToBitMap(mUsertodoImage)).trim());

            Toast.makeText(getActivity(), "Image text grabbed", Toast.LENGTH_SHORT).show();
        }

    }

    private void share_note_with_image() {
        String toDoTextContainer_share = mToDoTextBodyEditText.getText().toString();
        String toDoTextBodyDescriptionContainer_share = mToDoTextBodyDescription.getText().toString();
        CombinationText = "" + toDoTextContainer_share + "\n" + toDoTextBodyDescriptionContainer_share + "\n\n -Glofora Notes for android";
//
        if (!mUsertodoImage.isEmpty()) {
            File file = new File(getContext().getExternalCacheDir(), "logicchip.png");
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);

                bitmap = StringToBitMap(mUsertodoImage);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, CombinationText);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share note via"));
        } else {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, CombinationText);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share note via"));
        }
    }

    private void copy_to_clipboard() {
        String toDoTextContainer = mToDoTextBodyEditText.getText().toString();
        String toDoTextBodyDescriptionContainer = mToDoTextBodyDescription.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        CombinationText = "" + toDoTextContainer + "\n" + toDoTextBodyDescriptionContainer + "\n\n -Glofora Notes for android";
        ClipData clip = ClipData.newPlainText("text", CombinationText);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Copied To Clipboard!", Toast.LENGTH_SHORT).show();
    }

    private boolean permissionAlreadyGranted() {

        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            galleryAddPic();
        }
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMREA_CODE);

    }

    public void setReminderTextView() {
        if (mUserReminderDate != null) {
            mReminderTextView.setVisibility(View.VISIBLE);
            if (mUserReminderDate.before(new Date())) {
                mReminderTextView.setText(getString(R.string.date_error_check_again));
                mReminderTextView.setTextColor(Color.RED);
                return;
            }
            Date date = mUserReminderDate;
            String dateString = formatDate("d MMM, yyyy", date);
            String timeString;
            String amPmString = "";

            if (DateFormat.is24HourFormat(getContext())) {
                timeString = formatDate("k:mm", date);
            } else {
                timeString = formatDate("h:mm", date);
                amPmString = formatDate("a", date);
            }
            String finalString = String.format(getResources().getString(R.string.remind_date_and_time), dateString, timeString, amPmString);
            mReminderTextView.setTextColor(getResources().getColor(R.color.secondary_text));
            mReminderTextView.setText(finalString);
        } else {
            mReminderTextView.setVisibility(View.INVISIBLE);

        }
    }

    public void makeResult(int result) {

        Intent i = new Intent();
        if (mUserEnteredText.length() > 0) {
            mUserEnteredText=mUserEnteredText.trim();
            mUserEnteredDescription=mUserEnteredDescription.trim();
            String capitalizedString = Character.toUpperCase(mUserEnteredText.charAt(0)) + mUserEnteredText.substring(1);
            mUserToDoItem.setToDoText(capitalizedString);

            mUserToDoItem.setmToDoDescription(mUserEnteredDescription);
        } else {
            mUserEnteredText = "No Title";
            mUserEnteredDescription=mUserEnteredDescription.trim();
            String capitalizedString = Character.toUpperCase(mUserEnteredText.charAt(0)) + mUserEnteredText.substring(1);
            mUserToDoItem.setToDoText(capitalizedString);


            mUserToDoItem.setmToDoDescription(mUserEnteredDescription);
        }

        if (mUserReminderDate != null) {
            if (!mUserReminderDate.before(new Date())) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mUserReminderDate);
                calendar.set(Calendar.SECOND, 0);
                mUserReminderDate = calendar.getTime();
                mUserToDoItem.setHasReminder(mUserHasReminder);
                mUserToDoItem.setToDoDate(mUserReminderDate);
            } else {
                mUserToDoItem.setHasReminder(false);
                mUserToDoItem.setToDoDate(null);
            }
        }

        mUserToDoItem.setTodoColor(mUserColor);
        mUserToDoItem.setTodoImage(mUsertodoImage);

        i.putExtra(MainFragment.TODOITEM, mUserToDoItem);
        Gson gson = new Gson();
        String note = gson.toJson(mUserToDoItem);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SINGLE_NOTE_SESSION, note);
        editor.commit();
        getActivity().setResult(result, i);
    }

    //    function for resize image for good performance
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    //    Function to convert byte string to image
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    // put the image file path into this method
    public static String getFileToByte(Bitmap bmp) {

        ByteArrayOutputStream bos;
        byte[] bt;
        String encodeString = null;
        try {

            bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bt = bos.toByteArray();
            encodeString = Base64.encodeToString(bt, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodeString;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    private void setDateAndTimeEditText() {

        if (mUserToDoItem.hasReminder() && mUserReminderDate != null) {
            String userDate = formatDate("d MMM, yyyy", mUserReminderDate);
            String formatToUse;
            if (DateFormat.is24HourFormat(getContext())) {
                formatToUse = "k:mm";
            } else {
                formatToUse = "h:mm a";

            }
            String userTime = formatDate(formatToUse, mUserReminderDate);
            mTimeEditText.setText(userTime);
            mDateEditText.setText(userDate);

        } else {
            mDateEditText.setText(getString(R.string.date_reminder_default));
//            mUserReminderDate = new Date();
            boolean time24 = DateFormat.is24HourFormat(getContext());
            Calendar cal = Calendar.getInstance();
            if (time24) {
                cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
            } else {
                cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + 1);
            }
            cal.set(Calendar.MINUTE, 0);
            mUserReminderDate = cal.getTime();
            Log.d("OskarSchindler", "Imagined Date: " + mUserReminderDate);
            String timeString;
            if (time24) {
                timeString = formatDate("k:mm", mUserReminderDate);
            } else {
                timeString = formatDate("h:mm a", mUserReminderDate);
            }
            mTimeEditText.setText(timeString);

        }
    }

    public void hideKeyboard(EditText et) {

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    private void galleryAddPic() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_FROM_GALLARY);
    }


    public void setDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        int hour, minute;


        Calendar reminderCalendar = Calendar.getInstance();
        reminderCalendar.set(year, month, day);

        if (reminderCalendar.before(calendar)) {
            return;
        }

        if (mUserReminderDate != null) {
            calendar.setTime(mUserReminderDate);
        }

        if (DateFormat.is24HourFormat(getContext())) {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        } else {

            hour = calendar.get(Calendar.HOUR);
        }
        minute = calendar.get(Calendar.MINUTE);

        calendar.set(year, month, day, hour, minute);
        mUserReminderDate = calendar.getTime();
        setReminderTextView();
//        setDateAndTimeEditText();
        setDateEditText();
    }

    public void setTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        if (mUserReminderDate != null) {
            calendar.setTime(mUserReminderDate);
        }

//        if(DateFormat.is24HourFormat(this) && hour == 0){
//            //done for 24h time
//                hour = 24;
//        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, hour, minute, 0);
        mUserReminderDate = calendar.getTime();

        setReminderTextView();
//        setDateAndTimeEditText();
        setTimeEditText();
    }


    public void setDateEditText() {
        String dateFormat = "d MMM, yyyy";
        mDateEditText.setText(formatDate(dateFormat, mUserReminderDate));
    }

    public void setTimeEditText() {
        String dateFormat;
        if (DateFormat.is24HourFormat(getContext())) {
            dateFormat = "k:mm";
        } else {
            dateFormat = "h:mm a";

        }
        mTimeEditText.setText(formatDate(dateFormat, mUserReminderDate));
    }

//    ===========================================================


}

