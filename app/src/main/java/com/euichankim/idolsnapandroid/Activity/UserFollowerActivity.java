package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.euichankim.idolsnapandroid.Model.UserFollower;
import com.euichankim.idolsnapandroid.R;
import com.euichankim.idolsnapandroid.ViewHolder.User_SmallViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class UserFollowerActivity extends AppCompatActivity {

    ImageButton backBtn;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    String user_id;
    public static FirestorePagingAdapter<UserFollower, User_SmallViewHolder> mAdapter;
    public static Query mQuery;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_follower);
        UserFollowerActivity.mContext = UserFollowerActivity.this;

        user_id = getIntent().getStringExtra("user_id");
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        backBtn = findViewById(R.id.following_backBtn);
        recyclerView = findViewById(R.id.followinng_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserFollowerActivity.this, LinearLayoutManager.VERTICAL, false));
        swipeRefreshLayout = findViewById(R.id.following_swiperefresh);
        mQuery = firestore.collection("user")
                .document(user_id).collection("follower")
                .orderBy("follower_since", Query.Direction.DESCENDING);


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
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<UserFollower>()
                .setLifecycleOwner((LifecycleOwner) UserFollowerActivity.mContext)
                .setQuery(mQuery, pagingConfig, UserFollower.class)
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
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<UserFollower>()
                .setLifecycleOwner((LifecycleOwner) UserFollowerActivity.this)
                .setQuery(mQuery, pagingConfig, UserFollower.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<UserFollower, User_SmallViewHolder>(options) {
            @NonNull
            @Override
            public User_SmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_user_small, parent, false);
                return new User_SmallViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull User_SmallViewHolder viewHolder, int i, @NonNull UserFollower userfollowing) {
                // Bind to ViewHolder
                viewHolder.snaplikefollowuser_bind(userfollowing.getFollower_user_id());
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