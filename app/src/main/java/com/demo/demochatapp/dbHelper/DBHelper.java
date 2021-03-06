package com.demo.demochatapp.dbHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.demo.demochatapp.models.ChatItemModel;

public class DBHelper extends SQLiteOpenHelper implements BaseColumns {
    private static final String DB_NAME = "DemoChatApp";
    private static final int DB_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table_chat_master = "CREATE TABLE IF NOT EXISTS " + ChatItemModel.TABLE_NAME + " (" +
                ChatItemModel.KEY_MESSAGE_ID + " INTEGER PRIMARY KEY, " +
                ChatItemModel.KEY_MESSAGE_CONTENT_TYPE + " TEXT, " +
                ChatItemModel.KEY_MESSAGE_CONTENT +  " TEXT, " +
                ChatItemModel.KEY_SENDER_ID + " TEXT, " +
                ChatItemModel.KEY_MESSAGE_SENT_DATETIME + " TEXT, " +
                ChatItemModel.KEY_IS_MESSAGE_SENT_SUCCESSFULLY + " INTEGER, " +
                ChatItemModel.KEY_IS_MESSAGE_PUBLISHED_SUCCESSFULLY + " INTEGER)";

        db.execSQL(create_table_chat_master);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ChatItemModel.TABLE_NAME);
        onCreate(db);
    }
}
