package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Fragment.ProfileFragment;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    ImageButton backBtn, checkBtn;
    TextView setDefaultProfileTxt;
    EditText nameEdtx, usernameEdtx, bioEdtx;
    CircleImageView profileImg;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseStorage firebaseStorage;
    Uri resultUri;
    Spannable mspanable;
    int hashTagIsComing = 0;
    String profile_img_id, original_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        backBtn = findViewById(R.id.editprofile_backBtn);
        checkBtn = findViewById(R.id.editprofile_updateBtn);
        nameEdtx = findViewById(R.id.editprofile_nameEdtx);
        usernameEdtx = findViewById(R.id.editprofile_usernameEdtx);
        setDefaultProfileTxt = findViewById(R.id.editprofile_setdefaultprofile);
        bioEdtx = findViewById(R.id.editprofile_bioEdtx);
        profileImg = findViewById(R.id.editprofile_profileImg);
        progressBar = findViewById(R.id.editprofile_progressbar);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBtn.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                backBtn.setEnabled(false);
                profileImg.setEnabled(false);
                setDefaultProfileTxt.setEnabled(false);
                nameEdtx.setEnabled(false);
                usernameEdtx.setEnabled(false);
                bioEdtx.setEnabled(false);
                updateProfileImg();
            }
        });

        setDefaultProfileTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBtn.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                backBtn.setEnabled(false);
                profileImg.setEnabled(false);
                setDefaultProfileTxt.setEnabled(false);
                nameEdtx.setEnabled(false);
                usernameEdtx.setEnabled(false);
                bioEdtx.setEnabled(false);
                //system.currenttimemillis is not necessary since profile_img_id is going to be set as default
                updateUserInfo("default", true);
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // FOR ANDROID13 AND ABOVE
                    // request media only
                    Permissions.check(EditProfileActivity.this, Manifest.permission.READ_MEDIA_IMAGES, null, new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            openImagePicker();
                        }

                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            super.onDenied(context, deniedPermissions);
                            Toast.makeText(EditProfileActivity.this, getString(R.string.permissionforreadmediaimagesrequired), Toast.LENGTH_SHORT).show();
                            finish();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                } else {
                    // request read storage
                    Permissions.check(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, null, new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            openImagePicker();
                        }

                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            super.onDenied(context, deniedPermissions);
                            Toast.makeText(EditProfileActivity.this, getString(R.string.permissionforexternalstoragerequired), Toast.LENGTH_SHORT).show();
                            finish();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                }
            }
        });

        getUserInfo();
    }

    private void updateProfileImg() {
        if (resultUri == null) {
            updateUserInfo(profile_img_id, false);
        } else {
            UUID uuid = UUID.randomUUID();
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("user").child(mAuth.getCurrentUser().getUid());

            final StorageReference imageFilePath = storageReference.child(uuid.toString()).child(uuid.toString());
            imageFilePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            updateUserInfo(uuid.toString(), false);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            checkBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            backBtn.setEnabled(true);
                            profileImg.setEnabled(true);
                            setDefaultProfileTxt.setEnabled(true);
                            nameEdtx.setEnabled(true);
                            usernameEdtx.setEnabled(true);
                            bioEdtx.setEnabled(true);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    backBtn.setEnabled(true);
                    profileImg.setEnabled(true);
                    setDefaultProfileTxt.setEnabled(true);
                    nameEdtx.setEnabled(true);
                    usernameEdtx.setEnabled(true);
                    bioEdtx.setEnabled(true);
                }
            });
        }
    }

    private void updateUserInfo(String uuid, boolean setDefault) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", nameEdtx.getText().toString());
        userMap.put("username", usernameEdtx.getText().toString());
        userMap.put("description", bioEdtx.getText().toString());
        if (setDefault == true) {
            userMap.put("profile_img_id", "default");
        } else {
            userMap.put("profile_img_id", uuid);
        }

        if (usernameEdtx.getText().toString().equals(original_username)) {
            firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                    .update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(EditProfileActivity.this, getString(R.string.editcomplete), Toast.LENGTH_SHORT).show();
                            finish();
                            ProfileFragment.getUserInfo();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            checkBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            backBtn.setEnabled(true);
                            profileImg.setEnabled(true);
                            setDefaultProfileTxt.setEnabled(true);
                            nameEdtx.setEnabled(true);
                            usernameEdtx.setEnabled(true);
                            bioEdtx.setEnabled(true);
                        }
                    });
        } else {
            // check username availability before saving
            firestore.collection("username").document(usernameEdtx.getText().toString())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                if (documentSnapshot.getString("user_id").equals(mAuth.getCurrentUser().getUid())) {
                                    firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                                            .update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(EditProfileActivity.this, getString(R.string.editcomplete), Toast.LENGTH_SHORT).show();
                                                    finish();
                                                    ProfileFragment.getUserInfo();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    checkBtn.setVisibility(View.VISIBLE);
                                                    progressBar.setVisibility(View.GONE);
                                                    backBtn.setEnabled(true);
                                                    profileImg.setEnabled(true);
                                                    setDefaultProfileTxt.setEnabled(true);
                                                    nameEdtx.setEnabled(true);
                                                    usernameEdtx.setEnabled(true);
                                                    bioEdtx.setEnabled(true);
                                                }
                                            });
                                } else {
                                    checkBtn.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    backBtn.setEnabled(true);
                                    profileImg.setEnabled(true);
                                    setDefaultProfileTxt.setEnabled(true);
                                    nameEdtx.setEnabled(true);
                                    usernameEdtx.setEnabled(true);
                                    bioEdtx.setEnabled(true);
                                    usernameEdtx.requestFocus();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                    Toast.makeText(EditProfileActivity.this, getString(R.string.username_taken), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                                        .update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(EditProfileActivity.this, getString(R.string.editcomplete), Toast.LENGTH_SHORT).show();
                                                finish();
                                                ProfileFragment.getUserInfo();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                checkBtn.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                                backBtn.setEnabled(true);
                                                profileImg.setEnabled(true);
                                                setDefaultProfileTxt.setEnabled(true);
                                                nameEdtx.setEnabled(true);
                                                usernameEdtx.setEnabled(true);
                                                bioEdtx.setEnabled(true);
                                            }
                                        });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            checkBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            backBtn.setEnabled(true);
                            profileImg.setEnabled(true);
                            setDefaultProfileTxt.setEnabled(true);
                            nameEdtx.setEnabled(true);
                            usernameEdtx.setEnabled(true);
                            bioEdtx.setEnabled(true);
                        }
                    });
        }
    }

    private void getUserInfo() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.get("profile_img_id").equals("default") || documentSnapshot.getString("profile_img_id") == null || documentSnapshot.getString("profile_img_id").replaceAll("\\s", "").isEmpty()) {
                            Glide.with(EditProfileActivity.this).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                            profile_img_id = "default";
                        } else {
                            profile_img_id = documentSnapshot.getString("profile_img_id");
                            original_username = documentSnapshot.getString("username");
                            String user_id = documentSnapshot.getString("user_id");
                            String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_480x480";
                            Glide.with(EditProfileActivity.this).load(profileImgUrl).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                        }
                        nameEdtx.setText(documentSnapshot.getString("name"));
                        usernameEdtx.setText(documentSnapshot.getString("username"));
                        setTags(bioEdtx, documentSnapshot.getString("description"));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Image"), PICK_IMAGE);
    }

    private void changeTheColor(String s, int start, int end) {
        mspanable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setTags(EditText editText, String description) {
        SpannableString string = new SpannableString(description);

        int start = -1;
        for (int i = 0; i < description.length(); i++) {
            if (description.charAt(i) == '#') {
                start = i;
            } else if (description.charAt(i) == ' ' || description.charAt(i) == '\n' || (i == description.length() - 1 && start != -1)) {
                if (start != -1) {
                    if (i == description.length() - 1) {
                        i++; // case for if hash is last word and there is no
                        // space after word
                    }

                    final String tag = description.substring(start, i);
                    string.setSpan(new ClickableSpan() {

                        @Override
                        public void onClick(View widget) {
                            //Log.d("Hash", String.format("Clicked %s!", tag));
                            //Toast.makeText(EditSnapActivity.this, tag, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            // link color
                            ds.setColor(Color.parseColor("#FB3958"));
                            ds.setUnderlineText(false);
                        }
                    }, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = -1;
                }
            }
        }

        editText.setMovementMethod(LinkMovementMethod.getInstance());
        editText.setText(string);

        mspanable = bioEdtx.getText();
        bioEdtx.addTextChangedListener(new TextWatcher() {
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
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            CropImage.activity(data.getData())
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Glide.with(EditProfileActivity.this)
                        .load(resultUri)
                        .into(profileImg);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}