package com.euichankim.idolsnapandroid.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacerSettings;
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.euichankim.idolsnapandroid.Activity.SearchActivity;
import com.euichankim.idolsnapandroid.Adapter.DiscoverInterestedTagAdapter;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.Model.Tag;
import com.euichankim.idolsnapandroid.Model.UserInterestedTag;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.Home_SnapSmallViewHolder;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class DiscoverFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseDatabase firebaseDatabase;
    SwipeRefreshLayout swipeRefreshLayout;
    CardView searchBox;
    DiscoverInterestedTagAdapter discoverInterestedTagAdapter;
    public static RecyclerView recyclerView, tagRV;
    ShimmerFrameLayout shimmerLayout;
    public static Context mContext;
    public static FirestorePagingAdapter<Snap, Home_SnapSmallViewHolder> mAdapter;
    public static Query mQuery;
    List<String> interestedTag;
    MaxRecyclerAdapter maxRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        swipeRefreshLayout = view.findViewById(R.id.search_swiperefresh);
        searchBox = view.findViewById(R.id.search_textboxcardview);
        recyclerView = view.findViewById(R.id.recommend_rv);
        tagRV = view.findViewById(R.id.search_tag_rv);
        shimmerLayout = view.findViewById(R.id.homerecommend_shimmerlayout);
        swipeRefreshLayout = view.findViewById(R.id.search_swiperefresh);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
        tagRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(false);
        tagRV.setHasFixedSize(false);
        shimmerLayout.startShimmer();
        interestedTag = new ArrayList<>();
        discoverInterestedTagAdapter = new DiscoverInterestedTagAdapter(getContext(), interestedTag);
        tagRV.setAdapter(discoverInterestedTagAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateUserRelatedTag();
            }
        });

        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchActivity.class));
                ((Activity) view.getContext()).overridePendingTransition(0, 0);
            }
        });

        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(getContext()).setMediationProvider("max");
        AppLovinSdk.initializeSdk(getContext(), new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                // AppLovin SDK is initialized, start loading ads
                //Toast.makeText(MainActivity.this, "AppLovin SDK Initialized", Toast.LENGTH_SHORT).show();
                getUserRelatedTag();
            }
        });

        return view;
    }

    private void getUserRelatedTag() {
        List tagList = new ArrayList();

        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("interested_tag")
                .orderBy("last_interacted", Query.Direction.DESCENDING).limit(10)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : list) {
                            UserInterestedTag tag = ds.toObject(UserInterestedTag.class);

                            tagList.add(tag.getTag());
                            //interestTagList.add(tag.getTag());
                        }
                        getSnap(tagList);
                        interestedTag.clear();
                        Collections.shuffle(tagList);
                        interestedTag.addAll(tagList);
                        discoverInterestedTagAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserRelatedTag() {
        List tagList = new ArrayList();
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("interested_tag")
                .orderBy("last_interacted", Query.Direction.DESCENDING).limit(10)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : list) {
                            UserInterestedTag tag = ds.toObject(UserInterestedTag.class);

                            tagList.add(tag.getTag());
                            //interestTagList.add(tag.getTag());
                        }
                        updateSnap(getContext(), tagList);
                        interestedTag.clear();
                        Collections.shuffle(tagList);
                        interestedTag.addAll(tagList);
                        discoverInterestedTagAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSnap(Context context, List<String> relatedTag) {
        if (relatedTag==null || relatedTag.isEmpty()) {
            mQuery = firestore.collection("snap")
                    .whereEqualTo("visibility_private", false)
                    .orderBy("popularity", Query.Direction.DESCENDING);
        } else {
            mQuery = firestore.collection("snap")
                    .whereArrayContainsAny("tag", relatedTag)
                    .whereEqualTo("visibility_private", false)
                    .orderBy("popularity", Query.Direction.DESCENDING);
        }
        final int pageSize = 8;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) context)
                .setQuery(mQuery, pagingConfig, Snap.class)
                .build();
        mAdapter.updateOptions(options);
    }

    private void getSnap(List<String> relatedTag) {
        if (relatedTag.isEmpty() || relatedTag == null) {
            mQuery = firestore.collection("snap")
                    .whereEqualTo("visibility_private", false)
                    .orderBy("popularity", Query.Direction.DESCENDING);
        } else {
            mQuery = firestore.collection("snap")
                    .whereArrayContainsAny("tag", relatedTag)
                    .whereEqualTo("visibility_private", false)
                    .orderBy("popularity", Query.Direction.DESCENDING);
        }
        // Init Paging Configuration
        final int pageSize = 8;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) getContext())
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
                shimmerLayout.setVisibility(View.GONE);
                shimmerLayout.stopShimmer();
            }
        };

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.nativead_snap_small)
                .setTitleTextViewId(R.id.title_text_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.options_view)
                .build();

        MaxAdPlacerSettings settings = new MaxAdPlacerSettings("4d7bcf294088b21b");
        //settings.addFixedPosition(1);
        settings.setRepeatingInterval(9);
        maxRecyclerAdapter = new MaxRecyclerAdapter(settings, mAdapter, getActivity());
        maxRecyclerAdapter.getAdPlacer().setNativeAdViewBinder(binder);
        maxRecyclerAdapter.getAdPlacer().setAdSize(-1, -1);

        recyclerView.setAdapter(maxRecyclerAdapter);

        maxRecyclerAdapter.loadAds();

        mAdapter.addOnPagesUpdatedListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (mAdapter.getItemCount() == 0) {
                    swipeRefreshLayout.setRefreshing(false);
                    shimmerLayout.setVisibility(View.GONE);
                    shimmerLayout.stopShimmer();
                    updateSnap(getContext(), null);
                }
                return null;
            }
        });
    }

    private void getRandomSnap() {
        mQuery = firestore.collection("snap")
                .whereEqualTo("visibility_private", false)
                .orderBy("popularity", Query.Direction.DESCENDING);

        // Init Paging Configuration
        final int pageSize = 8;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) getContext())
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
                shimmerLayout.setVisibility(View.GONE);
                shimmerLayout.stopShimmer();
            }
        };

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.nativead_snap_small)
                .setTitleTextViewId(R.id.title_text_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.options_view)
                .build();

        MaxAdPlacerSettings settings = new MaxAdPlacerSettings("4d7bcf294088b21b");
        //settings.addFixedPosition(1);
        settings.setRepeatingInterval(9);
        maxRecyclerAdapter = new MaxRecyclerAdapter(settings, mAdapter, getActivity());
        maxRecyclerAdapter.getAdPlacer().setNativeAdViewBinder(binder);
        maxRecyclerAdapter.getAdPlacer().setAdSize(-1, -1);

        recyclerView.setAdapter(maxRecyclerAdapter);

        maxRecyclerAdapter.loadAds();
    }
}