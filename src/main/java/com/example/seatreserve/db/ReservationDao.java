package com.example.seatreserve.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.seatreserve.bean.Reservation;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReservationDao {
    private final DBHelper dbHelper;

    public ReservationDao(Context context){
        dbHelper = new DBHelper(context);
    }

    //新增预约记录
    public boolean addReservation(Reservation res){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.RES_ACCOUNT, res.getAccount());
        values.put(DBHelper.RES_LIB, res.getLibrary());
        values.put(DBHelper.RES_FLOOR, res.getFloor());
        values.put(DBHelper.RES_SEAT, res.getSeatNum());
        values.put(DBHelper.RES_START, res.getStartTime());
        values.put(DBHelper.RES_END, res.getEndTime());
        values.put(DBHelper.RES_END_ORIGINAL, res.getEndTimeOriginal());
        values.put(DBHelper.RES_IS_SIGN, res.getIsSign());
        values.put(DBHelper.RES_IS_BREAK, res.getIsBreak());
        values.put(DBHelper.RES_STATUS, res.getStatus());
        long result = db.insert(DBHelper.TABLE_RESERVE,null,values);
        db.close();
        return result != -1;
    }

    //查询【同一个图书馆+楼层+座位】在时间段内有没有冲突预约
    public boolean hasConflict(int lib, int floor, int seat, long start, long end){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.RES_LIB + "=? AND " + DBHelper.RES_FLOOR + "=? AND "
                + DBHelper.RES_SEAT + "=? AND " + DBHelper.RES_STATUS + "=0 AND "
                + DBHelper.RES_START + " < ? AND " + DBHelper.RES_END + " > ?";
        String[] args = {lib+"",floor+"",seat+"", end+"", start+""};
        Cursor cursor = db.query(DBHelper.TABLE_RESERVE,null,selection,args,null,null,null);
        boolean conflict = cursor.getCount()>0;
        cursor.close();
        db.close();
        return conflict;
    }

    // 查询用户是否存在正在进行中的预约 status=0
    public boolean hasOngoingReserve(String account){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_RESERVE,
                new String[]{DBHelper.RES_ID},
                DBHelper.RES_ACCOUNT + "=? AND " + DBHelper.RES_STATUS + "=0",
                new String[]{account},
                null,null,null);
        boolean has = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return has;
    }

    //查询当前用户所有预约记录
    public List<Reservation> getMyReservation(String account){
        List<Reservation> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_RESERVE,null,
                DBHelper.RES_ACCOUNT+"=?",new String[]{account},null,null,null);
        if(cursor.moveToFirst()){
            do{
                Reservation r = new Reservation();
                r.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_ID)));
                r.setAccount(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.RES_ACCOUNT)));
                r.setLibrary(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_LIB)));
                r.setFloor(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_FLOOR)));
                r.setSeatNum(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_SEAT)));
                r.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.RES_START)));
                r.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.RES_END)));
                r.setEndTimeOriginal(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.RES_END_ORIGINAL)));
                r.setIsSign(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_IS_SIGN)));
                r.setIsBreak(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_IS_BREAK)));
                r.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_STATUS)));
                list.add(r);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    //更新签到状态
    public boolean updateSignStatus(int resId, int isSign){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.RES_IS_SIGN,isSign);
        int rows = db.update(DBHelper.TABLE_RESERVE,values,DBHelper.RES_ID+"=?",new String[]{resId+""});
        db.close();
        return rows>0;
    }

    //更新预约状态（1=已结束，2=提前释放）
    public boolean updateStatus(int resId, int status){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.RES_STATUS,status);
        int rows = db.update(DBHelper.TABLE_RESERVE,values,DBHelper.RES_ID+"=?",new String[]{resId+""});
        db.close();
        return rows>0;
    }

    // 获取当前年月，格式 yyyyMM
    private int getCurrentYM(){
        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH)+1;
        return y*100 + m;
    }

    // 判断用户本月违约是否已满5次，true=禁止预约
    public boolean isUserBreakLimit(String account){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int nowYM = getCurrentYM();
        String sql = "SELECT " + DBHelper.USER_BREAK_RULE + "," + DBHelper.USER_LAST_BREAK_MONTH
                + " FROM " + DBHelper.TABLE_USER + " WHERE " + DBHelper.USER_ACCOUNT + "=?";
        Cursor cursor = db.rawQuery(sql,new String[]{account});
        int count = 0;
        if(cursor.moveToFirst()){
            int ym = cursor.getInt(1);
            if(ym == nowYM){
                count = cursor.getInt(0);
            }else{
                //跨月，重置本月违约次数
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.USER_BREAK_RULE,0);
                cv.put(DBHelper.USER_LAST_BREAK_MONTH,nowYM);
                db.update(DBHelper.TABLE_USER,cv,DBHelper.USER_ACCOUNT+"=?",new String[]{account});
                count = 0;
            }
        }
        cursor.close();
        db.close();
        return count >=5;
    }

    // 检查超时未签到预约，自动标记违约，同步累加本月违约次数
    public int checkBreak(String account){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();
        int nowYM = getCurrentYM();
        String where = DBHelper.RES_ACCOUNT+"=? AND " + DBHelper.RES_STATUS + "=0 AND "
                + DBHelper.RES_IS_SIGN + "=0 AND "+DBHelper.RES_END+"<?";
        String[] args = {account, now+""};

        Cursor c = db.query(DBHelper.TABLE_RESERVE,new String[]{DBHelper.RES_ID},where,args,null,null,null);
        int breakNum = c.getCount();
        c.close();
        if(breakNum <=0){
            db.close();
            return 0;
        }
        // 更新预约记录为违约
        ContentValues resCv = new ContentValues();
        resCv.put(DBHelper.RES_IS_BREAK,1);
        resCv.put(DBHelper.RES_STATUS,1);
        db.update(DBHelper.TABLE_RESERVE,resCv,where,args);

        // 更新用户表本月违约计数
        Cursor userCur = db.query(DBHelper.TABLE_USER,
                new String[]{DBHelper.USER_BREAK_RULE,DBHelper.USER_LAST_BREAK_MONTH},
                DBHelper.USER_ACCOUNT+"=?",new String[]{account},null,null,null);
        if(userCur.moveToFirst()){
            int userYM = userCur.getInt(1);
            int newCnt;
            if(userYM == nowYM){
                newCnt = userCur.getInt(0)+breakNum;
            }else{
                newCnt = breakNum;
            }
            ContentValues userCv = new ContentValues();
            userCv.put(DBHelper.USER_BREAK_RULE,newCnt);
            userCv.put(DBHelper.USER_LAST_BREAK_MONTH,nowYM);
            db.update(DBHelper.TABLE_USER,userCv,DBHelper.USER_ACCOUNT+"=?",new String[]{account});
        }
        userCur.close();
        db.close();
        return breakNum;
    }

    // 查询用户当前正在进行的预约
    public Reservation getUsingReserve(String account){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_RESERVE,
                null,
                DBHelper.RES_ACCOUNT+"=? AND "+DBHelper.RES_STATUS+"=0",
                new String[]{account},
                null,null,DBHelper.RES_START+" ASC");
        Reservation r = null;
        if(cursor.moveToFirst()){
            r = new Reservation();
            r.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_ID)));
            r.setAccount(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.RES_ACCOUNT)));
            r.setLibrary(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_LIB)));
            r.setFloor(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_FLOOR)));
            r.setSeatNum(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_SEAT)));
            r.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.RES_START)));
            r.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.RES_END)));
            r.setEndTimeOriginal(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.RES_END_ORIGINAL)));
            r.setIsSign(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_IS_SIGN)));
            r.setIsBreak(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_IS_BREAK)));
            r.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.RES_STATUS)));
        }
        cursor.close();
        db.close();
        return r;
    }

    //续约
    public String renewReserve(int reserveId, long newEndTime){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_RESERVE,
                new String[]{DBHelper.RES_START,DBHelper.RES_END,DBHelper.RES_END_ORIGINAL},
                DBHelper.RES_ID+"=?",
                new String[]{reserveId+""},
                null,null,null);
        if(!cursor.moveToFirst()){
            cursor.close();
            db.close();
            return "预约记录不存在";
        }
        long start = cursor.getLong(0);
        long oldEnd = cursor.getLong(1);
        cursor.close();

        long now = System.currentTimeMillis();
        //距离结束超过20分钟，不能续约
        if(oldEnd - now > 20*60*1000){
            db.close();
            return "距离结束还有很久，仅最后20分钟可以续约";
        }
        //总时长不能超过4小时
        long maxTotalEnd = start + 4*60*60*1000;
        if(newEndTime > maxTotalEnd){
            db.close();
            return "单次预约总时长不能超过4小时，续约失败";
        }
        //座位时间段冲突校验
        Cursor conflict = db.query(DBHelper.TABLE_RESERVE,
                null,
                DBHelper.RES_ID + " != ? AND " + DBHelper.RES_LIB + "=(SELECT "+DBHelper.RES_LIB+" FROM "+DBHelper.TABLE_RESERVE+" WHERE "+DBHelper.RES_ID+"=?) "
                        + "AND "+DBHelper.RES_FLOOR+"=(SELECT "+DBHelper.RES_FLOOR+" FROM "+DBHelper.TABLE_RESERVE+" WHERE "+DBHelper.RES_ID+"=?) "
                        + "AND "+DBHelper.RES_SEAT+"=(SELECT "+DBHelper.RES_SEAT+" FROM "+DBHelper.TABLE_RESERVE+" WHERE "+DBHelper.RES_ID+"=?) "
                        + "AND "+DBHelper.RES_STATUS+"=0 AND "+DBHelper.RES_START+" < ? AND "+DBHelper.RES_END+" > ?",
                new String[]{reserveId+"",reserveId+"",reserveId+"",reserveId+"",newEndTime+"",now+""},
                null,null,null);
        if(conflict.getCount()>0){
            conflict.close();
            db.close();
            return "该座位此时间段已被占用，续约失败";
        }
        conflict.close();

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.RES_END,newEndTime);
        db.update(DBHelper.TABLE_RESERVE,cv,DBHelper.RES_ID+"=?",new String[]{reserveId+""});
        db.close();
        return "续约成功";
    }
}
