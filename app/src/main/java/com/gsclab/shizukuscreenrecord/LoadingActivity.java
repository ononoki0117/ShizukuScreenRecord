package com.gsclab.shizukuscreenrecord;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gsclab.shizukuscreenrecord.service.HttpClient;
import com.gsclab.shizukuscreenrecord.service.ScreenRecorder;

import org.json.JSONException;

import java.io.File;
import java.util.Timer;

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
                loadingText.setText(R.string.loading_wait_4_start_processing);
                HttpClient.getInstance().remoteExecution(
                        ScreenRecorder.getFileName(),
                        getResources().getString(R.string.url_server) + getResources().getString(R.string.url_execute));
            }

            @Override
            public void fileUploadFailed(HttpClient httpClient) {
                loadingText.setText(R.string.loading_wait_4_response);
                try {
                    File recordFile = new File(Environment.getExternalStorageDirectory().getPath() + "/", ScreenRecorder.getFileName());

                    HttpClient.getInstance().sendFile2Server(recordFile, getResources().getString(R.string.url_server)
                            + getResources().getString(R.string.url_upload));

                } catch (JSONException e) {
                    loadingText.setText(R.string.loading_json_error);
                }
            }

            @Override
            public void executionSucceed(HttpClient httpClient) {
                loadingText.setText(R.string.loading_wait_4_processing);
                Intent intent = new Intent (getApplicationContext(), EmptyActivity.class);
                startActivity(intent);

                //HttpClient.getInstance().pollingStatus(ScreenRecorder.getFileName(), getResources().getString(R.string.url_server) + getResources().getString(R.string.url_status));
            }

            @Override
            public void executionFailed(HttpClient httpClient) {
                loadingText.setText(R.string.loading_wait_4_response);
                HttpClient.getInstance().remoteExecution(ScreenRecorder.getFileName(), getResources().getString(R.string.url_server) + getResources().getString(R.string.url_execute));
            }

            @Override
            public void executionEnded(HttpClient httpClient, String url, Timer timer) {
                timer.cancel();

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);

            }
        });

        try {
            loadingText.setText(R.string.loading_upload_file_info);
            File recordFile = new File(Environment.getExternalStorageDirectory().getPath() + "/", ScreenRecorder.getFileName());

            HttpClient.getInstance().sendFile2Server(recordFile, getResources().getString(R.string.url_server)
                    + getResources().getString(R.string.url_upload));

        } catch (JSONException e) {
            loadingText.setText(R.string.loading_json_error);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}