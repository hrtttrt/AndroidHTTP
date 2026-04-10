package com.example.androidhttp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER_IP = "10.0.2.2";
    private static final String SERVER_PORT = "8080";

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private TextView tvUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvUserInfo = findViewById(R.id.tv_user_info);
        Button btnRequest = findViewById(R.id.btn_request);
        btnRequest.setOnClickListener(v -> requestUserInfo("1001"));
    }

    private void requestUserInfo(String userId) {
        new Thread(() -> {
            String url = String.format("http://%s:%s/api/user/info?userId=%s",
                    SERVER_IP, SERVER_PORT, userId);
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        User user = new Gson().fromJson(json, User.class);
                        runOnUiThread(() -> tvUserInfo.setText(user.toString()));
                    } else {
                        runOnUiThread(() -> tvUserInfo.setText(getString(R.string.request_failed)));
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> tvUserInfo.setText(getString(R.string.network_error)));
                }
            });
        }).start();
    }
}
