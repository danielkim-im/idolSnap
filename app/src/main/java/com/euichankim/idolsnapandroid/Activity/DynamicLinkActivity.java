package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class DynamicLinkActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_link);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.isEmailVerified()) {
            //user is logged in check dynamic link
            checkDynamicLink();
        } else if (user != null && user.isEmailVerified() == false) {
            //Toast.makeText(IntroActivity.this, "VERIFY EMAIL", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DynamicLinkActivity.this, EmailVerificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else {
            startActivity(new Intent(DynamicLinkActivity.this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void checkDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {

                            deepLink = pendingDynamicLinkData.getLink();

                            if (deepLink.getQueryParameter("snap_id") != null) {
                                Intent intent = new Intent(DynamicLinkActivity.this, SnapActivity.class);
                                intent.putExtra("snap_id", deepLink.getQueryParameter("snap_id"));
                                startActivity(intent);
                                finish();
                            } else if (deepLink.getQueryParameter("user_id") != null) {
                                Intent intent = new Intent(DynamicLinkActivity.this, UserProfileActivity.class);
                                intent.putExtra("user_id", deepLink.getQueryParameter("user_id"));
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // no dynamic link

                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DynamicLinkActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}