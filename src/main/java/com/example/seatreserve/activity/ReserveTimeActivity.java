package com.example.seatreserve.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;
import com.example.seatreserve.bean.Reservation;
import com.example.seatreserve.db.ReservationDao;
import com.example.seatreserve.db.UserDao;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReserveTimeActivity extends AppCompatActivity {

    private EditText etStart, etEnd;
    private Button btnSubmit;
    private String loginAccount;
    private int libId, floor, seatNum;
    private ReservationDao reservationDao;
    private UserDao userDao;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    // 存选中的时间，不再依赖从文本框解析！
    private Calendar calStart = null;
    private Calendar calEnd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_time);

        loginAccount = getIntent().getStringExtra("login_account");
        libId = getIntent().getIntExtra("lib_id", 1);
        floor = getIntent().getIntExtra("floor", 1);
        seatNum = getIntent().getIntExtra("seat_num", 1);

        reservationDao = new ReservationDao(this);
        userDao = new UserDao(this);

        etStart = findViewById(R.id.et_start);
        etEnd = findViewById(R.id.et_end);
        btnSubmit = findViewById(R.id.btn_submit);

        etStart.setOnClickListener(v -> showDateTimePicker(etStart, true));
        etEnd.setOnClickListener(v -> showDateTimePicker(etEnd, false));

        btnSubmit.setOnClickListener(v -> {
            if (calStart == null || calEnd == null) {
                Toast.makeText(ReserveTimeActivity.this, "请选择起止时间", Toast.LENGTH_SHORT).show();
                return;
            }

            long startMs = calStart.getTimeInMillis();
            long endMs = calEnd.getTimeInMillis();
            long now = System.currentTimeMillis();

            if (startMs < now) {
                Toast.makeText(ReserveTimeActivity.this, "不能预约已经过去的时间！", Toast.LENGTH_SHORT).show();
                return;
            }

            long duration = endMs - startMs;
            if (duration > 4 * 60 * 60 * 1000L) {
                Toast.makeText(ReserveTimeActivity.this, "预约不能超过4小时！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (endMs <= startMs) {
                Toast.makeText(ReserveTimeActivity.this, "结束时间必须晚于开始时间！", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reservationDao.hasOngoingReserve(loginAccount)) {
                Toast.makeText(ReserveTimeActivity.this, "你还有未结束的预约，暂时不能预约新座位", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reservationDao.isUserBreakLimit(loginAccount)) {
                Toast.makeText(ReserveTimeActivity.this, "本月违约已达5次，禁止预约！", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reservationDao.hasConflict(libId, floor, seatNum, startMs, endMs)) {
                Toast.makeText(ReserveTimeActivity.this, "该座位这个时间段已经被预约！", Toast.LENGTH_SHORT).show();
                return;
            }

            Reservation res = new Reservation();
            res.setAccount(loginAccount);
            res.setLibrary(libId);
            res.setFloor(floor);
            res.setSeatNum(seatNum);
            res.setStartTime(startMs);
            res.setEndTime(endMs);
            res.setEndTimeOriginal(endMs);
            res.setIsSign(0);
            res.setIsBreak(0);
            res.setStatus(0);

            boolean ok = reservationDao.addReservation(res);
            if (ok) {
                userDao.updateHasUsing(loginAccount, 1);
                Toast.makeText(ReserveTimeActivity.this, "预约成功！", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ReserveTimeActivity.this, "预约失败！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * @param isStart 是否是开始时间
     */
    private void showDateTimePicker(EditText et, boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            int m = month + 1;
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year, month, dayOfMonth, hourOfDay, minute, 0);

                String dateStr = String.format(Locale.CHINA,
                        "%04d-%02d-%02d %02d:%02d",
                        year, m, dayOfMonth, hourOfDay, minute);
                et.setText(dateStr);

                if (isStart) {
                    calStart = selectedCal;
                } else {
                    calEnd = selectedCal;
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}
