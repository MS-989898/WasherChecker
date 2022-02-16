package com.example.washerchecker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();

    private Button btn_send, btn_dif, btn_timer;

    private TextView txtv_send, txtv_dif, txtv_timer;

    private String preTimeStr, difTimeStr;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private CountDownTimer countDownTimer;
    private boolean difTimeFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_send = findViewById(R.id.btn_send);
        btn_dif = findViewById(R.id.btn_dif);
        btn_timer = findViewById(R.id.btn_timer);
        txtv_send = findViewById(R.id.txtv_send);
        txtv_dif = findViewById(R.id.txtv_dif);
        txtv_timer = findViewById(R.id.txtv_timer);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nowTime = getNowTime();
                databaseReference.child("test").setValue(nowTime);
                txtv_send.setText(nowTime);
            }
        });

        btn_dif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("test").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        preTimeStr = snapshot.getValue(String.class);
                        try {
                            difTimeStr = getDifTime(preTimeStr);
                            if (difTimeStr != null) difTimeFlag = true;
                            txtv_dif.setText(difTimeStr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        btn_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (difTimeFlag){
                    try {
                        startTimer(difTimeStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String getNowTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        return dateFormat.format(date);
    }

    private String getDifTime(String preString) throws ParseException {
        Date preDate = dateFormat.parse(preString);
        Date nowDate = dateFormat.parse(getNowTime());

        long preTime = preDate.getTime();
        long nowTime = nowDate.getTime();
        if (preTime > nowTime) nowTime += 24 * 3600000;

        long dif = (nowTime - preTime) / 1000;
        return String.format("%02d", dif / 3600) + ":" + String.format("%02d", (dif % 3600) / 60) + ":" + String.format("%02d", dif % 60);
    }

    private long getTimeFromStr(String timeStr) {
        String[] strings = timeStr.split(":");
        long hour = Long.valueOf(strings[0]) * 1000 * 3600;
        long min = Long.valueOf(strings[1]) * 1000 * 60;
        long sec = Long.valueOf(strings[2]) * 1000;

        return hour + min + sec;
    }

    private void startTimer(String timeStr) throws ParseException {
        long time = getTimeFromStr(timeStr);

        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long l) {
                long tempTime = l;
                updateTimer(tempTime);
            }

            @Override
            public void onFinish() {
                countDownTimer.cancel();
            }
        }.start();
    }

    private void updateTimer(long time){
        int hour = (int) (time / 1000) / 3600;
        int minutes = (int) (time / 1000) % 3600 / 60;
        int seconds = (int) (time / 1000) % 60;

        txtv_timer.setText(String.format("%02d", hour) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
    }

}