package com.gsclab.shizukuscreenrecord;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gsclab.shizukuscreenrecord.util.ScreenRecorder;
import com.gsclab.shizukuscreenrecord.util.ShizukuUtil;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BUTTON1 = 1;

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

    public void onClickRecord(View v){
        if (ShizukuUtil.checkPermission(REQUEST_CODE_BUTTON1)) {
            Log.d("checkPermission", "Permission checked");
        }
        else {
            Toast.makeText(this, "Permission Not Checked", Toast.LENGTH_SHORT).show();
            return;
        }

        ScreenRecorder.getInstance().startRecordWithToast(this);
    }

    public void onClickStop(View v){
        if (ShizukuUtil.checkPermission(REQUEST_CODE_BUTTON1)) {
            Log.d("checkPermission", "Permission checked");
        }
        else {
            Toast.makeText(this, "Permission Not Checked", Toast.LENGTH_SHORT).show();
            return;
        }

        ScreenRecorder.getInstance().stopRecordWithToast(this);
    }

//    public void startRecord(View v) {
//        if (!ShizukuUtil.checkPermission(REQUEST_CODE_BUTTON1)) {
//            Toast.makeText(this, "Permission Not Checked", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d("checkPermission", "Permission checked");
//
//        String[] command = {"screenrecord", "--time-limit=30", "/sdcard/" + "test.mp4"};
//
//        Toast.makeText(this, "Record Started", Toast.LENGTH_LONG).show();
//
//        Thread th = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mProcess = Shizuku.newProcess(command, null, mDir);
//                BufferedReader reader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
//                StringBuilder output = new StringBuilder();
//                String line;
//                try {
//                    while ((line = reader.readLine()) != null) {
//                        output.append(line).append("\n");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                Log.d("result", output.toString());
//            }
//        });
//
//        th.start();
//
//    }

//    public void stopRecord(View v) {
//        if (!ShizukuUtil.checkPermission(REQUEST_CODE_BUTTON2)) {
//            Toast.makeText(this, "Permission Not Checked", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d("checkPermission", "Permission checked");
//
//        int num = 3;
//
//        String[] command = {Character.toString((char) num)};
//
//        try {
//            mProcess = Shizuku.newProcess(command, null, mDir);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
//            StringBuilder output = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            Toast.makeText(this, "Record Stopped", Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            Log.e("execution", e.getMessage());
//            Toast.makeText(this, "Command execution failed \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
}
