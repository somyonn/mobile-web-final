package com.example.helloandroid;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrendActivity extends AppCompatActivity {

    private TextView periodTextView;
    private TextView statisticsTextView;
    private Button btnHourly, btnDaily, btnMonthly;
    private BarChart barChart;
    private LineChart lineChart;
    private String currentPeriod = "시간별";
    private String site_url = "https://somyonn.pythonanywhere.com";
    private String token = "e384460136b565eccc0c70db839bdf8a85118b5d";
    private LoadTrendData taskLoadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);

        periodTextView = findViewById(R.id.periodTextView);
        statisticsTextView = findViewById(R.id.statisticsTextView);
        btnHourly = findViewById(R.id.btnHourly);
        btnDaily = findViewById(R.id.btnDaily);
        btnMonthly = findViewById(R.id.btnMonthly);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);

        setupBarChart();
        setupLineChart();

        // 초기 상태: 시간별 선택
        updatePeriod("시간별");
    }

    private List<String> chartLabels = new ArrayList<>();

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < chartLabels.size()) {
                    return chartLabels.get(index);
                }
                return "";
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < chartLabels.size()) {
                    return chartLabels.get(index);
                }
                return "";
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
    }

    public void onClickPeriod(View v) {
        String period = "";
        if (v.getId() == R.id.btnHourly) {
            period = "시간별";
        } else if (v.getId() == R.id.btnDaily) {
            period = "요일별";
        } else if (v.getId() == R.id.btnMonthly) {
            period = "30일간";
        }
        updatePeriod(period);
    }

    private void updatePeriod(String period) {
        currentPeriod = period;
        periodTextView.setText(period + " 추세");

        // 버튼 색상 업데이트
        resetButtonColors();
        switch (period) {
            case "시간별":
                btnHourly.setBackgroundColor(Color.parseColor("#6200EE"));
                btnHourly.setTextColor(Color.WHITE);
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                break;
            case "요일별":
                btnDaily.setBackgroundColor(Color.parseColor("#6200EE"));
                btnDaily.setTextColor(Color.WHITE);
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                break;
            case "30일간":
                btnMonthly.setBackgroundColor(Color.parseColor("#6200EE"));
                btnMonthly.setTextColor(Color.WHITE);
                barChart.setVisibility(View.GONE);
                lineChart.setVisibility(View.VISIBLE);
                break;
        }

        // 데이터 로드
        loadTrendData(period);
    }

    private void resetButtonColors() {
        btnHourly.setBackgroundColor(Color.LTGRAY);
        btnHourly.setTextColor(Color.BLACK);
        btnDaily.setBackgroundColor(Color.LTGRAY);
        btnDaily.setTextColor(Color.BLACK);
        btnMonthly.setBackgroundColor(Color.LTGRAY);
        btnMonthly.setTextColor(Color.BLACK);
    }

    private void loadTrendData(String period) {
        if (taskLoadData != null && taskLoadData.getStatus() == AsyncTask.Status.RUNNING) {
            taskLoadData.cancel(true);
        }
        taskLoadData = new LoadTrendData();
        taskLoadData.execute(period);
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

    private void displayTrendData(String period, JSONArray jsonArray) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        Map<String, Integer> dateCountMap = new HashMap<>();

        try {
            // 날짜별로 데이터 그룹화
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject post = jsonArray.getJSONObject(i);
                String dateStr = post.optString("published_date", post.optString("created_date", ""));
                Date postDate = parseDate(dateStr);
                
                if (postDate != null) {
                    calendar.setTime(postDate);
                    String dateKey = "";
                    boolean includeData = true;
                    
                    switch (period) {
                        case "시간별":
                            // 시간별: 전체 데이터를 시간별로 집계 (0시~23시)
                            dateKey = String.format(Locale.getDefault(), "%02d시", calendar.get(Calendar.HOUR_OF_DAY));
                            break;
                        case "요일별":
                            // 요일별: 전체 데이터를 요일별로 집계 (일~토)
                            String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
                            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                            dateKey = dayNames[dayOfWeek] + "요일";
                            break;
                        case "30일간":
                            // 30일간: 최근 30일간의 일별
                            Calendar thirtyDaysAgo = Calendar.getInstance();
                            thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);
                            thirtyDaysAgo.set(Calendar.HOUR_OF_DAY, 0);
                            thirtyDaysAgo.set(Calendar.MINUTE, 0);
                            thirtyDaysAgo.set(Calendar.SECOND, 0);
                            
                            if (postDate.after(thirtyDaysAgo.getTime()) || postDate.equals(thirtyDaysAgo.getTime())) {
                                // 날짜 형식: MM/dd
                                dateKey = String.format(Locale.getDefault(), "%02d/%02d", 
                                    calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                            } else {
                                includeData = false;
                            }
                            break;
                    }
                    
                    if (includeData && !dateKey.isEmpty()) {
                        dateCountMap.put(dateKey, dateCountMap.getOrDefault(dateKey, 0) + 1);
                    }
                }
            }
            
            // 그래프 데이터 생성
            List<Entry> entries = new ArrayList<>();
            List<BarEntry> barEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            int index = 0;
            
            // 정렬된 키 리스트 생성
            List<String> sortedKeys = new ArrayList<>();
            
            if (period.equals("시간별")) {
                // 시간별: 0시~23시 모두 포함 (24개)
                for (int hour = 0; hour < 24; hour++) {
                    String key = String.format(Locale.getDefault(), "%02d시", hour);
                    sortedKeys.add(key);
                    dateCountMap.putIfAbsent(key, 0); // 데이터가 없으면 0으로 설정
                }
            } else if (period.equals("요일별")) {
                // 요일별: 일~토 모두 포함 (7개)
                String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
                for (String day : dayNames) {
                    String key = day + "요일";
                    sortedKeys.add(key);
                    dateCountMap.putIfAbsent(key, 0); // 데이터가 없으면 0으로 설정
                }
            } else if (period.equals("30일간")) {
                // 30일간: 날짜별로 정렬
                sortedKeys = new ArrayList<>(dateCountMap.keySet());
                sortedKeys.sort((a, b) -> {
                    try {
                        String[] aParts = a.split("/");
                        String[] bParts = b.split("/");
                        int aMonth = Integer.parseInt(aParts[0]);
                        int aDay = Integer.parseInt(aParts[1]);
                        int bMonth = Integer.parseInt(bParts[0]);
                        int bDay = Integer.parseInt(bParts[1]);
                        
                        if (aMonth != bMonth) {
                            return Integer.compare(aMonth, bMonth);
                        }
                        return Integer.compare(aDay, bDay);
                    } catch (Exception e) {
                        return a.compareTo(b);
                    }
                });
            }
            
            for (String key : sortedKeys) {
                int count = dateCountMap.getOrDefault(key, 0);
                entries.add(new Entry(index, count));
                barEntries.add(new BarEntry(index, count));
                labels.add(key);
                index++;
            }
            
            // 레이블 저장
            chartLabels.clear();
            chartLabels.addAll(labels);
            
            // 그래프 업데이트
            if (period.equals("시간별") || period.equals("요일별")) {
                updateBarChart(barEntries, labels);
            } else {
                updateChart(entries, labels);
            }
            
            // 통계 정보 업데이트
            updateStatistics(dateCountMap, period);
            
        } catch (JSONException e) {
            Log.e("TrendActivity", "Error parsing data", e);
        }
    }

    private void updateStatistics(Map<String, Integer> dateCountMap, String period) {
        if (dateCountMap.isEmpty()) {
            statisticsTextView.setText("데이터가 없습니다.");
            return;
        }

        int total = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;
        String maxKey = "";
        String minKey = "";

        for (Map.Entry<String, Integer> entry : dateCountMap.entrySet()) {
            int count = entry.getValue();
            total += count;
            if (count > max) {
                max = count;
                maxKey = entry.getKey();
            }
            if (count < min) {
                min = count;
                minKey = entry.getKey();
            }
        }

        double average = (double) total / dateCountMap.size();

        StringBuilder stats = new StringBuilder();
        stats.append("총 기록: ").append(total).append("개");
        stats.append("  |  평균: ").append(String.format(Locale.getDefault(), "%.1f", average)).append("개");
        stats.append("  |  최대: ").append(max).append("개 (").append(maxKey).append(")");
        if (dateCountMap.size() > 1) {
            stats.append("  |  최소: ").append(min).append("개 (").append(minKey).append(")");
        }

        statisticsTextView.setText(stats.toString());
    }

    private void updateBarChart(List<BarEntry> entries, List<String> labels) {
        if (entries.isEmpty()) {
            barChart.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "흡연 횟수");
        dataSet.setColor(Color.parseColor("#6200EE"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f); // 막대 너비
        barChart.setData(barData);
        
        // X축 레이블 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelCount(labels.size(), false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        
        // Y축 설정
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(6, false);
        
        barChart.invalidate();
    }

    private void updateChart(List<Entry> entries, List<String> labels) {
        if (entries.isEmpty()) {
            lineChart.clear();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "흡연 횟수");
        dataSet.setColor(Color.parseColor("#6200EE"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#6200EE"));
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#6200EE"));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        
        // X축 레이블 설정
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelCount(Math.min(labels.size(), 10), false); // 최대 10개 레이블 표시
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        
        // Y축 설정
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setLabelCount(6, false); // Y축 레이블 개수
        
        lineChart.invalidate();
    }

    private class LoadTrendData extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
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
                Log.e("TrendActivity", "Error loading data", e);
            }
            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            if (jsonArray != null) {
                displayTrendData(currentPeriod, jsonArray);
            }
        }
    }
}
