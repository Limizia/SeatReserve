package com.example.seatreserve.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;

public class MainActivity extends AppCompatActivity {

    private LinearLayout btnChooseLib, btnMyReserve, btnPersonal;
    private String loginAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginAccount = getIntent().getStringExtra("login_account");

        btnChooseLib = findViewById(R.id.btn_choose_lib);
        btnMyReserve = findViewById(R.id.btn_my_reserve);
        btnPersonal = findViewById(R.id.btn_personal);

        btnChooseLib.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
            intent.putExtra("login_account", loginAccount);
            startActivity(intent);
        });

        btnMyReserve.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyReserveActivity.class);
            intent.putExtra("login_account", loginAccount);
            startActivity(intent);
        });

        btnPersonal.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PersonalActivity.class);
            intent.putExtra("login_account", loginAccount);
            startActivity(intent);
        });
    }
}
