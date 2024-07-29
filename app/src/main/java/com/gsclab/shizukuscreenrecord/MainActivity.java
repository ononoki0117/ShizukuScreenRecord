package com.gsclab.shizukuscreenrecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gsclab.shizukuscreenrecord.service.FileUpload;
import com.gsclab.shizukuscreenrecord.service.ScreenRecorder;
import com.gsclab.shizukuscreenrecord.util.ShizukuUtil;

import java.io.File;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BUTTON1 = 1;
    private static final int REQ_PERMISSION_PUSH = 2000;
    private static final int REQ_PERMISSION_STORAGE = 3000;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
        // Do stuff based on the result and the request code
    }

    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        ActionBar ab = getSupportActionBar();

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_PERMISSION_PUSH);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_STORAGE,
                    REQ_PERMISSION_STORAGE);
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                this.startActivity(intent);
            }
        }

        RadioGroup durationRadioGroup = (RadioGroup) findViewById(R.id.durationRadioGroup);
        durationRadioGroup.setOnCheckedChangeListener(durationClickListener);

        RadioGroup qualityRadioGroup = (RadioGroup) findViewById(R.id.qualityRadioGroup);
        qualityRadioGroup.setOnCheckedChangeListener(qualityClickListner);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private final RadioGroup.OnCheckedChangeListener durationClickListener = (radioGroup, i) -> {
        if (i == R.id.radioDuration30) {
            ScreenRecorder.setDuration(30);
        } else if (i == R.id.radioDuration20) {
            ScreenRecorder.setDuration(20);
        } else if (i == R.id.radioDuration10) {
            ScreenRecorder.setDuration(10);
        }
    };


    private final RadioGroup.OnCheckedChangeListener qualityClickListner = (radioGroup, i) -> {
        if (i == R.id.radioQualityHigh) {
            FileUpload.quality = FileUpload.QUALITY.HIGH;
        } else if (i == R.id.radioQualityMiddle) {
            FileUpload.quality = FileUpload.QUALITY.MIDDLE;
        } else if (i == R.id.radioQualityLow) {
            FileUpload.quality = FileUpload.QUALITY.LOW;
        }
    };

    public void onClickRecord(View v) {
        if (ShizukuUtil.checkPermission(REQUEST_CODE_BUTTON1)) {
            Log.d("checkPermission", "Permission checked");
        } else {
            Toast.makeText(this, "Permission Not Checked", Toast.LENGTH_SHORT).show();
            return;
        }

        ScreenRecorder.getInstance().startRecordWithToast(this);
    }

    public void onClickStop(View v) {
        if (ShizukuUtil.checkPermission(REQUEST_CODE_BUTTON1)) {
            Log.d("checkPermission", "Permission checked");
        } else {
            Toast.makeText(this, "Permission Not Checked", Toast.LENGTH_SHORT).show();
            return;
        }

        ScreenRecorder.getInstance().stopRecordWithToast(this);
    }

    public void onClickSend(View v) {
        if (!ScreenRecorder.getInstance().isFilePresent()) {
            Toast.makeText(this, "Record File does not exist!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQ_PERMISSION_STORAGE
                );
            }
            File recordFile = new File(Environment.getExternalStorageDirectory().getPath() + "/", ScreenRecorder.getInstance().getFileName());
            FileUpload.send2Server(recordFile, "http://192.168.0.52:8080/upload");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickStartRecord(View v) {
        if (ShizukuUtil.checkPermission(REQUEST_CODE_BUTTON1)) {
            Log.d("checkPermission", "Permission checked");
        } else {
            Toast.makeText(this, "Permission Not Checked", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getApplicationContext(), RecordingActivity.class);
        startActivity(intent);
    }
}
