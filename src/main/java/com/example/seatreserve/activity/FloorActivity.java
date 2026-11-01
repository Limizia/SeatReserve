package com.example.seatreserve.activity;

import android.content.Intent;//页面跳转类
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;//资源类

public class FloorActivity extends AppCompatActivity {

    private String loginAccount;
    private int libId;//图书馆编号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor);//绑定布局
        loginAccount = getIntent().getStringExtra("login_account");
        libId = getIntent().getIntExtra("lib_id", 1);//1为默认值，取不到数据就用1

        Button btnF1 = findViewById(R.id.btn_f1);
        Button btnF2 = findViewById(R.id.btn_f2);
        Button btnF3 = findViewById(R.id.btn_f3);
        Button btnF4 = findViewById(R.id.btn_f4);
        Button btnF5 = findViewById(R.id.btn_f5);

        //选中楼层，传到座位页面
        btnF1.setOnClickListener(v -> jumpToSeat(1));//给按钮设置点击监听
        btnF2.setOnClickListener(v -> jumpToSeat(2));
        btnF3.setOnClickListener(v -> jumpToSeat(3));
        btnF4.setOnClickListener(v -> jumpToSeat(4));
        btnF5.setOnClickListener(v -> jumpToSeat(5));
    }

    private void jumpToSeat(int floor){
        Intent intent = new Intent(FloorActivity.this, SeatActivity.class);//构建跳转意图
        intent.putExtra("login_account", loginAccount);//携带参数传给座位页
        intent.putExtra("lib_id", libId);
        intent.putExtra("floor", floor);
        startActivity(intent);
    }
}
