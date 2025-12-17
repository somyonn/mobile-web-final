package com.example.helloandroid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView dayCountTextView;
    private String site_url = "https://somyonn.pythonanywhere.com";
    private String token = "e384460136b565eccc0c70db839bdf8a85118b5d";
    private LoadDayCount taskLoadDayCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dayCountTextView = findViewById(R.id.dayCountTextView);
        
        // 금연 일차 로드
        loadDayCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 일차 업데이트
        loadDayCount();
    }

    private void loadDayCount() {
        if (taskLoadDayCount != null && taskLoadDayCount.getStatus() == AsyncTask.Status.RUNNING) {
            taskLoadDayCount.cancel(true);
        }
        taskLoadDayCount = new LoadDayCount();
        taskLoadDayCount.execute();
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        };
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                return sdf.parse(dateStr);
            } catch (Exception e) {
                // 다음 형식 시도
            }
        }
        
        if (dateStr.length() >= 10) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return sdf.parse(dateStr.substring(0, 10));
            } catch (Exception e) {
                // 실패
            }
        }
        
        return null;
    }

    private void updateDayCount(JSONArray jsonArray) {
        try {
            Date earliestDate = null;
            Calendar calendar = Calendar.getInstance();
            
            // 모든 포스트를 확인하여 가장 오래된 날짜 찾기
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject post = jsonArray.getJSONObject(i);
                String dateStr = post.optString("published_date", post.optString("created_date", ""));
                Date postDate = parseDate(dateStr);
                
                if (postDate != null) {
                    if (earliestDate == null || postDate.before(earliestDate)) {
                        earliestDate = postDate;
                    }
                }
            }
            
            int dayCount = 0;
            if (earliestDate != null) {
                // 가장 오래된 날짜부터 오늘까지의 일수 계산
                calendar.setTime(earliestDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startTime = calendar.getTimeInMillis();
                
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                long endTime = today.getTimeInMillis();
                
                // 일수 차이 계산 (밀리초를 일로 변환)
                dayCount = (int) ((endTime - startTime) / (1000 * 60 * 60 * 24));
            }
            
            // TextView 업데이트
            dayCountTextView.setText("금연 " + dayCount + "일차! 파이팅 💪");
            
        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing data", e);
            dayCountTextView.setText("금연 0일차! 파이팅 💪");
        }
    }

    public void onClickRecord(View v) {
        // 금연기록 화면으로 이동
        Intent intent = new Intent(this, ImageListActivity.class);
        startActivity(intent);
    }

    public void onClickTrend(View v) {
        // 금연추세 화면으로 이동
        Intent intent = new Intent(this, TrendActivity.class);
        startActivity(intent);
    }

    private class LoadDayCount extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonArray = new JSONArray();

            try {
                String apiUrl = site_url + "/api_root/Post/";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
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
                    jsonArray = new JSONArray(strJson);
                }
            } catch (IOException | JSONException e) {
                Log.e("MainActivity", "Error loading data", e);
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            if (jsonArray != null) {
                updateDayCount(jsonArray);
            } else {
                dayCountTextView.setText("금연 0일차! 파이팅 💪");
            }
        }
    }
}
