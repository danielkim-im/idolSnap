package com.euichankim.idolsnapandroid.Activity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.euichankim.idolsnapandroid.Helper.SingleMediaScanner;
import com.euichankim.idolsnapandroid.Model.UserInterestedTag;
import com.euichankim.idolsnapandroid.R;;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SnapFullScreenImgActivity extends AppCompatActivity {

    //    private static final int Write_Permission = 102;
    String imgUri;
    PhotoView imageView;
    ImageButton closeBtn, downloadBtn;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    ArrayList<String> tagList;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_img);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tagList = new ArrayList<>();
        imgUri = getIntent().getExtras().getString("content_url");
        tagList = getIntent().getExtras().getStringArrayList("taglist");
        imageView = findViewById(R.id.fsimg_imageview);
        closeBtn = findViewById(R.id.fsimg_closeBtn);
        downloadBtn = findViewById(R.id.fsimg_downloadBtn);

        Glide.with(SnapFullScreenImgActivity.this)
                .load(imgUri)
                .override(1080, 1080)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(imageView);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    Permissions.check(SnapFullScreenImgActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            downloadImage(imgUri);
                            addInterestedTag();
                        }

                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            super.onDenied(context, deniedPermissions);
                            Toast.makeText(SnapFullScreenImgActivity.this, getString(R.string.permissionforwriteexternalstoragerequired), Toast.LENGTH_SHORT).show();
                            finish();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                } else {
                    downloadImage(imgUri);
                    addInterestedTag();
                }
            }
        });
    }

    private void downloadImage(String path) {
        Glide.with(getApplicationContext()).asBitmap().load(path)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                            File myDir = new File(root + "/IdolSnap");
                            if (!myDir.exists()) {
                                myDir.mkdirs();
                            }

                            String name = System.currentTimeMillis() + ".jpg";
                            myDir = new File(myDir, name);
                            myDir.createNewFile();
                            FileOutputStream out = new FileOutputStream(myDir);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();

                            Toast.makeText(SnapFullScreenImgActivity.this, getString(R.string.imagesaved), Toast.LENGTH_SHORT).show();
                            new SingleMediaScanner(SnapFullScreenImgActivity.this, myDir);
                        } catch (Exception e) {
                            // some action
                            Toast.makeText(SnapFullScreenImgActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void addInterestedTag() {
        if (tagList!=null){
            for (String tag : tagList) {
                UserInterestedTag userInterestedTag = new UserInterestedTag(tag, FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                        .collection("interested_tag").document(tag).set(userInterestedTag).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                counter++;
                                if (counter == tagList.size()) {
                                    // Complete
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("querylog", e.getMessage());
                                Toast.makeText(SnapFullScreenImgActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                counter++;
                                if (counter == tagList.size()) {
                                    // Complete
                                }
                            }
                        });
            }
        }
    }
}