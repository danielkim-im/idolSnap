package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Adapter.UserStrikeRecordAdapter;
import com.euichankim.idolsnapandroid.Model.UserStrikeRecord;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserStrikeRecordActivity extends AppCompatActivity {

    ImageButton backBtn;
    RecyclerView recyclerView;
    UserStrikeRecordAdapter recordAdapter;
    List<UserStrikeRecord> strikeList;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_strike_record);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        strikeList = new ArrayList<>();
        recyclerView = findViewById(R.id.strikerecord_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserStrikeRecordActivity.this));
        backBtn = findViewById(R.id.strikerecord_backBtn);
        recyclerView.setHasFixedSize(false);
        recordAdapter = new UserStrikeRecordAdapter(UserStrikeRecordActivity.this, strikeList);
        recyclerView.setAdapter(recordAdapter);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getRecords();
    }

    private void getRecords() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("strike_record").whereEqualTo("user_id", mAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : list) {
                            UserStrikeRecord strikeRecord = ds.toObject(UserStrikeRecord.class);
                            strikeList.add(strikeRecord);
                        }
                        recordAdapter.notifyDataSetChanged();
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserStrikeRecordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}