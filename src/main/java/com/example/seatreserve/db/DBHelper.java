package com.example.seatreserve.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class   DBHelper extends SQLiteOpenHelper {

    //数据库名、版本
    private static final String DB_NAME = "SeatReserve.db";
    private static final int DB_VERSION = 2; //版本号从1改成2，触发升级

    //用户表
    public static final String TABLE_USER = "user";
    public static final String USER_ID = "_id";
    public static final String USER_ACCOUNT = "account";
    public static final String USER_PWD = "password";
    public static final String USER_BREAK_RULE = "break_rule"; //本月违约次数
    public static final String USER_HAS_USING = "has_using"; //0=无正在使用预约，1=有（不能再预约）
    public static final String USER_LAST_BREAK_MONTH = "last_break_month"; //上次违约年月 yyyyMM

    //预约表
    public static final String TABLE_RESERVE = "reservation";
    public static final String RES_ID = "_id";
    public static final String RES_ACCOUNT = "account";
    public static final String RES_LIB = "library"; //图书馆 1/2
    public static final String RES_FLOOR = "floor"; //楼层1~5
    public static final String RES_SEAT = "seat_num"; //座位号 1~24
    public static final String RES_START = "start_time"; //预约开始时间（毫秒long）
    public static final String RES_END = "end_time"; //预约结束时间（毫秒long）
    public static final String RES_END_ORIGINAL = "end_time_original"; //原始结束时间，续约用
    public static final String RES_IS_SIGN = "is_sign"; //是否签到 0未签到 1已签到
    public static final String RES_IS_BREAK = "is_break"; //是否违约 0否 1是
    public static final String RES_STATUS = "status"; //0进行中 1已结束 2提前释放

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建用户表
        String sqlUser = "CREATE TABLE " + TABLE_USER + "("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_ACCOUNT + " TEXT UNIQUE,"
                + USER_PWD + " TEXT,"
                + USER_BREAK_RULE + " INTEGER DEFAULT 0,"
                + USER_HAS_USING + " INTEGER DEFAULT 0,"
                + USER_LAST_BREAK_MONTH + " INTEGER DEFAULT 0)";
        db.execSQL(sqlUser);

        //创建预约表
        String sqlReserve = "CREATE TABLE " + TABLE_RESERVE + "("
                + RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RES_ACCOUNT + " TEXT,"
                + RES_LIB + " INTEGER,"
                + RES_FLOOR + " INTEGER,"
                + RES_SEAT + " INTEGER,"
                + RES_START + " LONG,"
                + RES_END + " LONG,"
                + RES_END_ORIGINAL + " LONG,"
                + RES_IS_SIGN + " INTEGER DEFAULT 0,"
                + RES_IS_BREAK + " INTEGER DEFAULT 0,"
                + RES_STATUS + " INTEGER DEFAULT 0)";
        db.execSQL(sqlReserve);

        db.execSQL("INSERT INTO user(account,password) VALUES('stu001','123456')");
        db.execSQL("INSERT INTO user(account,password) VALUES('stu002','123456')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVE);
        onCreate(db);
    }
}
