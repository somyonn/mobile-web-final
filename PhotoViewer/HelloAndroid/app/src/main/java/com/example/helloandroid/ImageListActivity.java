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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImageListActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    TextView textView;
    RecyclerView recyclerView;
    CloadImage taskDownload;
    private String site_url = "https://somyonn.pythonanywhere.com";
    private List<DateGroup> dateGroups = new ArrayList<>();
    private DateGroupAdapter dateGroupAdapter;
    
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
    TextView titleTextView;
    //swipe refresh
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        rootLayout = findViewById(R.id.rootLayout);
        switchToggleBg = findViewById(R.id.switchToggleBg);
        titleTextView = findViewById(R.id.titleTextView);

        switchToggleBg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rootLayout.setBackgroundColor(Color.BLACK);
                titleTextView.setBackgroundColor(Color.DKGRAY);
            } else {
                rootLayout.setBackgroundColor(Color.WHITE);
                titleTextView.setBackgroundColor(Color.parseColor("#6200EE")); // Material Purple
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
        dateGroupAdapter = new DateGroupAdapter(dateGroups);
        recyclerView.setAdapter(dateGroupAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            onClickDownload(null);  // 스와이프 리프레시 시 동기화 메서드 호출
        });

        new AlertDialog.Builder(this)
                .setTitle("도움말")
                .setMessage("앱 사용에 도움이 필요하면 여기를 참고하세요.\n- 이미지 클릭 시 저장 가능\n- 동기화는 화면을 아래로 당기기나 버튼 클릭\n- 날짜를 클릭하면 해당 날짜의 이미지를 볼 수 있습니다.")
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

    private class CloadImage extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... urls) {
            try {
                String apiUrl = urls[0];
                String token = "e384460136b565eccc0c70db839bdf8a85118b5d";

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
                    return new JSONArray(strJson);
                }
            } catch (IOException | JSONException e) {
                Log.e("ImageListActivity", "Error loading data", e);
            }
            return new JSONArray();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            stopRefreshing();
            if (jsonArray.length() == 0) {
                textView.setText("불러올 이미지가 없습니다.");
                dateGroups.clear();
                dateGroupAdapter.notifyDataSetChanged();
            } else {
                textView.setText("이미지 로드 성공!");
                processDataByDate(jsonArray);
            }
        }
    }

    private void processDataByDate(JSONArray jsonArray) {
        Map<String, DateGroup> dateGroupMap = new HashMap<>();
        
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject post = jsonArray.getJSONObject(i);
                String dateStr = post.optString("published_date", post.optString("created_date", ""));
                Date postDate = parseDate(dateStr);
                
                if (postDate != null) {
                    String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postDate);
                    String displayDate = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(postDate);
                    
                    DateGroup group = dateGroupMap.get(dateKey);
                    if (group == null) {
                        group = new DateGroup(dateKey, displayDate);
                        dateGroupMap.put(dateKey, group);
                    }
                    group.addPost(post);
                }
            }
            
            // 날짜별로 정렬 (최신순)
            dateGroups.clear();
            dateGroups.addAll(dateGroupMap.values());
            Collections.sort(dateGroups, (a, b) -> b.dateKey.compareTo(a.dateKey));
            
            dateGroupAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e("ImageListActivity", "Error processing data", e);
        }
    }

    // 날짜별 그룹 클래스
    private static class DateGroup {
        String dateKey;
        String displayDate;
        List<PostItem> posts = new ArrayList<>();

        DateGroup(String dateKey, String displayDate) {
            this.dateKey = dateKey;
            this.displayDate = displayDate;
        }

        void addPost(JSONObject post) {
            posts.add(new PostItem(post));
        }

        int getCount() {
            return posts.size();
        }
    }

    // 포스트 아이템 클래스
    private static class PostItem {
        JSONObject post;
        Bitmap bitmap;
        String imageUrl;

        PostItem(JSONObject post) {
            this.post = post;
            this.imageUrl = post.optString("image", "");
        }
    }

    // 날짜별 그룹 Adapter
    private class DateGroupAdapter extends RecyclerView.Adapter<DateGroupAdapter.ViewHolder> {
        private List<DateGroup> groups;
        private Map<Integer, Boolean> expandedStates = new HashMap<>();

        DateGroupAdapter(List<DateGroup> groups) {
            this.groups = groups;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_group, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DateGroup group = groups.get(position);
            holder.dateTextView.setText(group.displayDate);
            holder.countTextView.setText("흡연횟수: " + group.getCount() + "회");
            
            boolean isExpanded = expandedStates.getOrDefault(position, false);
            holder.imagesLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.expandIcon.setRotation(isExpanded ? 180 : 0);
            
            // 이미지 리스트 설정
            ImageAdapter imageAdapter = new ImageAdapter(group.posts, position);
            holder.imagesRecyclerView.setLayoutManager(new GridLayoutManager(ImageListActivity.this, 2));
            holder.imagesRecyclerView.setAdapter(imageAdapter);
            
            holder.itemView.setOnClickListener(v -> {
                boolean newExpanded = !isExpanded;
                expandedStates.put(position, newExpanded);
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateTextView;
            TextView countTextView;
            ImageView expandIcon;
            LinearLayout imagesLayout;
            RecyclerView imagesRecyclerView;

            ViewHolder(View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                countTextView = itemView.findViewById(R.id.countTextView);
                expandIcon = itemView.findViewById(R.id.expandIcon);
                imagesLayout = itemView.findViewById(R.id.imagesLayout);
                imagesRecyclerView = itemView.findViewById(R.id.imagesRecyclerView);
            }
        }
    }

    // 이미지 Adapter (날짜별 그룹 내부의 이미지들)
    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<PostItem> postItems;
        private int groupPosition;

        public ImageAdapter(List<PostItem> postItems, int groupPosition) {
            this.postItems = postItems;
            this.groupPosition = groupPosition;
            // 이미지 로드 시작
            loadImages();
        }

        private void loadImages() {
            for (int i = 0; i < postItems.size(); i++) {
                final int position = i;
                PostItem item = postItems.get(position);
                if (!item.imageUrl.isEmpty() && item.bitmap == null) {
                    new Thread(() -> {
                        try {
                            String url = item.imageUrl.replace("127.0.0.1", "10.0.2.2");
                            URL imageUrl = new URL(url);
                            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(imgStream);
                            item.bitmap = bitmap;
                            imgStream.close();
                            conn.disconnect();
                            
                            runOnUiThread(() -> notifyItemChanged(position));
                        } catch (Exception e) {
                            Log.e("ImageAdapter", "Error loading image", e);
                        }
                    }).start();
                }
            }
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            PostItem item = postItems.get(position);
            if (item.bitmap != null) {
                holder.imageView.setImageBitmap(item.bitmap);
            } else {
                holder.imageView.setImageBitmap(null);
            }
            
            holder.imageView.setOnClickListener(v -> {
                if (item.bitmap != null) {
                    new AlertDialog.Builder(ImageListActivity.this)
                            .setTitle("이미지 저장")
                            .setMessage("이 이미지를 사진첩에 저장하시겠습니까?")
                            .setPositiveButton("확인", (dialog, which) -> {
                                saveImageToGallery(item.bitmap, ImageListActivity.this);
                            })
                            .setNegativeButton("취소", null)
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return postItems.size();
        }

        public void saveImageToGallery(Bitmap bitmap, android.content.Context context) {
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    context.getContentResolver(),
                    bitmap,
                    "Image_" + System.currentTimeMillis(),
                    "Image downloaded from app"
            );

            if (savedImageURL != null) {
                android.widget.Toast.makeText(context, "사진이 저장되었습니다.", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(context, "사진 저장에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show();
            }
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            
            public ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageViewItem);
            }
        }
    }
}
