package com.gsclab.shizukuscreenrecord.service;


import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
    private OkHttpClient client;

    private final MediaType JSON = MediaType.get("application/json");

    public Scope scope = Scope.ROOM;
    public Quality quality = Quality.HIGH;
    public boolean isTransfer = true;

    private HttpClient() {
        client = new OkHttpClient();
    }

    private static class HttpClientHelper {
        private static final HttpClient INSTANCE = new HttpClient();
    }

    public static HttpClient getInstance() {
        return HttpClientHelper.INSTANCE;
    }

    public enum Quality {
        HIGH("high"),
        MIDDLE("middle"),
        LOW("low");

        private final String label;

        Quality(String label) {
            this.label = label;
        }

        @Override
        @NonNull
        public String toString(){
            return label;
        }
    }

    public enum Scope {
        ROOM("room"),
        OBJECT("object");

        private final String label;

        Scope(String label) {
            this.label = label;
        }

        @NonNull
        @Override
        public String toString(){
            return label;
        }
    }

    public void sendFile2Server(File file, String url) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filename", ScreenRecorder.getFileName());
        jsonObject.put("quality", quality);
        jsonObject.put("scope", scope);
        jsonObject.put("styletransfer", isTransfer);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .addFormDataPart("info", jsonObject.toString(), RequestBody.create(jsonObject.toString(), JSON))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("File Upload Failed", e.toString());
                responseCallback.fileUploadFailed(HttpClient.getInstance());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("Response", response.body().string());

                responseCallback.fileUploadSucceed(HttpClient.getInstance());
            }
        });
    }

    public void remoteExecution(String filename, String url){
        Request request = new Request.Builder()
                .url(url + "/" + filename)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Execute failed", e.toString());
                responseCallback.executionFailed(HttpClient.getInstance());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("Execute Response", response.body().string());
                responseCallback.executionSucceed(HttpClient.getInstance());
            }
        });
    }

    public void pollingStatus(String filename, String url){
        Request request = new Request.Builder()
                .url(url + "/" + filename)
                .get()
                .build();

        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("Polling Failed", e.toString());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            String message = jsonObject.getString("message");

                            if(message.equals("done")){
                                Log.i("Polling Ended", jsonObject.toString());
                                String url = jsonObject.getString("url");
                                responseCallback.executionEnded(HttpClient.getInstance(), url, timer);
                            }
                            else {
                                Log.i("Polling Not Ended", jsonObject.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };


        timer.schedule(timerTask, 1000, 10000);
    }

    public interface ResponseCallback {
        void fileUploadSucceed(HttpClient httpClient);
        void fileUploadFailed(HttpClient httpClient);
        void executionSucceed(HttpClient httpClient);
        void executionFailed(HttpClient httpClient);
        void executionEnded(HttpClient httpClient, String url, Timer timer);
    }

    private ResponseCallback responseCallback = null;

    public void setResponseCallback(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }
}
