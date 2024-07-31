package com.gsclab.shizukuscreenrecord;

import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gsclab.shizukuscreenrecord.service.HttpClient;
import com.gsclab.shizukuscreenrecord.service.ScreenRecorder;

import org.json.JSONException;

import java.io.File;

public class LoadingActivity extends AppCompatActivity {
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadingText = (TextView) findViewById(R.id.loadingInfoTextView);
        loadingText.setText(R.string.loading_ready);

        HttpClient.getInstance().setResponseCallback(new HttpClient.ResponseCallback() {
            @Override
            public void fileUploadSucceed(HttpClient httpClient) {
                loadingText.setText(R.string.loading_wait_4_processing);
            }

            @Override
            public void fileUploadFailed(HttpClient httpClient) {
                loadingText.setText(R.string.loading_wait_4_response);
            }

            @Override
            public void fileInfoUploadSucceed(HttpClient httpClient) {
                loadingText.setText(R.string.loading_upload_file);

                File recordFile = new File(Environment.getExternalStorageDirectory().getPath() + "/", ScreenRecorder.getFileName());
                httpClient.sendFile2Server(recordFile, getResources().getString(R.string.url_server)
                        + getResources().getString(R.string.url_api)
                        + getResources().getString(R.string.url_upload));
            }

            @Override
            public void fileInfoUploadFailed(HttpClient httpClient) {
                loadingText.setText(R.string.loading_wait_4_response);

                try{
                    loadingText.setText(R.string.loading_upload_file_info);
                    HttpClient.getInstance().sendFileInfo2Server(getResources().getString(R.string.url_server)
                            + getResources().getString(R.string.url_api)
                            + getResources().getString(R.string.url_info));
                } catch (JSONException e){
                    loadingText.setText(e.getMessage());
                }
            }
        });

        try{
            loadingText.setText(R.string.loading_upload_file_info);
            HttpClient.getInstance().sendFileInfo2Server(getResources().getString(R.string.url_server)
                    + getResources().getString(R.string.url_api)
                    + getResources().getString(R.string.url_info));
        } catch (JSONException e){
            Toast.makeText(this, "JSON ERROR;;;", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}