package com.euichankim.idolsnapandroid.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacerSettings;
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;
import com.euichankim.idolsnapandroid.Activity.SearchResultActivity;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.Home_SnapSmallViewHolder;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class SearchResultSnapFragment extends Fragment {

    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    ArrayList<Snap> snapList;
    ShimmerFrameLayout shimmerLayout;
    SwipeRefreshLayout swipeRefreshLayout;
    FirestorePagingAdapter<Snap, Home_SnapSmallViewHolder> mAdapter;
    Query mQuery;
    MaxRecyclerAdapter maxRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result_snap, container, false);

        //searchedTag = Arrays.copyOfRange(SearchResultActivity.searchTagList, 0, 10);

        String[] words = SearchResultActivity.searchedTag.split("\\s+");
        ArrayList<String> keyword_list = new ArrayList<>();
        int counter;
        if (words.length > 10) {
            counter = 10;
        } else {
            counter = words.length;
        }
        for (int i = 0; i < counter; i++) {
            // You may want to check for a non-word character before blindly
            // performing a replacement
            // It may also be necessary to adjust the character class
            keyword_list.add(words[i].replaceAll("[^\\w]", ""));
            //words[i] = words[i].replaceAll("[^\\w]", "");
        }

        firestore = FirebaseFirestore.getInstance();
        mQuery = firestore.collection("snap")
                .whereArrayContainsAny("keyword", keyword_list)
                .whereEqualTo("visibility_private", false)
                .orderBy("created_at", Query.Direction.DESCENDING);
        recyclerView = view.findViewById(R.id.srs_rv);
        shimmerLayout = view.findViewById(R.id.srs_shimmerlayout);
        swipeRefreshLayout = view.findViewById(R.id.srs_swiperefresh);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(false);
        snapList = new ArrayList<>();
        shimmerLayout.startShimmer();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateSnap();
            }
        });

        getSnap();

        return view;
    }

    private void updateSnap() {
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) getContext())
                .setQuery(mQuery, pagingConfig, Snap.class)
                .build();
        mAdapter.updateOptions(options);
    }

    private void getSnap() {
        final int pageSize = 4;
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

        MaxAdPlacerSettings settings = new MaxAdPlacerSettings("8b0ccdcb62d5a2a9");
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
                }
                return null;
            }
        });
    }
}