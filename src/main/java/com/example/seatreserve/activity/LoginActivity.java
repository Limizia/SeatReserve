package com.example.seatreserve.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;
import com.example.seatreserve.bean.User;
import com.example.seatreserve.db.UserDao;

public class LoginActivity extends AppCompatActivity {//登陆页面

    private EditText etAccount, etPwd;
    private Button btnLogin;
    private UserDao userDao;//用户数据库操作类，封装用户登录查询逻辑

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userDao = new UserDao(this);//实例化用户

        etAccount = findViewById(R.id.et_account);//账号
        etPwd = findViewById(R.id.et_pwd);//密码
        btnLogin = findViewById(R.id.btn_login);//按钮

        btnLogin.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String pwd = etPwd.getText().toString().trim();//转为字符串，去掉首尾空格
            if(account.isEmpty() || pwd.isEmpty()){
                Toast.makeText(LoginActivity.this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
                return;//弹窗提示
            }
            User loginUser = userDao.login(account,pwd);//调用dao的login方法，去数据库查询
            if(loginUser != null){
                if(loginUser.getBreakRule() >=5){
                    Toast.makeText(LoginActivity.this,"本月违约次数已达上限，下月才能预约座位！",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoginActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);//跳转
                intent.putExtra("login_account",account);
                startActivity(intent);
            }else{
                Toast.makeText(LoginActivity.this,"账号或密码错误",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
