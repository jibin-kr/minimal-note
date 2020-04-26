package com.example.calyx.mykeep.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class ToDoItem_alpha implements Serializable {
    private String a;
    private boolean b;
    //add description
    private String d;
    //    private Date mLastEdited;
    private int e;
    private Date c;
    private UUID f;
    private String g;
    //add description
    private static final String TODODESCRIPTION = "d";
    private static final String TODOTEXT = "a";
    private static final String TODOREMINDER = "b";
    //    private static final String TODOLASTEDITED = "todolastedited";
    private static final String TODOCOLOR = "e";
    private static final String TODODATE = "c";
    private static final String TODOIDENTIFIER = "f";
    private static final String TODO_IMAGE = "g";


    public ToDoItem_alpha(String todoBody, String tododescription, boolean hasReminder, Date toDoDate, String img) {
        a = todoBody;
        b = hasReminder;
        c = toDoDate;
        d = tododescription;
        e = 1677725;
        f = UUID.randomUUID();
        g=img;
    }

    public ToDoItem_alpha(JSONObject jsonObject) throws JSONException {
        a = jsonObject.getString(TODOTEXT);
        d = jsonObject.getString(TODODESCRIPTION);
        b = jsonObject.getBoolean(TODOREMINDER);
        e = jsonObject.getInt(TODOCOLOR);

        f = UUID.fromString(jsonObject.getString(TODOIDENTIFIER));

//        if(jsonObject.has(TODOLASTEDITED)){
//            mLastEdited = new Date(jsonObject.getLong(TODOLASTEDITED));
//        }
        if (jsonObject.has(TODODATE)) {
            c = new Date(jsonObject.getLong(TODODATE));
        }
        g  = jsonObject.getString(TODO_IMAGE);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TODOTEXT, a);
        jsonObject.put(TODOREMINDER, b);
        jsonObject.put(TODODESCRIPTION, d);
//        jsonObject.put(TODOLASTEDITED, mLastEdited.getTime());
        if (c != null) {
            jsonObject.put(TODODATE, c.getTime());
        }
        jsonObject.put(TODOCOLOR, e);
        jsonObject.put(TODOIDENTIFIER, f.toString());
        jsonObject.put(TODO_IMAGE,g==null?"": g.toString());

        return jsonObject;
    }


    public ToDoItem_alpha() {
        this("Clean my room","Sweep and Mop my Room", true, new Date(),null);
    }

    public String getmToDoDescription() { return d;}

    public void setmToDoDescription(String mToDoDescription){this.d = mToDoDescription;}

    public String getToDoText() {
        return a;
    }

    public void setToDoText(String mToDoText) {
        this.a = mToDoText;
    }

    public boolean hasReminder() {
        return b;
    }

    public void setHasReminder(boolean mHasReminder) {
        this.b = mHasReminder;
    }

    public Date getToDoDate() {
        return c;
    }

    public int getTodoColor() {
        return e;
    }

    public void setTodoColor(int mTodoColor) {
        this.e = mTodoColor;
    }

    public void setToDoDate(Date mToDoDate) {
        this.c = mToDoDate;
    }


    public UUID getIdentifier() {
        return f;
    }

    public String getTodoImage(){
        return g;
    }
    public void setTodoImage(String mTodoImage) {
        this.g = mTodoImage;
    }
}

