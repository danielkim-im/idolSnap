package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateSnapActivity extends AppCompatActivity {

    ImageButton closeBtn, uploadbtn;
    ImageView imageView;
    TextView addsourceTxt;
    LinearLayout addSourceLin;
    EditText descEdtx;
    ProgressBar progressBar;
    Spannable mspanable;
    int hashTagIsComing = 0;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    StorageReference storageReference;
    String imgPath, sourceStr;
    Switch allowCommentSwitch;
    boolean allowComment = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createsnap);

        imgPath = getIntent().getStringExtra("imgPath");

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        closeBtn = findViewById(R.id.createpost_closeBtn);
        imageView = findViewById(R.id.createpost_imageview);
        addsourceTxt = findViewById(R.id.createpost_addSourceTxt);
        addSourceLin = findViewById(R.id.createpost_addSourceLin);
        allowCommentSwitch = findViewById(R.id.createpost_commentSwitch);
        descEdtx = findViewById(R.id.createpost_descEdtx);
        uploadbtn = findViewById(R.id.createpost_uploadBtn);
        progressBar = findViewById(R.id.createpost_progressbar);
        sourceStr = null;
        addsourceTxt.setVisibility(View.GONE);
        allowCommentSwitch.setChecked(true);

        mspanable = descEdtx.getText();
        descEdtx.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String startChar = null;
                try {
                    startChar = Character.toString(s.charAt(start));
                    //Log.i(getClass().getSimpleName(), "CHARACTER OF NEW WORD: " + startChar);
                } catch (Exception ex) {
                    startChar = "";
                }
                if (startChar.equals("#")) {
                    changeTheColor(s.toString().substring(start), start, start + count);
                    hashTagIsComing++;
                }
                if (startChar.equals(" ")) {
                    hashTagIsComing = 0;
                }
                if (hashTagIsComing != 0) {
                    changeTheColor(s.toString().substring(start), start, start + count);
                    hashTagIsComing++;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addSourceLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CreateSnapActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottomsheet_createpost_source);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                ImageButton closeBtn, finishBtn;
                EditText editText;
                closeBtn = bottomSheetDialog.findViewById(R.id.bcs_closeBtn);
                finishBtn = bottomSheetDialog.findViewById(R.id.bcs_completeBtn);
                editText = bottomSheetDialog.findViewById(R.id.bcs_edtx);
                editText.setText(sourceStr);

                editText.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            if (editText.getText().toString().trim().length() != 0) {
                                sourceStr = editText.getText().toString().replaceAll("\\s+$", "");
                                addsourceTxt.setVisibility(View.VISIBLE);
                                addsourceTxt.setText(sourceStr);
                            } else {
                                addsourceTxt.setVisibility(View.GONE);
                                sourceStr = null;
                            }
                            bottomSheetDialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
                finishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editText.getText().toString().trim().length() != 0) {
                            sourceStr = editText.getText().toString().replaceAll("\\s+$", "");
                            addsourceTxt.setVisibility(View.VISIBLE);
                            addsourceTxt.setText(sourceStr);
                        } else {
                            addsourceTxt.setVisibility(View.GONE);
                            sourceStr = null;
                        }
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.show();
            }
        });

        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                uploadbtn.setVisibility(View.INVISIBLE);
                closeBtn.setEnabled(false);
                descEdtx.setEnabled(false);
                allowCommentSwitch.setEnabled(false);
                List<String> hashtags = new ArrayList<>();
                String[] words = descEdtx.getText().toString().split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].startsWith("#")) {
                        hashtags.add(words[i]);
                    }
                }
                if (hashtags.size() <= 20) {
                    uploadSnap();
                } else {
                    progressBar.setVisibility(View.GONE);
                    uploadbtn.setVisibility(View.VISIBLE);
                    closeBtn.setEnabled(true);
                    descEdtx.setEnabled(true);
                    allowCommentSwitch.setEnabled(true);
                    Toast.makeText(CreateSnapActivity.this, getString(R.string.hashtagsonasnap), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Glide.with(CreateSnapActivity.this)
                .load(imgPath)
                .override(1080, 1080)
                .into(imageView);

        descEdtx.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void uploadSnap() {
        if (imgPath != null && !descEdtx.getText().toString().isEmpty() && descEdtx.getText().toString().contains("#")) {
            UUID uuid = UUID.randomUUID();
            String content_type;
            content_type = "image";
            if (allowCommentSwitch.isChecked()) {
                allowComment = true;
            } else {
                allowComment = false;
            }
            /*if (MimeTypeMap.getFileExtensionFromUrl(imgPath).equals("gif")) {
                content_type = "gif";
                *//*storageReference= FirebaseStorage.getInstance()
                        .getReference().child("snap").child(mAuth.getCurrentUser().getUid()).child("snap_gif").child(uuid.toString());*//*
            } else {
                content_type = "image";
                *//*storageReference= FirebaseStorage.getInstance()
                        .getReference().child("snap").child(mAuth.getCurrentUser().getUid()).child("snap_image").child(uuid.toString());*//*
            }*/
            storageReference = FirebaseStorage.getInstance()
                    .getReference().child("snap").child(mAuth.getCurrentUser().getUid()).child(uuid.toString());
            final StorageReference imageFilePath = storageReference.child(uuid.toString());
            imageFilePath.putFile(Uri.fromFile(new File(imgPath))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    /*Snap snap = new Snap(mAuth.getCurrentUser().getUid(), descEdtx.getText().toString().replaceAll("\\s+$", ""),
                            FieldValue.serverTimestamp(), content_type, uuid.toString(), true, sourceStr);*/
                    HashMap<String, Object> snap = new HashMap<>();
                    snap.put("author_id", mAuth.getCurrentUser().getUid());
                    snap.put("description", descEdtx.getText().toString().replaceAll("\\s+$", ""));
                    snap.put("created_at", FieldValue.serverTimestamp());
                    snap.put("content_type", content_type);
                    snap.put("content_id", uuid.toString());
                    snap.put("visibility_private", true);
                    snap.put("source", sourceStr);
                    snap.put("allow_comment", allowComment);

                    firestore.collection("snap").add(snap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(CreateSnapActivity.this, getString(R.string.snapuploaded), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateSnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    uploadbtn.setVisibility(View.VISIBLE);
                                    descEdtx.setEnabled(true);
                                    closeBtn.setEnabled(true);
                                    allowCommentSwitch.setEnabled(false);
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateSnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    uploadbtn.setVisibility(View.VISIBLE);
                    descEdtx.setEnabled(true);
                    closeBtn.setEnabled(true);
                    allowCommentSwitch.setEnabled(true);
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            uploadbtn.setVisibility(View.VISIBLE);
            closeBtn.setEnabled(true);
            descEdtx.setEnabled(true);
            descEdtx.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(descEdtx, InputMethodManager.SHOW_IMPLICIT);
            Toast.makeText(CreateSnapActivity.this, getString(R.string.hashtagrequired), Toast.LENGTH_SHORT).show();
        }
    }

    private void changeTheColor(String s, int start, int end) {
        mspanable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

/*    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Image"), PICK_IMAGE);
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imgUri = data.getData();
            Glide.with(CreateSnapActivity.this)
                    .load(imgUri)
                    .override(1080, 1080)
                    .into(imageView);
        } else {
            finish();
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Read_Permission) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        //Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
                        openImagePicker();
                    } else {
                        //Toast.makeText(this, "denied rerequesting", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }
            }
        }
    }*/
}