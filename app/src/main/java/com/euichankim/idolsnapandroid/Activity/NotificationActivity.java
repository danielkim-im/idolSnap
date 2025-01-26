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

import com.euichankim.idolsnapandroid.Model.Notification;
import com.euichankim.idolsnapandroid.R;
import com.euichankim.idolsnapandroid.ViewHolder.Notification_ViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class NotificationActivity extends AppCompatActivity {

    public static FirestorePagingAdapter<Notification, Notification_ViewHolder> mAdapter;
    public static Context mContext;
    public static Query mQuery;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        NotificationActivity.mContext = NotificationActivity.this;

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mQuery = firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                .collection("notification")
                .orderBy("notified_at", Query.Direction.DESCENDING);
        swipeRefreshLayout = findViewById(R.id.notification_swiperefresh);
        backBtn = findViewById(R.id.notification_backbtn);
        recyclerView = findViewById(R.id.notification_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateNotifications();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadNotifications();
    }

    public static void updateNotifications() {
        final int pageSize = 8;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Notification>()
                .setLifecycleOwner((LifecycleOwner) mContext)
                .setQuery(mQuery, pagingConfig, Notification.class)
                .build();
        mAdapter.updateOptions(options);
    }

    private void loadNotifications() {
        // Init Paging Configuration
        final int pageSize = 8;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Notification>()
                .setLifecycleOwner((LifecycleOwner) NotificationActivity.this)
                .setQuery(mQuery, pagingConfig, Notification.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<Notification, Notification_ViewHolder>(options) {
            @NonNull
            @Override
            public Notification_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_notification, parent, false);
                return new Notification_ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Notification_ViewHolder viewHolder, int i, @NonNull Notification notification) {
                // Bind to ViewHolder
                viewHolder.bind(notification);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull Notification_ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                //shimmerLayout.setVisibility(View.GONE);
                //shimmerLayout.stopShimmer();
            }
        };

        recyclerView.setAdapter(mAdapter);

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
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}