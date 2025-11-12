package com.example.helloandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    TextView textView;
    JSONObject post_json;
    String imageUrl = null;
    RecyclerView recyclerView;
    CloadImage taskDownload;
//    private String site_url = "http://10.0.2.2:8000";
    private String site_url = "https://somyonn.pythonanywhere.com";
    public String getRealPathFromURI(Uri contentUri) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        return result;
    }

    //dark mode
    LinearLayout rootLayout;
    Switch switchToggleBg;
    //swipe refresh
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);
        switchToggleBg = findViewById(R.id.switchToggleBg);

        switchToggleBg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rootLayout.setBackgroundColor(Color.BLACK);
            } else {
                rootLayout.setBackgroundColor(Color.WHITE);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ImageAdapter(new ArrayList<>()));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            onClickDownload(null);  // 스와이프 리프레시 시 동기화 메서드 호출
        });

        new AlertDialog.Builder(this)
                .setTitle("도움말")
                .setMessage("앱 사용에 도움이 필요하면 여기를 참고하세요.\n- 이미지 클릭 시 저장 가능\n- 동기화는 화면을 아래로 당기기나 버튼 클릭\n...")
                .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();

        onClickDownload(null);
    }

    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        textView.setText("다운로드 중...");
        swipeRefreshLayout.setRefreshing(true);
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void onClickUpload(View v) {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }
//  기존 localhost때 적용
//    private class CloadImage extends AsyncTask<String, Void, List<Bitmap>> {
//        @Override
//        protected List<Bitmap> doInBackground(String... urls) {
//            List<Bitmap> bitmapList = new ArrayList<>();
//
//            try {
//                String apiUrl = urls[0];
//                // localhost token
////              String token = "40e61cc85d828c131094bbdf9f21a52f8b3066a6";
//                // pythonanywhere token
//                String token = "e384460136b565eccc0c70db839bdf8a85118b5d";
//                URL urlAPI = new URL(apiUrl);
//                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
//                conn.setRequestProperty("Authorization", "Token " + token);
//                conn.setRequestMethod("GET");
//                conn.setConnectTimeout(3000);
//                conn.setReadTimeout(3000);
//
//                int responseCode = conn.getResponseCode();
//
//                try {
//                    InputStream errorStream = conn.getErrorStream();
//                    if (errorStream != null) {
//                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
//                        StringBuilder errorResult = new StringBuilder();
//                        String line;
//                        while ((line = errorReader.readLine()) != null) {
//                            errorResult.append(line);
//                        }
//                        errorReader.close();
//                        Log.e("API Error Response", errorResult.toString());
//                    }
//                } catch (IOException ioe) {
//                    ioe.printStackTrace();
//                }
//
//                Log.d("API Response Code", String.valueOf(responseCode));
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    InputStream is = conn.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//                    StringBuilder result = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        result.append(line);
//                    }
//                    is.close();
//                    String strJson = result.toString();
//
//                    JSONObject jsonObj = new JSONObject(strJson);
//                    JSONArray aryJson = jsonObj.getJSONArray("results");
//                    for (int i = 0; i < aryJson.length(); i++){
//                        post_json = (JSONObject) aryJson.get(i);
//                        imageUrl = post_json.getString("image");
//                        if (!imageUrl.equals("")) {
//                            imageUrl = imageUrl.replace("127.0.0.1", "10.0.2.2");
//                            URL myImageUrl = new URL(imageUrl);
//                            HttpURLConnection imgConn = (HttpURLConnection) myImageUrl.openConnection();
//                            InputStream imgStream = imgConn.getInputStream();
//                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
//                            bitmapList.add(imageBitmap);
//                            imgStream.close();
//                            imgConn.disconnect();
//                        }
//                    }
//
//                }
//            } catch (IOException | JSONException e) {
//                e.printStackTrace();
//            }
//            return bitmapList;
//        }
//
//        @Override
//        protected void onPostExecute(List<Bitmap> images) {
//            if (images.isEmpty()) {
//                textView.setText("불러올 이미지가 없습니다.");
//                recyclerView.setAdapter(null);
//            } else {
//                textView.setText("이미지 로드 성공!");
//                ImageAdapter adapter = new ImageAdapter(images);
//                recyclerView.setAdapter(adapter);
//            }
//        }
//    }
    private class CloadImage extends AsyncTask<String, Void, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmapList = new ArrayList<>();

            try {
                String apiUrl = urls[0];
                // localhost token
                // String token = "40e61cc85d828c131094bbdf9f21a52f8a6";
                // pythonanywhere token
                String token = "e384460136b565eccc0c70db839bdf8a85118b5d";

                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();

                try {
                    InputStream errorStream = conn.getErrorStream();
                    if (errorStream != null) {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder errorResult = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorResult.append(line);
                        }
                        errorReader.close();
                        Log.e("API Error Response", errorResult.toString());
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                Log.d("API Response Code", String.valueOf(responseCode));
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();

                    // 루트가 배열일 경우 JSONArray로 먼저 파싱
                    JSONArray aryJson = new JSONArray(strJson);
                    for (int i = 0; i < aryJson.length(); i++) {
                        JSONObject post_json = aryJson.getJSONObject(i);

                        // author 필드가 필요한 경우 서버에서 넘겨준 author 값을 로컬에 저장할 수도
                        int author = post_json.optInt("author");

                        String imageUrl = post_json.getString("image");
                        if (!imageUrl.equals("")) {
                            imageUrl = imageUrl.replace("127.0.0.1", "10.0.2.2");
                            URL myImageUrl = new URL(imageUrl);
                            HttpURLConnection imgConn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = imgConn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
                            bitmapList.add(imageBitmap);
                            imgStream.close();
                            imgConn.disconnect();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return bitmapList;
        }

        @Override
        protected void onPostExecute(List<Bitmap> images) {
            stopRefreshing();
            if (images.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
                recyclerView.setAdapter(null);
            } else {
                textView.setText("이미지 로드 성공!");
                ImageAdapter adapter = new ImageAdapter(images);
                recyclerView.setAdapter(adapter);
            }
        }
    }

}
