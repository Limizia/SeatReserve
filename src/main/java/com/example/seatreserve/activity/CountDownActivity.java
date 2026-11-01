package com.example.seatreserve.activity;

import android.app.AlertDialog;
import android.os.Bundle;//保存页面状态
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;//弹出短暂文字提示
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;
import com.example.seatreserve.bean.Reservation;
import com.example.seatreserve.db.ReservationDao;
import java.util.Locale;//本地化工具

public class CountDownActivity extends AppCompatActivity {

    private TextView tvCount, tvTip;//倒计时，提示文字
    private Button btnBack, btnRenew;
    private CountDownTimer countDownTimer;//倒计时对象
    private ReservationDao reservationDao;//预约数据库对象

    @Override//重写父类
    protected void onCreate(Bundle savedInstanceState) {//onCreate页面创建时自动执行
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);//绑定布局文件

        tvTip = findViewById(R.id.tv_tip);
        tvCount = findViewById(R.id.tv_count);
        btnBack = findViewById(R.id.btn_back);
        btnRenew = findViewById(R.id.btn_renew);

        reservationDao = new ReservationDao(this);//新建

        long startMs = getIntent().getLongExtra("start_ms", 0);//getIntent()接收上一个位面的数据
        long endMs = getIntent().getLongExtra("end_ms", 0);//endMS预约时间结束
        int reserveId = getIntent().getIntExtra("reserve_id", -1);
        String loginAccount = getIntent().getStringExtra("login_account");

        startTimer(endMs);

        btnBack.setOnClickListener(v -> finish());//按钮

        btnRenew.setOnClickListener(v -> {
            long[] addTimeArr = {
                    1 * 60 * 60 * 1000L,
                    2 * 60 * 60 * 1000L,
                    3 * 60 * 60 * 1000L,
                    4 * 60 * 60 * 1000L
            };//60分*60秒*1000毫秒
            String[] optionArr = {"续约1小时", "续约2小时", "续约3小时", "续约4小时"};//单位毫秒

            AlertDialog.Builder builder = new AlertDialog.Builder(CountDownActivity.this);//弹窗构造器
            builder.setTitle("请选择续约时长");
            builder.setItems(optionArr, (dialog, which) -> {//setItems:加载选项列表
                long addTime = addTimeArr[which];
                long newEndTime = endMs + addTime;

                String msg = reservationDao.renewReserve(reserveId, newEndTime);//调用方法
                Toast.makeText(CountDownActivity.this, msg, Toast.LENGTH_SHORT).show();
                if ("续约成功".equals(msg)) {
                    Reservation newRes = reservationDao.getUsingReserve(loginAccount);
                    if (newRes != null) {
                        restartTimer(newRes.getEndTime());
                    }
                }
            });
            builder.show();//把弹窗显示出来
        });
    }

    private void startTimer(long endMs) {
        long now = System.currentTimeMillis();
        long remainTime = endMs - now;//剩余时间
        if (remainTime <= 0) {
            tvCount.setText("00:00:00");
            tvTip.setText("座位使用时间已结束");
            btnRenew.setEnabled(false);
            return;
        }
        countDownTimer = new CountDownTimer(remainTime, 1000) {//倒计时器，总时长，1s回调一次
            @Override
            public void onTick(long millisUntilFinished) {
                updateUI(millisUntilFinished);
            }
//onTick:每隔一秒执行一次，millisUntilFinished剩余时间
            @Override
            public void onFinish() {//倒计时结束自动回调
                tvCount.setText("00:00:00");
                tvTip.setText("座位使用时间已结束");
                btnRenew.setEnabled(false);
            }
        }.start();
    }

    private void restartTimer(long newEndMs) {
        if (countDownTimer != null) {
            countDownTimer.cancel();//停止旧的倒计时
        }
        startTimer(newEndMs);
    }

    private void updateUI(long ms) {//刷新时间
        long hour = ms / (1000 * 60 * 60);
        long minute = (ms % (1000 * 60 * 60)) / (1000 * 60);
        long second = (ms % (1000 * 60)) / 1000;
        String timeText = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
                        //格式化字符串
        tvCount.setText(timeText);
        tvTip.setText("距离座位结束还剩");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
