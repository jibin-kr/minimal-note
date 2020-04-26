package com.example.calyx.mykeep.Utility;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;

public class StoreRetrieveData {
    private String mFileName;
    private  Context mContext;
    public dataBaseHelper dbHelper;
    Utils utl;


    public StoreRetrieveData(Context context, String filename) {
        mFileName = filename;
        dbHelper = new dataBaseHelper(context);
        mContext=context;
        utl=new Utils();

    }

    public static JSONArray toJSONArray(ArrayList<ToDoItem> items) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ToDoItem item : items) {
            JSONObject jsonObject = item.toJSON();
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public void saveToFile(ArrayList<ToDoItem> items) throws JSONException, IOException {
        dbHelper.deleteAllDatas();

        String encrypteData=utl.encrypt(toJSONArray(items).toString(),"123");
        AddData(encrypteData);

    }

    public ArrayList<ToDoItem> loadFromFile() throws IOException, JSONException {
        ArrayList<ToDoItem> items = new ArrayList<>();
        try {
            Cursor data = dbHelper.getData();
            if (data != null && data.getCount() > 0) {
                while (data.moveToNext()) {
                    //get the value from the database in column 1
                    //then add it to the ArrayListif
                    if (!data.isNull(1)) {
                        String decriptSting=utl.decrypt(data.getString(1),"123");
                        if(decriptSting.contains("INVALID")) {
                             decriptSting=data.getString(1);
                        }

                        JSONArray jsonArray = (JSONArray) new JSONTokener(decriptSting).nextValue();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            ToDoItem item = new ToDoItem(jsonArray.getJSONObject(i));
                            items.add(item);
                        }

                    }
                }
            }

        } finally {

        }
        return items;
    }

    public void AddData(String newEntry) {
        boolean insertData = dbHelper.addData(newEntry);

        if (insertData) {

        } else {

        }
    }


}
