package com.gsclab.shizukuscreenrecord;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gsclab.shizukuscreenrecord.service.HttpClient;
import com.gsclab.shizukuscreenrecord.service.ScreenRecorder;

public class EmptyActivity extends AppCompatActivity {
    private TextView loadingText;

    private boolean isWebOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_empty);

        loadingText = (TextView) findViewById(R.id.emptyInfoTextView);

        String filename = ScreenRecorder.getFileName().replace(".mp4", "");

        HttpClient.getInstance().pollingStatus(filename, getResources().getString(R.string.url_server) + getResources().getString(R.string.url_status));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadingText.setText(R.string.loading_wait_4_processing);
    }

    @Override
    protected void onPause(){
        super.onPause();
        finishAffinity();
        System.runFinalization();
        System.exit(0);
    }
}