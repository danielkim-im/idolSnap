package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applovin.sdk.AppLovinPrivacySettings;
import com.euichankim.idolsnapandroid.R;;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout accountStrikeRecordLin, signoutLin, userInfoLin, aboutLin, adLin, hiddenSnapUserLin;
    ImageButton backBtn;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        backBtn = findViewById(R.id.settings_backBtn);
        userInfoLin = findViewById(R.id.settings_accountinfo);
        aboutLin = findViewById(R.id.settings_about);
        accountStrikeRecordLin = findViewById(R.id.settings_userAccountStrike);
        signoutLin = findViewById(R.id.settings_signout);
        adLin = findViewById(R.id.settings_ad);
        hiddenSnapUserLin = findViewById(R.id.settings_hiddensnapuser);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        aboutLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        userInfoLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, AccountInfoActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        accountStrikeRecordLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, UserStrikeRecordActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        hiddenSnapUserLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, SettingsManageHideActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        adLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askApplovinConsent();
            }
        });


        signoutLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("token", "null");
                map.put("updated_at", FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("private_info").document("fcm_token").set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AuthUI.getInstance()
                                .signOut(SettingsActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(SettingsActivity.this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SettingsActivity.this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                        finish();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void askApplovinConsent() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SettingsActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_applovin_consent);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        CardView agree;
        TextView donotagree;
        ProgressBar progressBar;
        agree = bottomSheetDialog.findViewById(R.id.consent_agreeCardview);
        donotagree = bottomSheetDialog.findViewById(R.id.consent_donotagree);
        progressBar = bottomSheetDialog.findViewById(R.id.consent_progressbar);

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree.setVisibility(View.GONE);
                donotagree.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("has_consent", true);
                hashMap.put("last_updated_at", FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("private_info")
                        .document("applovin_consent").set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                AppLovinPrivacySettings.setHasUserConsent(true, SettingsActivity.this);
                                bottomSheetDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AppLovinPrivacySettings.setHasUserConsent(false, SettingsActivity.this);
                                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        });
            }
        });

        donotagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree.setVisibility(View.GONE);
                donotagree.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("has_consent", false);
                hashMap.put("last_updated_at", FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("private_info")
                        .document("applovin_consent").set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                AppLovinPrivacySettings.setHasUserConsent(false, SettingsActivity.this);
                                bottomSheetDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AppLovinPrivacySettings.setHasUserConsent(false, SettingsActivity.this);
                                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        });
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}