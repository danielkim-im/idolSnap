package com.euichankim.idolsnapandroid.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.CombinedLoadStates;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.Profile_SnapSmallViewHolder;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class ProfileSnapFragment extends Fragment {

    public static RecyclerView recyclerView;
    TextView noSnapTxt;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    public static Context mContext;
    ShimmerFrameLayout shimmerLayout;
    public static SwipeRefreshLayout swipeRefreshLayout;
    public static FirestorePagingAdapter<Snap, Profile_SnapSmallViewHolder> mAdapter;
    public static Query mQuery;
    String user_id, currentContext;

    public ProfileSnapFragment(String user_id, String currentContext) {
        this.user_id = user_id;
        this.currentContext = currentContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_snap, container, false);
        ProfileSnapFragment.mContext = getActivity();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.profile_snap_rv);
        shimmerLayout = view.findViewById(R.id.profile_shimmerlayout);
        noSnapTxt = view.findViewById(R.id.profile_snap_nosnapTxt);
        swipeRefreshLayout = view.findViewById(R.id.profilesnap_swiperefresh);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(false);
        mQuery = firestore.collection("snap")
                .whereEqualTo("author_id", user_id)
                .whereEqualTo("visibility_private", false)
                .orderBy("created_at", Query.Direction.DESCENDING);
        noSnapTxt.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateSnap(getContext());
                if (currentContext == "ProfileFragment") {
                    ProfileFragment.getUserInfo();
                    ProfileFragment.getUserCount();
                } else if (currentContext == "UserProfileActivity") {
                    UserProfileActivity.getProfileInfo();
                    UserProfileActivity.getProfileCount();
                }
            }
        });

        getSnap_setAdapter();


        return view;
    }

    public static void updateSnap(Context context) {
        final int pageSize = 4;
        final int prefetchDistance = 8;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) context)
                .setQuery(mQuery, pagingConfig, Snap.class)
                .build();
        mAdapter.updateOptions(options);
    }

    private void getSnap_setAdapter() {
        // Init Paging Configuration
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
        mAdapter = new FirestorePagingAdapter<Snap, Profile_SnapSmallViewHolder>(options) {
            @NonNull
            @Override
            public Profile_SnapSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_snap_small, parent, false);
                return new Profile_SnapSmallViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Profile_SnapSmallViewHolder viewHolder, int i, @NonNull Snap snap) {
                // Bind to ViewHolder
                viewHolder.bind(snap);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull Profile_SnapSmallViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                shimmerLayout.setVisibility(View.GONE);
                shimmerLayout.stopShimmer();
            }
        };

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

        // Finally Set the Adapter to mRecyclerView
        recyclerView.setAdapter(mAdapter);
    }
}