package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class EmailVerificationActivity extends AppCompatActivity {

    boolean updateAgain = true;
    TextView descriptionTxt;
    ImageButton verifyBtn;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        descriptionTxt = findViewById(R.id.ev_descriptionTxt);
        verifyBtn = findViewById(R.id.ev_verifyBtn);
        progressBar = findViewById(R.id.ev_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        verifyBtn.setVisibility(View.INVISIBLE);

        if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
            FirebaseAuth.getInstance().getCurrentUser()
                    .sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                verifyBtn.setVisibility(View.VISIBLE);
                                if (Locale.getDefault().getLanguage().equals("ko")) {
                                    descriptionTxt.setText("본인인증을 위해 " + FirebaseAuth.getInstance().getCurrentUser().getEmail() + "으로 인증메일을 전송하였습니다. " +
                                            "이메일 인증후 서비스를 정상적으로 사용할 수 있으며, 인증메일을 못 받으셨을 경우 스펨함을 확인해주세요.");
                                } else {
                                    descriptionTxt.setText("Verification email has been sent to " + FirebaseAuth.getInstance().getCurrentUser().getEmail() +
                                            "\nOnce you have verified your email, you will be able to user our service. If you have not received the verification email" +
                                            " please check spam box.");
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                verifyBtn.setVisibility(View.INVISIBLE);
                                if (Locale.getDefault().getLanguage().equals("ko")) {
                                    descriptionTxt.setText("인증메일 전송 실패\n나중에 다시 시도해 주세요");
                                } else {
                                    descriptionTxt.setText("Failed to send verification email\nPlease try again later");
                                }
                            }
                        }
                    });
        } else {
            startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
                FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (updateAgain == true) {
                                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    updateAgain = false;
                                    startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                } else {
                                    Toast.makeText(EmailVerificationActivity.this, getString(R.string.pleaseverifyemail), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    verifyBtn.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (updateAgain == true) {
                        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                            updateAgain = false;
                            progressBar.setVisibility(View.VISIBLE);
                            verifyBtn.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(EmailVerificationActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }
                }
            }
        });
    }
}