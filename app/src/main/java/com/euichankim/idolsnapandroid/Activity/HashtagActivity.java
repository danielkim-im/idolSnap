package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacerSettings;
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.Home_SnapSmallViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Locale;

public class HashtagActivity extends AppCompatActivity {

    String tag;
    TextView tagTxt, numTxt;
    ImageButton backBtn, moreBtn;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    Query mQuery;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirestorePagingAdapter<Snap, Home_SnapSmallViewHolder> mAdapter;
    MaxRecyclerAdapter maxRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag);

        tag = getIntent().getStringExtra("tag").replaceAll("#", "").replaceAll("\\s", "").toLowerCase();
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mQuery = firestore.collection("snap")
                .whereArrayContains("tag", tag)
                .whereEqualTo("visibility_private", false)
                .orderBy("created_at", Query.Direction.DESCENDING);
        backBtn = findViewById(R.id.hashtag_backBtn);
        moreBtn = findViewById(R.id.hashtag_moreBtn);
        tagTxt = findViewById(R.id.hashtag_tagTxt);
        numTxt = findViewById(R.id.hashtag_numTxt);
        swipeRefreshLayout = findViewById(R.id.hashtag_swiperefresh);
        recyclerView = findViewById(R.id.hashtag_rv);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(false);

        tagTxt.setText("#" + tag);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateSnap();
                getSnapCount();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HashtagActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottomsheet_alert_report);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                TextView reportBtn, cancelBtn;
                ProgressBar progressBar;
                reportBtn = bottomSheetDialog.findViewById(R.id.alertreport_reportTxt);
                cancelBtn = bottomSheetDialog.findViewById(R.id.alertreport_cancelTxt);
                progressBar = bottomSheetDialog.findViewById(R.id.alertreport_progressbar);
                progressBar.setVisibility(View.GONE);

                reportBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reportBtn.setVisibility(View.GONE);
                        cancelBtn.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("reporter_id", mAuth.getCurrentUser().getUid());
                        hashMap.put("reported_at", FieldValue.serverTimestamp());
                        firestore.collection("report_tag").document(tag)
                                .collection("reporter")
                                .document(mAuth.getCurrentUser().getUid()).set(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(HashtagActivity.this, getString(R.string.reportsent), Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        firestore.collection("report_tag").document(tag)
                                                .collection("reporter")
                                                .document(mAuth.getCurrentUser().getUid())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot.exists()) {
                                                            Toast.makeText(HashtagActivity.this, getString(R.string.alreadyreported), Toast.LENGTH_SHORT).show();
                                                            bottomSheetDialog.dismiss();
                                                        } else {
                                                            reportBtn.setVisibility(View.VISIBLE);
                                                            cancelBtn.setVisibility(View.VISIBLE);
                                                            progressBar.setVisibility(View.GONE);
                                                            Toast.makeText(HashtagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        reportBtn.setVisibility(View.VISIBLE);
                                                        cancelBtn.setVisibility(View.VISIBLE);
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(HashtagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.show();
            }
        });

        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(HashtagActivity.this).setMediationProvider("max");
        AppLovinSdk.initializeSdk(HashtagActivity.this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                // AppLovin SDK is initialized, start loading ads
                //Toast.makeText(MainActivity.this, "AppLovin SDK Initialized", Toast.LENGTH_SHORT).show();
                getSnap();
            }
        });
        getSnapCount();
    }

    private void updateSnap() {
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) HashtagActivity.this)
                .setQuery(mQuery, pagingConfig, Snap.class)
                .build();
        mAdapter.updateOptions(options);
    }

    private void getSnap() {
        // Init Paging Configuration
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) HashtagActivity.this)
                .setQuery(mQuery, pagingConfig, Snap.class)
                .build();
        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<Snap, Home_SnapSmallViewHolder>(options) {
            @NonNull
            @Override
            public Home_SnapSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_snap_small, parent, false);
                return new Home_SnapSmallViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Home_SnapSmallViewHolder viewHolder, int i, @NonNull Snap snap) {
                // Bind to ViewHolder
                viewHolder.bind(snap);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull Home_SnapSmallViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                /*shimmerLayout.setVisibility(View.GONE);
                shimmerLayout.stopShimmer();*/
            }
        };

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.nativead_snap_small)
                .setTitleTextViewId(R.id.title_text_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.options_view)
                .build();

        MaxAdPlacerSettings settings = new MaxAdPlacerSettings("4162fea8ebe7f1d8");
        //settings.addFixedPosition(1);
        settings.setRepeatingInterval(9);
        maxRecyclerAdapter = new MaxRecyclerAdapter(settings, mAdapter, HashtagActivity.this);
        maxRecyclerAdapter.getAdPlacer().setNativeAdViewBinder(binder);
        maxRecyclerAdapter.getAdPlacer().setAdSize(-1, -1);

        recyclerView.setAdapter(maxRecyclerAdapter);

        maxRecyclerAdapter.loadAds();
    }

    private void getSnapCount() {
        AggregateQuery countQuery = mQuery.count();
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AggregateQuerySnapshot snapshot = task.getResult();
                long snapCount = snapshot.getCount();
                if (Locale.getDefault().getLanguage().equals("ko")) {
                    numTxt.setText("스냅 " + snapCount + "개");
                } else {
                    if (snapCount <= 1) {
                        numTxt.setText(snapCount + " Snap");
                    } else {
                        numTxt.setText(snapCount + " Snaps");
                    }
                }
            } else {
                Toast.makeText(HashtagActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                numTxt.setText(task.getException().getMessage());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HashtagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                if (Locale.getDefault().getLanguage().equals("ko")) {
                    numTxt.setText("스냅 -개");
                } else {
                    numTxt.setText("- Snap");
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}