package com.example.seatreserve.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;

public class LibraryActivity extends AppCompatActivity {

    private Button btnLib1, btnLib2;
    private String loginAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        loginAccount = getIntent().getStringExtra("login_account");
//拿到跳转到本页的意图，取出上个页面传来的登陆账号
        btnLib1 = findViewById(R.id.btn_lib1);
        btnLib2 = findViewById(R.id.btn_lib2);

        //图书馆编号 1、2，传给楼层页面
        btnLib1.setOnClickListener(v -> {//按钮点击事件
            Intent intent = new Intent(LibraryActivity.this, FloorActivity.class);
            intent.putExtra("login_account", loginAccount);
            intent.putExtra("lib_id", 1);//putExtra携带参数
            startActivity(intent);
        });
        btnLib2.setOnClickListener(v -> {
            Intent intent = new Intent(LibraryActivity.this, FloorActivity.class);
            intent.putExtra("login_account", loginAccount);
            intent.putExtra("lib_id", 2);
            startActivity(intent);
        });
    }
}
