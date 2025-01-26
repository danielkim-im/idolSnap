package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Adapter.InterestedTagAdapter;
import com.euichankim.idolsnapandroid.Interface.IdolListListener;
import com.euichankim.idolsnapandroid.Model.IdolList;
import com.euichankim.idolsnapandroid.Model.UserInterestedTag;
import com.euichankim.idolsnapandroid.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InterestedTagActivity extends AppCompatActivity implements IdolListListener {

    RecyclerView recyclerView;
    CardView completeBtn;
    InterestedTagAdapter mainAdapter;
    List<IdolList> interestedTagList;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    Dialog dialog;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_tag);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.interestedtag_progressbar);
        recyclerView = findViewById(R.id.interestedtag_RV);
        completeBtn = findViewById(R.id.interestedtag_completeBtn);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.HORIZONTAL));
        recyclerView.setHasFixedSize(false);
        interestedTagList = new ArrayList<>();
        // custom loading dialog
        dialog = new Dialog(InterestedTagActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.alertdialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mainAdapter = new InterestedTagAdapter(interestedTagList, this);
        recyclerView.setAdapter(mainAdapter);

        getInterestedTagList();

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> chosenIdol = new ArrayList<>();
                chosenIdol.addAll(mainAdapter.getSelectedArtists());
                if (!chosenIdol.isEmpty()) {
                    saveUserInterestedTag(chosenIdol);
                }
            }
        });
    }

    private void saveUserInterestedTag(List<String> tagList) {
        dialog.show();

        for (String tag : tagList) {
            UserInterestedTag userInterestedTag = new UserInterestedTag(tag, FieldValue.serverTimestamp());
            firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                    .collection("interested_tag").document(tag).set(userInterestedTag).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            counter++;
                            if (counter == tagList.size()) {
                                dialog.dismiss();
                                startActivity(new Intent(InterestedTagActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(InterestedTagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            counter++;
                            if (counter == tagList.size()) {
                                dialog.dismiss();
                                startActivity(new Intent(InterestedTagActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        }
                    });
        }
    }

    private void getInterestedTagList() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("idolList");
        databaseReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Activity activity = InterestedTagActivity.this;
                if (activity.isFinishing())
                    return;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot artistsnap : dataSnapshot.getChildren()) {
                        IdolList idolList = artistsnap.getValue(IdolList.class);
                        interestedTagList.add(idolList);
                    }
                }
                Collections.shuffle(interestedTagList);
                mainAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InterestedTagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onArtistSelected(Boolean isSelected) {
        if (isSelected) {
            //completeBtn.setVisibility(View.VISIBLE);
        } else {
            //completeBtn.setVisibility(View.GONE);
        }
    }
}