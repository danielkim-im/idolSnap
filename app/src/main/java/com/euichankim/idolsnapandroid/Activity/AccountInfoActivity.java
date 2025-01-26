package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AccountInfoActivity extends AppCompatActivity {

    TextView emailTxt, createDateTxt, accountidTxt;
    ImageButton backBtn;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        backBtn = findViewById(R.id.accountinfo_backBtn);
        emailTxt = findViewById(R.id.accountinfo_emailtxt);
        createDateTxt = findViewById(R.id.accountinfo_createdDatetxt);
        accountidTxt = findViewById(R.id.accountinfo_uidtxt);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("private_info").document("user_info").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                emailTxt.setText(documentSnapshot.getString("email"));
                createDateTxt.setText(timestampToString(((Timestamp) documentSnapshot.get("created_at")).getSeconds() * 1000));
                accountidTxt.setText(documentSnapshot.getString("user_id"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);

        String date;
        date = DateFormat.format("yyyy.MM.dd 'at' HH:mm:ss z", calendar).toString();

        //date = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL);
        return date;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}