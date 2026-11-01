package com.example.seatreserve.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;
import com.example.seatreserve.db.ReservationDao;
import java.util.List;
import com.example.seatreserve.bean.Reservation;

public class PersonalActivity extends AppCompatActivity {

    private TextView tvAccount, tvBreakCount;
    private Button btnLogout;
    private String loginAccount;
    private ReservationDao reservationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        loginAccount = getIntent().getStringExtra("login_account");
        reservationDao = new ReservationDao(this);

        tvAccount = findViewById(R.id.tv_account);
        tvBreakCount = findViewById(R.id.tv_break_count);
        btnLogout = findViewById(R.id.btn_logout);

        tvAccount.setText("账号：" + loginAccount);

        //统计违约记录
        List<Reservation> allRes = reservationDao.getMyReservation(loginAccount);
        int breakNum = 0;
        for(Reservation r : allRes){
            if(r.getIsBreak() == 1){
                breakNum++;
            }
        }
        tvBreakCount.setText("违约次数：" + breakNum);

        //退出登录，回到登录页面（LoginActivity）
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalActivity.this, LoginActivity.class);
            //清空任务栈，不能返回
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }
}
