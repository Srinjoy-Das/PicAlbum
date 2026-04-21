package com.srinjoy.picalbum;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.database.Cursor;
import android.provider.MediaStore;
import java.util.Date;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;

public class ViewPicture extends AppCompatActivity {

    ViewPager2 viewPager;
    ArrayList<String> images;
    ImageView backBtn;
    ImageView infoBtn;
    ImageView editBtn;
    ImageView shareBtn;
    ImageView deleteBtn;
    int position;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);

        backBtn = findViewById(R.id.backBtn);
        infoBtn = findViewById(R.id.infoBtn);
        editBtn = findViewById(R.id.editBtn);
        shareBtn = findViewById(R.id.shareBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        viewPager = findViewById(R.id.viewPager);
        images = getIntent().getStringArrayListExtra("images");
        position = getIntent().getIntExtra("position", 0);

        if (images == null || images.isEmpty()) return;

        ImagePagerAdapter adapter = new ImagePagerAdapter(this, images);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(position, false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentImage = images.get(viewPager.getCurrentItem());
                imageInfo(currentImage);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewPager.getCurrentItem();
                String currentImage = images.get(position);

                confirmDelete(currentImage, position);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewPager.getCurrentItem();
                String currentImage = images.get(position);

                shareImage(currentImage);
            }
        });

    }

    private void shareImage(String currentImage) {

        Uri uri = Uri.parse(currentImage);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Share_Image"));

    }

    private void confirmDelete(String currentImage, int position) {

        new AlertDialog.Builder(this)
                .setTitle("Image Delete")
                .setMessage("Are you sure you want to delete it ?")
                .setPositiveButton("Delete", ((dialog, which) -> {
                    deleteImage(currentImage, position);
                }))
                .setNegativeButton("Cancel", null)
                .show();

    }

    private void deleteImage(String imageUri, int position) {

        Uri uri = Uri.parse(imageUri);

        try{
            getContentResolver().delete(uri, null, null);

            images.remove(position);
            viewPager.getAdapter().notifyDataSetChanged();

            if(images.isEmpty()){
                finish();
            }
        }catch (SecurityException e){

            IntentSender intentSender = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                intentSender = MediaStore.createDeleteRequest(
                        getContentResolver(),
                        java.util.Collections.singletonList(uri)
                ).getIntentSender();
            }

            try {
                startIntentSenderForResult(
                        intentSender,
                        100,
                        null,
                        0,
                        0,
                        0
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            images.remove(viewPager.getCurrentItem());
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    private void imageInfo(String imageUri) {

        Uri uri = Uri.parse(imageUri);

        Cursor cursor = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {

            int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
            int dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

            String name = cursor.getString(nameIndex);
            long size = cursor.getLong(sizeIndex);
            long date = cursor.getLong(dateIndex);

            String message =
                    "Name: " + name + "\n\n" +
                            "Size: " + (size / 1024) + " KB\n\n" +
                            "Date: " + new Date(date).toString();

            cursor.close();

            new AlertDialog.Builder(this)
                    .setTitle("Image Info")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();

        }
    }
}