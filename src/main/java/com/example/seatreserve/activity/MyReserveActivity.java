package com.example.seatreserve.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;
import com.example.seatreserve.bean.Reservation;
import com.example.seatreserve.db.ReservationDao;
import com.example.seatreserve.db.UserDao;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReserveActivity extends AppCompatActivity {

    private ListView listView;//列表控件
    private String loginAccount;
    private ReservationDao reservationDao;
    private UserDao userDao;
    private List<Reservation> reserveList;//集合
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                          //时间格化器
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reserve);
        listView = findViewById(R.id.lv_reserve);//xml中的id

        loginAccount = getIntent().getStringExtra("login_account");
        reservationDao = new ReservationDao(this);
        userDao = new UserDao(this);

        // 打开页面自动检测超时违约
        int breakCount = reservationDao.checkBreak(loginAccount);
        if (breakCount > 0) {
            Toast.makeText(this, "检测到" + breakCount + "条预约超时违约！", Toast.LENGTH_SHORT).show();
        }

        loadData();
    }

    private void loadData() {
        reserveList = reservationDao.getMyReservation(loginAccount);
        MyAdapter adapter = new MyAdapter();
        listView.setAdapter(adapter);
    }

    class MyAdapter extends BaseAdapter {//自定义适配器内部类，专门为listview提供数据
        @Override
        public int getCount() {
            return reserveList.size();
        }

        @Override
        public Object getItem(int position) {
            return reserveList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {//布局填充器，把xml布局转化为view对象
                convertView = LayoutInflater.from(MyReserveActivity.this).inflate(R.layout.item_reserve, parent, false);
                holder = new ViewHolder();
                holder.tvInfo = convertView.findViewById(R.id.tv_info);
                holder.btnSign = convertView.findViewById(R.id.btn_sign);
                holder.btnRenew = convertView.findViewById(R.id.btn_renew);
                holder.btnRelease = convertView.findViewById(R.id.btn_release);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }//缓存机制，提高流畅度

            Reservation r = reserveList.get(position);
            String startTime = sdf.format(new Date(r.getStartTime()));
            String endTime = sdf.format(new Date(r.getEndTime()));

            String statusText;
            if (r.getStatus() == 0) statusText = "进行中";
            else if (r.getStatus() == 1) statusText = "已结束";
            else statusText = "提前释放";

            String breakText = r.getIsBreak() == 1 ? "【违约】" : "";
            holder.tvInfo.setText("图书馆:" + r.getLibrary() + " 楼层:" + r.getFloor() + " 座位:" + r.getSeatNum()
                    + "\n开始:" + startTime + " 结束:" + endTime + "\n状态：" + statusText + breakText);

            boolean canOperate = r.getStatus() == 0;
            // 防止重复签到
            if (r.getIsSign() == 1) {
                holder.btnSign.setEnabled(false);
                holder.btnSign.setText("已签到");
            } else {
                holder.btnSign.setEnabled(canOperate);
                holder.btnSign.setText("签到");
            }
            holder.btnRenew.setEnabled(canOperate);
            holder.btnRelease.setEnabled(canOperate);


            holder.btnSign.setOnClickListener(v -> {
                long now = System.currentTimeMillis();
                long start = r.getStartTime();
                long fifteenMin = 15 * 60 * 1000L;
                long signStartWindow = start - fifteenMin;
                long signEndWindow = start + fifteenMin;

                if (now < signStartWindow) {
                    Toast.makeText(MyReserveActivity.this, "还未到签到时段，可在预约开始前15分钟签到！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (now > signEndWindow) {
                    Toast.makeText(MyReserveActivity.this, "已超出签到窗口（开始后15分钟），无法签到！", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean ret = reservationDao.updateSignStatus(r.getId(), 1);
                if (ret) {
                    Toast.makeText(MyReserveActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
                    loadData();
                    // 跳转倒计时页面，带上预约id和账号，给续约使用
                    Intent intent = new Intent(MyReserveActivity.this, CountDownActivity.class);
                    intent.putExtra("start_ms", r.getStartTime());
                    intent.putExtra("end_ms", r.getEndTime());
                    intent.putExtra("reserve_id", r.getId());
                    intent.putExtra("login_account", loginAccount);
                    startActivity(intent);
                }
            });

            //新版续约
            holder.btnRenew.setOnClickListener(v -> {
                long[] addTimeArr = {
                        1 * 60 * 60 * 1000L,
                        2 * 60 * 60 * 1000L,
                        3 * 60 * 60 * 1000L,
                        4 * 60 * 60 * 1000L
                };
                String[] optionArr = {"续约1小时", "续约2小时", "续约3小时", "续约4小时"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MyReserveActivity.this);
                builder.setTitle("请选择续约时长");
                builder.setItems(optionArr, (dialog, which) -> {
                    long addTime = addTimeArr[which];
                    long newEndTime = r.getEndTime() + addTime;
                    String resultMsg = reservationDao.renewReserve(r.getId(), newEndTime);
                    Toast.makeText(MyReserveActivity.this, resultMsg, Toast.LENGTH_SHORT).show();
                    loadData();
                });
                builder.show();
            });

            //提前释放：status=2，清除用户has_using标记
            holder.btnRelease.setOnClickListener(v -> {
                boolean ret = reservationDao.updateStatus(r.getId(), 2);
                if (ret) {
                    userDao.updateHasUsing(loginAccount, 0);
                    Toast.makeText(MyReserveActivity.this, "已提前释放座位", Toast.LENGTH_SHORT).show();
                    loadData();
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvInfo;
        Button btnSign, btnRenew, btnRelease;
    }//静态内部类

    @Override
    protected void onResume() {
        super.onResume();
        //每次回到页面刷新列表
        loadData();
    }
}
