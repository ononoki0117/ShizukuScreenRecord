package com.gsclab.shizukuscreenrecord.service;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gsclab.shizukuscreenrecord.util.IToastNotify;
import com.gsclab.shizukuscreenrecord.util.ShizukuUtil;

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

    public interface statusCallback{
        void finishRecord(ScreenRecorder screenRecorder);
    }

    private statusCallback statusCallback = null;

    public static void setStatusCallback(statusCallback callback){
        ScreenUtilHelper.INSTANCE.statusCallback = callback;
    }

    public static void setDuration(int duration) {
        ScreenUtilHelper.INSTANCE.duration = duration;
    }

    public int duration = 30;

    private boolean filePresent = false;

    public boolean isFilePresent() {
        return filePresent;
    }

    private String fileName = "test.mp4";

    public static String getFileName() {
        return ScreenUtilHelper.INSTANCE.fileName;
    }

    private void setFileName() {

    }

    private final String mDir = "/";

    private Thread recordThread;

    Timer recordTimer = new Timer();

    public void startRecord() {
        String[] command = {"screenrecord", "--time-limit=" + duration, Environment.getExternalStorageDirectory().getPath() + '/' + fileName};

        recordThread = new Thread(() -> {
            Log.d("recordThread", "record Thread loaded");
            filePresent = false;
            ShizukuUtil.mProcess = Shizuku.newProcess(command, null, mDir);

            BufferedReader reader = new BufferedReader(new InputStreamReader(ShizukuUtil.mProcess.getInputStream()));

            try {
                reader.readLine();
                filePresent = true;
            } catch (Exception e) {
                Log.d("Thread Exception", Objects.requireNonNull(e.getMessage()));
            }

            if(statusCallback != null){
                this.statusCallback.finishRecord(this);
            }
            Log.d("result", "Record Ended");
        });

        recordThread.start();
    }

    public void startRecordWithToast(AppCompatActivity activity) {
        Toast.makeText(activity, "Record Started", Toast.LENGTH_LONG).show();
        startRecord();

        IToastNotify notifyEnd = () -> activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Record End!", Toast.LENGTH_LONG).show();
            }
        });

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                notifyEnd.makeToast();
            }
        };
        recordTimer.schedule(task, duration * 1000L);
    }

    public void stopRecordWithToast(AppCompatActivity activity) {
        try {
            assert ShizukuUtil.mProcess != null;

            ShizukuUtil.mProcess.destroy();
            recordTimer.cancel();
            ShizukuUtil.mProcess = null;

            Toast.makeText(activity, "Record Stopped", Toast.LENGTH_LONG).show();
        } catch (AssertionError e) {
            Toast.makeText(activity, "Record not running", Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            Toast.makeText(activity, "RuntimeException!", Toast.LENGTH_LONG).show();
        }
    }
}
