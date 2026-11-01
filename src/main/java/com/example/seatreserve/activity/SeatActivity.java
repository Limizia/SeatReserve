package com.example.seatreserve.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.seatreserve.R;
import java.util.ArrayList;
import java.util.List;

public class SeatActivity extends AppCompatActivity {

    private GridView gvSeat;
    private String loginAccount;
    private int libId, floor;
    private List<Integer> seatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        loginAccount = getIntent().getStringExtra("login_account");
        libId = getIntent().getIntExtra("lib_id", 1);
        floor = getIntent().getIntExtra("floor", 1);

        gvSeat = findViewById(R.id.gv_seat);
        //初始化 1~24号座位
        for (int i = 1; i <= 24; i++) {
            seatList.add(i);
        }

        SeatAdapter adapter = new SeatAdapter();
        gvSeat.setAdapter(adapter);

        gvSeat.setOnItemClickListener((parent, view, position, id) -> {
            int seatNum = seatList.get(position);
            //跳转到预约时间页面，把所有参数带过去
            Intent intent = new Intent(SeatActivity.this, ReserveTimeActivity.class);
            intent.putExtra("login_account", loginAccount);
            intent.putExtra("lib_id", libId);
            intent.putExtra("floor", floor);
            intent.putExtra("seat_num", seatNum);
            startActivity(intent);
        });
    }

    class SeatAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return seatList.size();
        }
        @Override
        public Object getItem(int position) {
            return seatList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(SeatActivity.this,R.layout.item_seat,null);
                holder = new ViewHolder();
                holder.tvSeat = convertView.findViewById(R.id.tv_seat_num);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            int num = seatList.get(position);
            holder.tvSeat.setText(num+"");
            return convertView;
        }
        class ViewHolder{
            TextView tvSeat;
        }
    }
}
