package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    EditText emailEdtx, passwordEdtx;
    ImageButton signupBtn;
    TextView gotoSignin;
    ProgressBar progressBar;
    ImageButton backBtn;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        emailEdtx = findViewById(R.id.signup_emailEdtx);
        passwordEdtx = findViewById(R.id.signup_passwordEdtx);
        signupBtn = findViewById(R.id.signup_signupBtn);
        gotoSignin = findViewById(R.id.signup_gotosigninTxt);
        progressBar = findViewById(R.id.signup_progressbar);
        backBtn = findViewById(R.id.signup_backBtn);

        gotoSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void signUp(){
        final String email = emailEdtx.getText().toString();
        final String password = passwordEdtx.getText().toString();

        if (!email.isEmpty()&&!password.isEmpty()){
            signupBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            emailEdtx.setEnabled(false);
            passwordEdtx.setEnabled(false);
            gotoSignin.setEnabled(false);

            createUserAccount(email, password);
        }else{
            signupBtn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            emailEdtx.setEnabled(true);
            passwordEdtx.setEnabled(true);
            gotoSignin.setEnabled(true);
        }
    }

    private void createUserAccount(String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            signupBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            emailEdtx.setEnabled(true);
                            passwordEdtx.setEnabled(true);
                            gotoSignin.setEnabled(true);
                            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }else {
                                startActivity(new Intent(SignUpActivity.this, EmailVerificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        }
                        else{
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            signupBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            emailEdtx.setEnabled(true);
                            passwordEdtx.setEnabled(true);
                            gotoSignin.setEnabled(true);
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
    public void finish(){
        super.finish();

    }
}