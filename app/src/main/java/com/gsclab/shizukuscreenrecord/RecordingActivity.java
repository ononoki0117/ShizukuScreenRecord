package com.gsclab.shizukuscreenrecord;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gsclab.shizukuscreenrecord.service.ScreenRecorder;

public class RecordingActivity extends AppCompatActivity {
    private boolean isRecorded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recording);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isRecorded)
            return;

        isRecorded = true;

        TextView countDownText= (TextView) findViewById(R.id.countDownText);;
        TextView descriptionText= (TextView) findViewById(R.id.recordDescriptionText);

        descriptionText.setText("3초 뒤 녹화 시작");

        CountDownTimer timer = new CountDownTimer(3000, 1000) {
            private int remain = 3;

            @Override
            public void onTick(long l) {
                countDownText.setText(Integer.toString(remain));
                String str = remain + "초 뒤 녹화 시작";
                descriptionText.setText(str);
                remain--;
            }

            @Override
            public void onFinish() {
                countDownText.setText("시작!");
                descriptionText.setText("화면 녹화중...");

                startRecord();
            }
        };

        timer.start();
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    private void startRecord(){
        ScreenRecorder.setStatusCallback(screenRecorder -> changeActivity());
        ScreenRecorder.getInstance().startRecordWithToast(this);
    }

    private void changeActivity(){
        Intent intent = new Intent(getApplicationContext(), LoadingActivity.class);
        finishAffinity();
        startActivity(intent);
    }
}