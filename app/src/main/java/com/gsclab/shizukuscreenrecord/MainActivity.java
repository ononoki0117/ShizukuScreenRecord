package com.gsclab.shizukuscreenrecord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gsclab.shizukuscreenrecord.service.ScreenRecorder;
import com.gsclab.shizukuscreenrecord.util.ShizukuUtil;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BUTTON1 = 1;
    private static final int REQ_PERMISSION_PUSH = 2000;

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

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_PERMISSION_PUSH);
        }

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
}
