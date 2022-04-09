package ir.javadroid.sqlite_sample;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "user.db";
    private static final String TBL_NAME = "private_info";
    private static final String TBL_NAME2 = "user_info";


    private static int DB_VERSION = 3;


    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBL_NAME + " (Id INTEGER PRIMARY KEY ,User_Name VARCAHR(100),User_Address VARCHAR(1000))");
        db.execSQL("CREATE TABLE " + TBL_NAME2 + " (Id INTEGER PRIMARY KEY , Favorite_User_Code VARCHAR(50))");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME2);
        onCreate(db);

    }

    //----------------------------------- تغییر یافته ---------------------
    public boolean insertUser(int id, String name, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Id", id);
        values.put("User_Name", name); // نام فیلد اشتباه درج شده بود
        values.put("User_Address", address);// نام فیلد اشتباه درج شده بود
        long result = db.insert(TBL_NAME, null, values);
        db.close();
        if (result == -1)
            return false;
        else
            return true;
    }


    public boolean removeUser(String id) {
        SQLiteDatabase db = getWritableDatabase();
        Long lastItemId = DatabaseUtils.queryNumEntries(db, TBL_NAME);
        long result = db.delete(TBL_NAME, "Id=?", new String[]{id});

        if (lastItemId != Integer.parseInt(id)) {
            int check = (int) (lastItemId - Integer.parseInt(id));
            for (int i = 1; i <= check; i++) {
                ContentValues values = new ContentValues();
                values.put("Id", id);
                int newId = Integer.parseInt(id) + 1;
                db.update(TBL_NAME, values, "id=?", new String[]{String.valueOf(newId)});
                id = String.valueOf(Integer.parseInt(id) + 1);
            }
        }
        if (result < 1)
            return false;
        else
            return true;
    }


    public void dropTable(String TableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TableName, null, null);
        db.close();
    }



    //لیست یوزرهای دیتابیس
    @SuppressLint("Range")
    public ArrayList<ModelUser> getAllUsers() {
        ArrayList<ModelUser> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TBL_NAME, null);
        if (res.moveToFirst()) {
            do {
                //Id INTEGER PRIMARY KEY ,User_Name VARCAHR(100),User_Address
                userList.add(new ModelUser(
                        res.getInt(res.getColumnIndex("Id")),
                        res.getString(res.getColumnIndex("User_Name")),
                        res.getString(res.getColumnIndex("User_Address"))
                ));
            } while (res.moveToNext());
        }

        return userList;
    }

    //حذف یک یوزر
    @SuppressLint("Range")
    public void deleteUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TBL_NAME, "Id=?", new String[]{Integer.toString(userId)});
    }


    //باز کردن مجدد دیتابیس
    public MyDatabaseHelper reopen(Context context) {
        return new MyDatabaseHelper(context);
    }
}
