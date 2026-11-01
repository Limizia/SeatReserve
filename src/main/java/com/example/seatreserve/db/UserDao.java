package com.example.seatreserve.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.seatreserve.bean.User;

public class UserDao {
    private DBHelper dbHelper;

    public UserDao(Context context){
        dbHelper = new DBHelper(context);
    }

    //登录查询：根据账号密码查询用户
    public User login(String account, String pwd){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USER,
                null,
                DBHelper.USER_ACCOUNT + "=? AND " + DBHelper.USER_PWD + "=?",
                new String[]{account, pwd},
                null,null,null);
        User user = null;
        if(cursor.moveToFirst()){
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.USER_ID)));
            user.setAccount(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_ACCOUNT)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_PWD)));
            user.setBreakRule(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.USER_BREAK_RULE)));
            user.setHasUsing(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.USER_HAS_USING)));
        }
        cursor.close();
        db.close();
        return user;
    }

    //更新用户【是否正在使用座位】的状态
    public void updateHasUsing(String account, int status){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.USER_HAS_USING, status);
        db.update(DBHelper.TABLE_USER, values, DBHelper.USER_ACCOUNT+"=?", new String[]{account});
        db.close();
    }

    //增加违约次数
    public void addBreakCount(String account){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USER,new String[]{DBHelper.USER_BREAK_RULE},
                DBHelper.USER_ACCOUNT+"=?",new String[]{account},null,null,null);
        int count = 0;
        if(cursor.moveToFirst()){
            count = cursor.getInt(0);
        }
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(DBHelper.USER_BREAK_RULE, count+1);
        db.update(DBHelper.TABLE_USER,values,DBHelper.USER_ACCOUNT+"=?",new String[]{account});
        db.close();
    }
}
