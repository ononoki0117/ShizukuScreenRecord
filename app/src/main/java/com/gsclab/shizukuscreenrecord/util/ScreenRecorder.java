package com.gsclab.shizukuscreenrecord.util;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import rikka.shizuku.Shizuku;

public class ScreenRecorder {
    private ScreenRecorder() {
    }

    private static class ScreenUtilHelper {
        private static final ScreenRecorder INSTANCE = new ScreenRecorder();
    }

    public static ScreenRecorder getInstance() {
        return ScreenUtilHelper.INSTANCE;
    }

    public static void setDuration(int duration) {
        ScreenUtilHelper.INSTANCE.duration = duration;
    }

    public int duration = 30;

    private final String mDir = "/";

    private Thread recordThread;
    private Timer recordTimer;

    public void startRecord() {
        String[] command = {"screenrecord", "--time-limit=30", Environment.getExternalStorageDirectory().getAbsolutePath() + "test.mp4"};

        recordThread = new Thread(() -> {
            ShizukuUtil.mProcess = Shizuku.newProcess(command, null, mDir);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ShizukuUtil.mProcess.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (Exception e) {
                Log.d("Thread Exception", Objects.requireNonNull(e.getMessage()));
            }
            Log.d("result", output.toString());
        });

        recordThread.start();
    }

    public void stopRecord() {
        if (recordThread.isAlive()) {
            recordThread.interrupt();
        }
    }

    public void startRecordWithToast(AppCompatActivity activity) {
        Toast.makeText(activity, "Record Started", Toast.LENGTH_LONG).show();
        startRecord();

        ToastNotify notifyEnd = () -> Toast.makeText(activity, "Record End!", Toast.LENGTH_LONG).show();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                notifyEnd.makeToast();
            }
        };
    }

    public void stopRecordWithToast(AppCompatActivity activity) {
        if (recordThread.isAlive()) {
            recordThread.interrupt();
            Toast.makeText(activity, "Thread Interrupted", Toast.LENGTH_LONG).show();
        }
    }

    private interface ToastNotify{
        void makeToast();
    }
}
