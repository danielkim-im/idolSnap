package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.util.Locale;

public class AuthActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    CardView googleSigninBtn, emailSignupBtn;
    TextView gotoSigninTxt, titleTxt, noticeTxt;
    FirebaseFirestore firestore;
    ProgressBar progressBar;
    Dialog dialog;
    String noticeStr;

    //Google Auth
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        googleSigninBtn = findViewById(R.id.auth_googleSignInBtn);
        emailSignupBtn = findViewById(R.id.auth_emailSignUpBtn);
        gotoSigninTxt = findViewById(R.id.auth_gotosigninTxt);
        noticeTxt = findViewById(R.id.auth_noticeTxt);
        titleTxt = findViewById(R.id.auth_title);
        progressBar = findViewById(R.id.auth_progressbar);
        progressBar.setVisibility(View.GONE);
        dialog = new Dialog(AuthActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.alertdialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        noticeStr = getString(R.string.auth_notice);

        googleSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signinGoogle();
            }
        });

        emailSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AuthActivity.this, SignUpActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        gotoSigninTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AuthActivity.this, SignInActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        GoogleSignInRequest();
        setSpannableString();
    }

    private void setSpannableString() {
        SpannableString ss = new SpannableString(noticeStr);

        ClickableSpan pp = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://privacy.idolsnap.net/"));
                startActivity(i);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.red));
            }
        };

        ClickableSpan tos = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://tos.idolsnap.net/"));
                startActivity(i);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.red));
            }
        };

        if (Locale.getDefault().getLanguage().equals("ko")) {
            ss.setSpan(pp, 32, 41, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(tos, 19, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ss.setSpan(pp, 93, 107, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(tos, 39, 55, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        noticeTxt.setText(ss);
        noticeTxt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void GoogleSignInRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signinGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        dialog.show();
        progressBar.setVisibility(View.VISIBLE);
        googleSigninBtn.setEnabled(false);
        emailSignupBtn.setEnabled(false);
        gotoSigninTxt.setEnabled(false);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    startActivity(new Intent(AuthActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    dialog.dismiss();
                    progressBar.setVisibility(View.GONE);
                    googleSigninBtn.setEnabled(true);
                    emailSignupBtn.setEnabled(true);
                    gotoSigninTxt.setEnabled(true);
                    Toast.makeText(AuthActivity.this, "Error: Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                progressBar.setVisibility(View.GONE);
                googleSigninBtn.setEnabled(true);
                emailSignupBtn.setEnabled(true);
                gotoSigninTxt.setEnabled(true);
                Toast.makeText(AuthActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            //user is already connected so we need to redirect the user to home page
            startActivity(new Intent(AuthActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            overridePendingTransition(0, 0);
        } else if (user != null && firebaseAuth.getCurrentUser().isEmailVerified() == false) {
            //Toast.makeText(IntroActivity.this, "VERIFY EMAIL", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AuthActivity.this, EmailVerificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else {
            //new user
        }
    }
}