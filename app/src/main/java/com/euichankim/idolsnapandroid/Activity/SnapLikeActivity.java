package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.euichankim.idolsnapandroid.Model.Comment;
import com.euichankim.idolsnapandroid.Model.Like;
import com.euichankim.idolsnapandroid.R;
import com.euichankim.idolsnapandroid.ViewHolder.SnapComment_ViewHolder;
import com.euichankim.idolsnapandroid.ViewHolder.User_SmallViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class SnapLikeActivity extends AppCompatActivity {

    ImageButton backBtn;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    String snap_id;
    public static FirestorePagingAdapter<Like, User_SmallViewHolder> mAdapter;
    public static Query mQuery;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_like);
        SnapLikeActivity.mContext = SnapLikeActivity.this;

        snap_id = getIntent().getStringExtra("snap_id");
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        backBtn = findViewById(R.id.like_backBtn);
        recyclerView = findViewById(R.id.like_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(SnapLikeActivity.this, LinearLayoutManager.VERTICAL, false));
        swipeRefreshLayout = findViewById(R.id.like_swiperefresh);
        mQuery = firestore.collection("snap")
                .document(snap_id).collection("like")
                .orderBy("liked_at", Query.Direction.DESCENDING);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });

        get();
    }

    public static void update() {
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Like>()
                .setLifecycleOwner((LifecycleOwner) SnapLikeActivity.mContext)
                .setQuery(mQuery, pagingConfig, Like.class)
                .build();
        mAdapter.updateOptions(options);
    }

    public void get() {
        // Init Paging Configuration
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Like>()
                .setLifecycleOwner((LifecycleOwner) SnapLikeActivity.this)
                .setQuery(mQuery, pagingConfig, Like.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<Like, User_SmallViewHolder>(options) {
            @NonNull
            @Override
            public User_SmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_user_small, parent, false);
                return new User_SmallViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull User_SmallViewHolder viewHolder, int i, @NonNull Like like) {
                // Bind to ViewHolder
                viewHolder.snaplikefollowuser_bind(like.getUser_id());
            }

            @Override
            public void onViewAttachedToWindow(@NonNull User_SmallViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                //shimmerLayout.setVisibility(View.GONE);
                //shimmerLayout.stopShimmer();
            }
        };

        mAdapter.addOnPagesUpdatedListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (mAdapter.getItemCount() == 0) {
                    swipeRefreshLayout.setRefreshing(false);
                    //shimmerLayout.setVisibility(View.GONE);
                    //shimmerLayout.stopShimmer();
                }
                return null;
            }
        });

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}