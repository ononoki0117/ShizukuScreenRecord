package com.gsclab.shizukuscreenrecord.service;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gsclab.shizukuscreenrecord.util.IToastNotify;
import com.gsclab.shizukuscreenrecord.util.ShizukuUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public interface StatusCallback {
        void finishRecord(ScreenRecorder screenRecorder);
    }

    private StatusCallback statusCallback = null;

    public void setStatusCallback(StatusCallback callback){
        this.statusCallback = callback;
    }

    public static void setDuration(int duration) {
        ScreenUtilHelper.INSTANCE.duration = duration;
    }

    public int duration = 20;

    private boolean filePresent = false;

    public boolean isFilePresent() {
        return filePresent;
    }

    private String fileName;

    public static String getFileName() {
        return ScreenUtilHelper.INSTANCE.fileName;
    }

    private void setFileName() {
        StringBuilder builder = new StringBuilder();

        LocalDateTime localDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm");
        builder.append(localDateTime.format(formatter))
                .append(".mp4");

        fileName = builder.toString();
    }

    private final String mDir = "/";

    // 나중에 녹화 멈추는 방법을 찾으면 쓸려고 필드로 놔 둠
    private Thread recordThread;

    Timer recordTimer = new Timer();

    public void startRecord() {
        setFileName();

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
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(activity.getApplicationContext(), notification);
                r.play();
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
