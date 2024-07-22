package com.gsclab.shizukuscreenrecord.service;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileDownloader {
    private FileDownloader() {
    }

    private static class FileDownloaderHelper {
        private static final FileDownloader INSTANCE = new FileDownloader();
    }

    public static FileDownloader getInstance() {
        return FileDownloaderHelper.INSTANCE;
    }

    private final String outputFilePath = Environment.getExternalStorageDirectory().getPath();

    private File outputFile;

    private final OkHttpClient client = new OkHttpClient();

    public void requestFileStatus(String url, String fileName) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("filename", fileName)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("requestStatus ", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("Response", response.body().string());
            }
        });
    }

    public void requestDownload(String url, String fileName) {
        String filePath = outputFilePath + '/' + fileName;

        outputFile = new File(filePath);

        Request request = new Request.Builder()
                .url(url + '/' + fileName)
                .addHeader("filename", fileName)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("requestDownload", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (outputFile.exists()) {
                    if (outputFile.delete()) {
                        Log.d("File", "File Deleted");
                    }
                }

                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    Log.e("Create New File", e.getMessage());
                    return;
                }

                try {
                    InputStream inputStream = response.body().byteStream();
                    OutputStream outputStream = new FileOutputStream(outputFile);

                    byte[] data = new byte[2048];

                    int count;
                    long total = 0;

                    while ((count = inputStream.read(data)) != -1) {
                        Log.d("time", "executed " + count + "times");
                        total += count;
                        outputStream.write(data, 0, count);
                    }

                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("Create New File", e.getMessage());
                }
            }
        });
    }
}
