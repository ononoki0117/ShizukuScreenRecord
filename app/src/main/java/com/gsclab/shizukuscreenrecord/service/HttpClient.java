package com.gsclab.shizukuscreenrecord.service;


import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

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

    public Quality quality = Quality.HIGH;

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

        Quality(String label){
            this.label = label;
        }

        public String getLabel(){
            return label;
        }
    }

    public void sendFile2Server(File file, String url) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(), RequestBody.create(MultipartBody.FORM, file))
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

    public void sendFileInfo2Server(String url) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", ScreenRecorder.getFileName());
        jsonObject.put("quality", quality.getLabel());

        RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("File Info Upload Failed", e.toString());
                responseCallback.fileInfoUploadFailed(HttpClient.getInstance());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("File Info Upload Response", response.message());
                responseCallback.fileInfoUploadSucceed(HttpClient.getInstance());
            }
        });
    }

    public interface ResponseCallback {
        void fileUploadSucceed(HttpClient httpClient);
        void fileUploadFailed(HttpClient httpClient);
        void fileInfoUploadSucceed(HttpClient httpClient);
        void fileInfoUploadFailed(HttpClient httpClient);
    }

    private ResponseCallback responseCallback = null;

    public void setResponseCallback(ResponseCallback responseCallback){
        this.responseCallback = responseCallback;
    }
}
