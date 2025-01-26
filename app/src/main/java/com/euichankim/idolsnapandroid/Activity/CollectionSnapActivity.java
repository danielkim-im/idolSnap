package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Model.CollectionItem;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.Collection_SnapSmallViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CollectionSnapActivity extends AppCompatActivity {

    String collection_id, collection_title, author_id;
    public static RecyclerView recyclerView;
    List<String> snapIdList;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageButton backBtn, moreBtn;
    TextView toolbarTitletxt;

    private FirestorePagingAdapter<CollectionItem, Collection_SnapSmallViewHolder> mAdapter;
    Query mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectionsnap);

        collection_id = getIntent().getExtras().getString("collection_id");
        collection_title = getIntent().getExtras().getString("collection_title");
        author_id = getIntent().getExtras().getString("author_id");

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mQuery = firestore.collection("collection").document(collection_id).collection("collection_item").orderBy("added_at", Query.Direction.DESCENDING);
        recyclerView = findViewById(R.id.collection_rv);
        swipeRefreshLayout = findViewById(R.id.collection_swiperefresh);
        backBtn = findViewById(R.id.collection_backBtn);
        moreBtn = findViewById(R.id.collection_moreBtn);
        toolbarTitletxt = findViewById(R.id.collection_toolbar_title);
        snapIdList = new ArrayList<>();
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(false);
        swipeRefreshLayout.setRefreshing(true);

        toolbarTitletxt.setText(collection_title);

        if (author_id.equals(mAuth.getCurrentUser().getUid())) {
            moreBtn.setVisibility(View.VISIBLE);
        } else {
            moreBtn.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCollectionItem();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CollectionSnapActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottomsheet_collection_more);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                LinearLayout editLin = bottomSheetDialog.findViewById(R.id.collection_more_editLin);

                editLin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        Intent collectionIntent = new Intent(CollectionSnapActivity.this, EditCollectionActivity.class);
                        collectionIntent.putExtra("collection_id", collection_id);
                        startActivity(collectionIntent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });

                bottomSheetDialog.show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getCollectionItem();
    }

    private void getCollectionItem() {
        // Init Paging Configuration
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<CollectionItem>()
                .setLifecycleOwner((LifecycleOwner) CollectionSnapActivity.this)
                .setQuery(mQuery, pagingConfig, CollectionItem.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<CollectionItem, Collection_SnapSmallViewHolder>(options) {
            @NonNull
            @Override
            public Collection_SnapSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_snap_small, parent, false);
                return new Collection_SnapSmallViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Collection_SnapSmallViewHolder viewHolder, int i, @NonNull CollectionItem collectionItem) {
                // Bind to ViewHolder
                viewHolder.bind(collectionItem);
            }
        };

        // Finally Set the Adapter to mRecyclerView
        recyclerView.setAdapter(mAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}