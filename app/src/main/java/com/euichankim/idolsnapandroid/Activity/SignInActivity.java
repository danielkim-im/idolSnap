package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    EditText emailEdtx, passwordEdtx;
    ImageButton signinBtn;
    ImageButton backBtn;
    TextView gotoSignup, forgotPswdTxt;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        emailEdtx = findViewById(R.id.signin_emailEdtx);
        passwordEdtx = findViewById(R.id.signin_passwordEdtx);
        signinBtn = findViewById(R.id.signin_signinBtn);
        gotoSignup = findViewById(R.id.signin_gotoSignup);
        progressBar = findViewById(R.id.signin_progressbar);
        backBtn = findViewById(R.id.signin_backBtn);
        forgotPswdTxt = findViewById(R.id.signin_forgotPswdTxt);

        gotoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailEdtx.getText().toString();
                final String password = passwordEdtx.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    signIn(email, password);
                }
            }
        });

        forgotPswdTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotpswdBottomsheet();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void showForgotpswdBottomsheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SignInActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_signin_forgotpswd);
        //bottomSheetDialog.setCanceledOnTouchOutside(true);

        ImageButton closeBtn = bottomSheetDialog.findViewById(R.id.signinbs_closeBtn);
        EditText emailEdtx = bottomSheetDialog.findViewById(R.id.signinbs_emailEdtx);
        CardView resetPswdCv = bottomSheetDialog.findViewById(R.id.signinbs_resetPswd);
        TextView resetPswdTxt = bottomSheetDialog.findViewById(R.id.signinbs_resetpswdTxt);
        ProgressBar progressBar = bottomSheetDialog.findViewById(R.id.signinbs_progressbar);
        progressBar.setVisibility(View.GONE);
        resetPswdTxt.setVisibility(View.VISIBLE);

        bottomSheetDialog.show();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        resetPswdCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!emailEdtx.getText().toString().isEmpty()) {
                    resetPswdTxt.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseAuth.sendPasswordResetEmail(emailEdtx.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        bottomSheetDialog.dismiss();
                                        Toast.makeText(SignInActivity.this, getString(R.string.emailsent), Toast.LENGTH_SHORT).show();
                                    } else {
                                        resetPswdTxt.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(SignInActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    resetPswdTxt.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void signIn(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        signinBtn.setVisibility(View.INVISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    signinBtn.setVisibility(View.VISIBLE);
                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                        startActivity(new Intent(SignInActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        startActivity(new Intent(SignInActivity.this, EmailVerificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                } else {
                    Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    signinBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void finish() {
        super.finish();

    }
}