package com.example.calyx.mykeep.Main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.calyx.mykeep.AddToDo.AddToDoActivity;
import com.example.calyx.mykeep.AddToDo.AddToDoFragment;
import com.example.calyx.mykeep.AppDefault.AppDefaultFragment;
import com.example.calyx.mykeep.R;
import com.example.calyx.mykeep.Reminder.ReminderFragment;
import com.example.calyx.mykeep.Utility.ItemTouchHelperClass;
import com.example.calyx.mykeep.Utility.RecyclerViewEmptySupport;
import com.example.calyx.mykeep.Utility.StoreRetrieveData;
import com.example.calyx.mykeep.Utility.ToDoItem;
import com.example.calyx.mykeep.Utility.TodoNotificationService;
import com.example.calyx.mykeep.Utility.dataBaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends AppDefaultFragment {
    private RecyclerViewEmptySupport mRecyclerView;
    private FloatingActionButton mAddToDoItemFAB;
    public static ArrayList<ToDoItem> mToDoItemsArrayList;
    private CoordinatorLayout mCoordLayout;
    public static final String TODOITEM = "com.calyx.MyKeep.MainActivity";
    private MainFragment.BasicListAdapter adapter;
    private static final int REQUEST_ID_TODO_ITEM = 100;
    private ToDoItem mJustDeletedToDoItem;
    private TextView txtHMtitle;
    private dataBaseHelper db;
    private int mIndexOfDeletedToDoItem;
    public static final String DATE_TIME_FORMAT_12_HOUR = "MMM d, yyyy  h:mm a";
    public static final String DATE_TIME_FORMAT_24_HOUR = "MMM d, yyyy  k:mm";
    public static final String FILENAME = "todoitems.json";
    public static StoreRetrieveData storeRetrieveData;
    public ItemTouchHelper itemTouchHelper;
    private CustomRecyclerScrollViewListener customRecyclerScrollViewListener;
    public static final String SHARED_PREF_DATA_SET_CHANGED = "com.calyx.MyKeep.datasetchanged";
    public static final String CHANGE_OCCURED = "com.calyx.MyKeep.changeoccured";
    private int mTheme = -1;
    private String theme = "name_of_the_theme";
    public static final String THEME_PREFERENCES = "com.calyx.MyKeep.themepref";
    public static final String RECREATE_ACTIVITY = "com.calyx.MyKeep.recreateactivity";
    public static final String THEME_SAVED = "com.calyx.MyKeep.savedtheme";
    public static final String DARKTHEME = "com.calyx.MyKeep.darktheme";
    public static final String LIGHTTHEME = "com.calyx.MyKeep.lighttheme";
    public String NOTE_SESSION = "MyKeep_Session";
    public String SINGLE_NOTE_SESSION = "MyKeep_SINGLE_Session";


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //We recover the theme we've set and setTheme accordingly
        theme = getActivity().getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE).getString(THEME_SAVED, LIGHTTHEME);

        if (theme.equals(LIGHTTHEME)) {
            mTheme = R.style.CustomStyle_LightTheme;
        } else {
            mTheme = R.style.CustomStyle_DarkTheme;
        }
        db = new dataBaseHelper(getContext());
        this.getActivity().setTheme(mTheme);

        super.onCreate(savedInstanceState);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CHANGE_OCCURED, false);
        editor.apply();


        storeRetrieveData = new StoreRetrieveData(getContext(), FILENAME);
        mToDoItemsArrayList = getLocallyStoredData(storeRetrieveData);
        adapter = new MainFragment.BasicListAdapter(mToDoItemsArrayList);
        setAlarms();

        mCoordLayout = view.findViewById(R.id.myCoordinatorLayout);
        txtHMtitle = view.findViewById(R.id.txtHMtitle);
        if (theme.equals(DARKTHEME)) {
            mCoordLayout.setBackgroundColor(getResources().getColor(R.color.mdtp_dark_gray));
            txtHMtitle.setTextColor(getResources().getColor(R.color.secondary_text));
        }
        mAddToDoItemFAB = view.findViewById(R.id.addToDoItemFAB);

        mAddToDoItemFAB.setOnClickListener(v -> {
            Intent newTodo = new Intent(getContext(), AddToDoActivity.class);
            ToDoItem item = new ToDoItem("", "", false, null, "");
            int color = ColorGenerator.MATERIAL.getRandomColor();
            item.setTodoColor(color);

            newTodo.putExtra(TODOITEM, item);
            startActivityForResult(newTodo, REQUEST_ID_TODO_ITEM);
        });
        mRecyclerView = view.findViewById(R.id.toDoRecyclerView);
        if (theme.equals(LIGHTTHEME)) {
            mRecyclerView.setBackgroundColor(getResources().getColor(R.color.primary_lightest));
        } else {
            mRecyclerView.setBackgroundColor(getResources().getColor(R.color.mdtp_dark_gray));
        }

        customRecyclerScrollViewListener = new CustomRecyclerScrollViewListener() {
            @Override
            public void show() {

                mAddToDoItemFAB.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                mAddToDoItemFAB.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }

            @Override
            public void hide() {
//
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mAddToDoItemFAB.getLayoutParams();
                int fabMargin = lp.bottomMargin;
                mAddToDoItemFAB.animate().translationY(mAddToDoItemFAB.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }
        };

        mRecyclerView.setEmptyView(view.findViewById(R.id.toDoEmptyView));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addOnScrollListener(customRecyclerScrollViewListener);


        ItemTouchHelper.Callback callback = new ItemTouchHelperClass(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);


        mRecyclerView.setAdapter(adapter);


    }


    public static ArrayList<ToDoItem> getLocallyStoredData(StoreRetrieveData storeRetrieveData) {
        ArrayList<ToDoItem> items = null;

        try {
            items = storeRetrieveData.loadFromFile();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if (items == null) {
            items = new ArrayList<>();
        }
        return items;

    }

    @Override
    public void onResume() {
        super.onResume();


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(ReminderFragment.EXIT, false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ReminderFragment.EXIT, false);
            editor.apply();
            getActivity().finish();
        }
        /*
        We need to do this, as this activity's onCreate won't be called when coming back from SettingsActivity,
        thus our changes to dark/light mode won't take place, as the setContentView() is not called again.
        So, inside our SettingsFragment, whenever the checkbox's value is changed, in our shared preferences,
        we mark our recreate_activity key as true.

        Note: the recreate_key's value is changed to false before calling recreate(), or we woudl have ended up in an infinite loop,
        as onResume() will be called on recreation, which will again call recreate() and so on....
        and get an ANR

         */
        if (getActivity().getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE).getBoolean(RECREATE_ACTIVITY, false)) {
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(RECREATE_ACTIVITY, false);
            editor.apply();
            getActivity().recreate();
        }


    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getString(SINGLE_NOTE_SESSION, null) != null) {
            String NoteString = (prefs.getString(SINGLE_NOTE_SESSION, ""));
            if (NoteString.equals("EMPTY")) {
                Toast.makeText(getContext(), "Empty note discarded!", Toast.LENGTH_LONG).show();
            } else {
                TypeToken<ToDoItem> token = new TypeToken<ToDoItem>() {
                };
                Gson gson = new Gson();
                ToDoItem item = gson.fromJson(NoteString, token.getType());
                InsertUpdateNote(item);
            }


            prefs.edit().remove(SINGLE_NOTE_SESSION).commit();
        }

        if (sharedPreferences.getBoolean(CHANGE_OCCURED, false)) {

            mToDoItemsArrayList = getLocallyStoredData(storeRetrieveData);


            adapter = new MainFragment.BasicListAdapter(mToDoItemsArrayList);
            mRecyclerView.setAdapter(adapter);
            setAlarms();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(CHANGE_OCCURED, false);
            editor.apply();


        }
        saveData();

    }

    private void setAlarms() {
        if (mToDoItemsArrayList != null) {
            for (ToDoItem item : mToDoItemsArrayList) {
                if (item.hasReminder() && item.getToDoDate() != null) {
                    if (item.getToDoDate().before(new Date())) {
                        item.setToDoDate(null);
                        continue;
                    }
                    Intent i = new Intent(getContext(), TodoNotificationService.class);
                    i.putExtra(TodoNotificationService.TODOUUID, item.getIdentifier());
                    i.putExtra(TodoNotificationService.TODOTEXT, item.getToDoText());
                    createAlarm(i, item.getIdentifier().hashCode(), item.getToDoDate().getTime());
                }
            }
        }
    }

    public void InsertUpdateNote(ToDoItem item) {

        if (item.getToDoText().length() <= 0) {
            return;
        }
        boolean existed = false;

        if (item.hasReminder() && item.getToDoDate() != null) {
            Intent i = new Intent(getContext(), TodoNotificationService.class);
            i.putExtra(TodoNotificationService.TODOTEXT, item.getToDoText());
            i.putExtra(TodoNotificationService.TODOUUID, item.getIdentifier());
            createAlarm(i, item.getIdentifier().hashCode(), item.getToDoDate().getTime());
        }

        for (int i = 0; i < mToDoItemsArrayList.size(); i++) {
            if (item.getIdentifier().equals(mToDoItemsArrayList.get(i).getIdentifier())) {
                mToDoItemsArrayList.set(i, item);
                existed = true;
                adapter.notifyDataSetChanged();
                break;
            }
        }
        if (!existed) {
            addToDataStore(item);
        }

    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
    }

    private boolean doesPendingIntentExist(Intent i, int requestCode) {
        PendingIntent pi = PendingIntent.getService(getContext(), requestCode, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private void createAlarm(Intent i, int requestCode, long timeInMillis) {
        AlarmManager am = getAlarmManager();
        PendingIntent pi = PendingIntent.getService(getContext(), requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
    }

    private void deleteAlarm(Intent i, int requestCode) {
        if (doesPendingIntentExist(i, requestCode)) {
            PendingIntent pi = PendingIntent.getService(getContext(), requestCode, i, PendingIntent.FLAG_NO_CREATE);
            pi.cancel();
            getAlarmManager().cancel(pi);
            Log.d("OskarSchindler", "PI Cancelled " + doesPendingIntentExist(i, requestCode));
        }
    }

    private void addToDataStore(ToDoItem item) {
        mToDoItemsArrayList.add(item);
        adapter.notifyItemInserted(mToDoItemsArrayList.size() - 1);

    }

    public class BasicListAdapter extends RecyclerView.Adapter<BasicListAdapter.ViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {
        private ArrayList<ToDoItem> items;

        @Override
        public void onItemMoved(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(items, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(items, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemRemoved(final int position) {
            //Remove this line if not using Google Analytics


            mJustDeletedToDoItem = items.remove(position);
            //update sqlite


            mIndexOfDeletedToDoItem = position;
            Intent i = new Intent(getContext(), TodoNotificationService.class);
            deleteAlarm(i, mJustDeletedToDoItem.getIdentifier().hashCode());
            notifyItemRemoved(position);
            String toShow = "Todo";
            Snackbar.make(mCoordLayout, "Deleted " + toShow, Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> {

                        //Comment the line below if not using Google Analytics

                        items.add(mIndexOfDeletedToDoItem, mJustDeletedToDoItem);
                        if (mJustDeletedToDoItem.getToDoDate() != null && mJustDeletedToDoItem.hasReminder()) {
                            Intent i1 = new Intent(getContext(), TodoNotificationService.class);
                            i1.putExtra(TodoNotificationService.TODOTEXT, mJustDeletedToDoItem.getToDoText());
                            i1.putExtra(TodoNotificationService.TODOUUID, mJustDeletedToDoItem.getIdentifier());
                            createAlarm(i1, mJustDeletedToDoItem.getIdentifier().hashCode(), mJustDeletedToDoItem.getToDoDate().getTime());
                        }
                        notifyItemInserted(mIndexOfDeletedToDoItem);
                    }).show();
        }

        @Override
        public BasicListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final BasicListAdapter.ViewHolder holder, final int position) {
            ToDoItem item = items.get(position);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(THEME_PREFERENCES, MODE_PRIVATE);
            //Background color for each to-do item. Necessary for night/day mode
            int bgColor;
            //color of title text in our to-do item. White for night mode, dark gray for day mode
            int todoTextColor;

            if (sharedPreferences.getString(THEME_SAVED, LIGHTTHEME).equals(LIGHTTHEME)) {
                bgColor = Color.WHITE;
                todoTextColor = getResources().getColor(R.color.secondary_text);
            } else {
                bgColor = Color.DKGRAY;
                todoTextColor = Color.WHITE;
            }
            holder.linearLayout.setBackgroundColor(bgColor);

            holder.mToDoTextview.setText(item.getToDoText());

            if (item.getmToDoDescription().equals("")) {
                holder.textViewDesc.setVisibility(View.GONE);
                holder.mToDoTextview.setMaxLines(2);
                if (item.hasReminder() && item.getToDoDate() != null) {

                    holder.mTimeTextView.setVisibility(View.VISIBLE);

                } else {
                    holder.mTimeTextView.setVisibility(View.GONE);
                }

            } else {
                holder.textViewDesc.setText(item.getmToDoDescription());
                holder.textViewDesc.setVisibility(View.VISIBLE);
                holder.mToDoTextview.setMaxLines(1);


                if (item.hasReminder() && item.getToDoDate() != null) {

                    holder.mTimeTextView.setVisibility(View.VISIBLE);
                    holder.textViewDesc.setMaxLines(1);

                } else {
                    holder.mTimeTextView.setVisibility(View.GONE);
                    holder.textViewDesc.setMaxLines(2);

                }
            }


            holder.mToDoTextview.setTextColor(todoTextColor);
            TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                    .textColor(Color.WHITE)
                    .useFont(Typeface.DEFAULT)
                    .toUpperCase()
                    .endConfig()
                    .buildRound(item.getToDoText().substring(0, 1), item.getTodoColor());
            if (item.getToDoDate() != null) {
                String timeToShow;
                if (android.text.format.DateFormat.is24HourFormat(getContext())) {
                    timeToShow = AddToDoFragment.formatDate(MainFragment.DATE_TIME_FORMAT_24_HOUR, item.getToDoDate());
                } else {
                    timeToShow = AddToDoFragment.formatDate(MainFragment.DATE_TIME_FORMAT_12_HOUR, item.getToDoDate());
                }
                holder.mTimeTextView.setText(timeToShow);
            }
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int img_size = size.x / 8;
            if (item.getTodoImage().length() > 0) {

                holder.mColorImageView.setImageBitmap(StringToBitMap(item.getTodoImage()));
                holder.mColorImageView_1.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(0, 0);
                holder.mColorImageView_1.setLayoutParams(parms);
                holder.mColorImageView.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams parms_1 = new LinearLayout.LayoutParams(img_size, img_size);
                parms_1.leftMargin = 35;
                holder.mColorImageView.setLayoutParams(parms_1);

            } else {

                holder.mColorImageView_1.setImageDrawable(myDrawable);
                holder.mColorImageView.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(0, 0);
                holder.mColorImageView.setLayoutParams(parms);
                holder.mColorImageView_1.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams parms_1 = new LinearLayout.LayoutParams(img_size, img_size);
                parms_1.leftMargin = 35;
                holder.mColorImageView_1.setLayoutParams(parms_1);

            }


        }

        private int color;
        private Paint paint;
        private Rect rect;
        private RectF rectF;
        private Bitmap result;
        private Canvas canvas;
        private float roundPx;

        public Bitmap getRoundedRectBitmap(Bitmap bitmap, int pixels) {
            result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(result);

            color = 0xff424242;
            paint = new Paint();
            rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            rectF = new RectF(rect);
            roundPx = pixels;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return result;
        }

        /**
         * @param encodedString
         * @return bitmap (from given string)
         */
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

        /**
         * Showing popup menu when tapping on 3 dots
         */
        private void showPopupMenu(View view) {
            // inflate menu


            PopupMenu popup = new PopupMenu(getContext(), view, Gravity.CENTER);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_notes, popup.getMenu());
            popup.show();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        BasicListAdapter(ArrayList<ToDoItem> items) {

            this.items = items;
        }


        @SuppressWarnings("deprecation")
        public class ViewHolder extends RecyclerView.ViewHolder {

            View mView;
            LinearLayout linearLayout;
            TextView mToDoTextview;
            public de.hdodenhof.circleimageview.CircleImageView mColorImageView;
            public ImageView thumbnail, mColorImageView_1;
            TextView mTimeTextView, textViewDesc;

            public ViewHolder(View v) {
                super(v);
                mView = v;
                v.setOnClickListener(v1 -> {
                    ToDoItem item = items.get(ViewHolder.this.getAdapterPosition());
                    Intent i = new Intent(getContext(), AddToDoActivity.class);
                    i.putExtra(TODOITEM, item);
                    startActivityForResult(i, REQUEST_ID_TODO_ITEM);
                });
                mToDoTextview = v.findViewById(R.id.toDoListItemTextview);
                mTimeTextView = v.findViewById(R.id.todoListItemTimeTextView);
                textViewDesc = v.findViewById(R.id.todoListItemTextViewDec);
                mColorImageView = v.findViewById(R.id.toDoListItemColorImageView);
                mColorImageView_1 = v.findViewById(R.id.toDoListItemColorImageView_1);


                thumbnail = v.findViewById(R.id.thumbnail);
                linearLayout = v.findViewById(R.id.listItemLinearLayout);
            }


        }
    }

    public void saveData() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (prefs.getString(NOTE_SESSION, null) != null) {
                String NoteString = (prefs.getString(NOTE_SESSION, ""));
                if (NoteString.contains("Restored")) {

                    mToDoItemsArrayList = getLocallyStoredData(storeRetrieveData);

                }
                prefs.edit().remove(NOTE_SESSION).commit();

            }

            storeRetrieveData.saveToFile(mToDoItemsArrayList);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
        mRecyclerView.removeOnScrollListener(customRecyclerScrollViewListener);
    }

    @Override
    protected int layoutRes() {

        return R.layout.fragment_main;
    }

    public static MainFragment newInstance() {

        return new MainFragment();
    }
}
