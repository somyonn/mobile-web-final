package com.example.helloandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final List<Bitmap> imageList;

    public ImageAdapter(List<Bitmap> imageList) {// 생성자에서 이미지 목록 입력
        this.imageList = imageList;
    }

    @Override
    @NonNull
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 이미지 항목을 나타낼 뷰 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        // 해당 위치의 이미지를 뷰에 설정
        Bitmap bitmap = imageList.get(position);
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("이미지 저장")
                    .setMessage("이 이미지를 사진첩에 저장하시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        // 이미지 저장 수행
                        saveImageToGallery(bitmap, v.getContext());
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

    }

    public static void saveImageToGallery(Bitmap bitmap, Context context) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                bitmap,
                "Image_" + System.currentTimeMillis(),
                "Image downloaded from app"
        );

        if (savedImageURL != null) {
            Toast.makeText(context, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "사진 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem); // item_image.xml에 있는 ImageView
        }
    }

}