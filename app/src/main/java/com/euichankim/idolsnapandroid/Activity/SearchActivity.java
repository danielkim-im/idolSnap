package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Adapter.SearchAdapter;
import com.euichankim.idolsnapandroid.Model.UserSearchRecord;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    EditText editText;
    ConstraintLayout searchTxtCons;
    ImageButton backBtn;
    RecyclerView recyclerView;
    TextView searchtextview;
    SearchAdapter searchAdapter;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    List<UserSearchRecord> searchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        searchList = new ArrayList<>();
        editText = findViewById(R.id.search_edtx);
        backBtn = findViewById(R.id.search_backBtn);
        searchtextview = findViewById(R.id.searchitem_textview);
        searchTxtCons = findViewById(R.id.searchitem_textCons);
        recyclerView = findViewById(R.id.search_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        searchAdapter = new SearchAdapter(SearchActivity.this, searchList);
        recyclerView.setAdapter(searchAdapter);
        searchTxtCons.setVisibility(View.GONE);

        if (getIntent().getStringExtra("text") != null) {
            editText.setText(getIntent().getStringExtra("text"));
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    searchTxtCons.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    searchTxtCons.setVisibility(View.VISIBLE);
                    searchtextview.setText(s);
                }
            }
        });

        searchTxtCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSearchRecord(editText.getText().toString());
                Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                intent.putExtra("tag", editText.getText().toString());
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    saveSearchRecord(editText.getText().toString());
                    Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                    intent.putExtra("tag", editText.getText().toString());
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                }
                return false;
            }
        });


        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        getSearchRecord();
    }

    private void getSearchRecord() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("search_record")
                .orderBy("searched_at", Query.Direction.DESCENDING).limit(10)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot ds : list) {
                                UserSearchRecord searchRecord = ds.toObject(UserSearchRecord.class);
                                searchList.add(searchRecord);
                            }
                            searchAdapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveSearchRecord(String searched) {
        if (searched.trim().length() != 0) {
            String key = searched.replaceAll("\\s", "").toLowerCase();
            Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
            calendar.add(Calendar.DATE, 90);
            UserSearchRecord userSearchRecord = new UserSearchRecord(searched.replaceAll("\\s+$", ""), key, FieldValue.serverTimestamp(), calendar.getTime());
            firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                    .collection("search_record").document(key).set(userSearchRecord)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // saved search record
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void finish() {
        super.finish();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        overridePendingTransition(0, 0);
    }
}