package com.euichankim.idolsnapandroid.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Adapter.GalleryAdapter;
import com.euichankim.idolsnapandroid.R;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private static final int Read_Permission = 101;
    RecyclerView recyclerView;
    GalleryAdapter galleryAdapter;
    ImageButton closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        closeBtn = findViewById(R.id.gallery_closeBtn);
        recyclerView = findViewById(R.id.gallery_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // FOR ANDROID13 AND ABOVE
            // request media only
            Permissions.check(GalleryActivity.this, Manifest.permission.READ_MEDIA_IMAGES, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    loadGalleryItems();
                }

                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    super.onDenied(context, deniedPermissions);
                    Toast.makeText(GalleryActivity.this, getString(R.string.permissionforreadmediaimagesrequired), Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
        } else {
            // request read storage
            Permissions.check(GalleryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    loadGalleryItems();
                }

                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    super.onDenied(context, deniedPermissions);
                    Toast.makeText(GalleryActivity.this, getString(R.string.permissionforexternalstoragerequired), Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadGalleryItems() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllItems = new ArrayList<>();
        String absolutePathOfItem;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        String orderBy = MediaStore.Video.Media.DATE_ADDED;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        //get folder name
        //column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            absolutePathOfItem = cursor.getString(column_index_data);

            listOfAllItems.add(absolutePathOfItem);
        }

        galleryAdapter = new GalleryAdapter(this, listOfAllItems, new GalleryAdapter.GalleryListener() {
            @Override
            public void onItemClick(String path) {
                File file = new File(path);
                int file_size = Integer.parseInt(String.valueOf(file.length() / 1024 / 1024));

                if (file_size <= 5) {
                    Intent intent = new Intent(GalleryActivity.this, CreateSnapActivity.class);
                    intent.putExtra("imgPath", path);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Toast.makeText(GalleryActivity.this, getString(R.string.imagesize5mb), Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(galleryAdapter);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_change, R.anim.slide_down);
    }
}