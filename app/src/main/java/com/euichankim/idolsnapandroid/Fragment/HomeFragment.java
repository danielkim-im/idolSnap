package com.euichankim.idolsnapandroid.Fragment;

import static com.google.android.material.tabs.TabLayout.*;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacerSettings;
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.euichankim.idolsnapandroid.Activity.MainActivity;
import com.euichankim.idolsnapandroid.Adapter.UserLargeAdapter;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.Model.UserFollowing;
import com.euichankim.idolsnapandroid.Model.UserInterestedTag;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.Home_SnapLargeViewHolder;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    LottieAnimationView lottieAnimationView;
    public static RecyclerView recyclerView;
    public static SwipeRefreshLayout swipeRefreshLayout;
    ShimmerFrameLayout shimmerLayout;
    public static Context mContext;
    FirebaseAuth mAuth;
    ImageButton filterBtn;
    TextView toolbarTitle;
    public static FirebaseFirestore firestore;
    LinearLayoutManager linearLayoutManager;
    public static Query mQuery;
    public static FirestorePagingAdapter<Snap, Home_SnapLargeViewHolder> mAdapter;
    RecyclerView userRV;
    UserLargeAdapter userLargeAdapter;
    ConstraintLayout snapCons;
    LinearLayout userCons;
    boolean isOrderbyTrending = false;
    MaxRecyclerAdapter maxRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mContext = getActivity();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        snapCons = view.findViewById(R.id.home_snapCons);
        userCons = view.findViewById(R.id.home_recUserCons);
        filterBtn = view.findViewById(R.id.home_toolbar_filtericon);
        toolbarTitle = view.findViewById(R.id.home_toolbar_title);
        lottieAnimationView = view.findViewById(R.id.home_recUserLottie);
        swipeRefreshLayout = view.findViewById(R.id.home_swiperefresh);
        recyclerView = view.findViewById(R.id.home_recyclerview);
        userRV = view.findViewById(R.id.home_recUserRV);
        shimmerLayout = view.findViewById(R.id.home_shimmer_layout);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        userRV.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.setHasFixedSize(false);
        shimmerLayout.startShimmer();
        snapCons.setVisibility(View.VISIBLE);
        userCons.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFollowing();
            }
        });

        toolbarTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        filterBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                bottomSheetDialog.setContentView(R.layout.bottomsheet_filter_snap);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                CardView itemView = bottomSheetDialog.findViewById(R.id.filter_cardview);
                TextView textView = bottomSheetDialog.findViewById(R.id.filter_item_txt);
                ImageView icon = bottomSheetDialog.findViewById(R.id.filter_icon);

                if (isOrderbyTrending) {
                    icon.setImageResource(R.drawable.ic_time);
                    textView.setText(getString(R.string.orderbylatest));
                } else {
                    icon.setImageResource(R.drawable.ic_fire);
                    textView.setText(getString(R.string.orderbytrending));
                }

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isOrderbyTrending) {
                            isOrderbyTrending = false;
                            getFollowing();
                            bottomSheetDialog.dismiss();
                        } else {
                            isOrderbyTrending = true;
                            getFollowing();
                            bottomSheetDialog.dismiss();
                        }
                    }
                });

                bottomSheetDialog.show();
            }
        });

        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(getContext()).setMediationProvider("max");
        AppLovinSdk.initializeSdk(getContext(), new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                // AppLovin SDK is initialized, start loading ads
                //Toast.makeText(MainActivity.this, "AppLovin SDK Initialized", Toast.LENGTH_SHORT).show();
                getFollowing();
            }
        });

        return view;
    }

    private void getFollowing() {
        List<String> followingUserIDList = new ArrayList<>();
        List<String> list = new ArrayList<>();
        followingUserIDList.clear();
        list.clear();

        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot ds : documentSnapshotList) {
                    UserFollowing user = ds.toObject(UserFollowing.class);
                    //UserFollowing_RTDB user = ds.get(UserFollowing_RTDB.class);
                    followingUserIDList.add(user.getFollowing_user_id());
                }

                if (followingUserIDList.size() > 9) {
                    Collections.shuffle(followingUserIDList);
                    list.add(mAuth.getCurrentUser().getUid());
                    for (int i = 0; i < 9; i++) {
                        list.add(followingUserIDList.get(i));
                        if (list.size() >= 10) {
                            if (isOrderbyTrending) {
                                mQuery = firestore.collection("snap")
                                        .whereIn("author_id", list)
                                        .whereEqualTo("visibility_private", false)
                                        .orderBy("popularity", Query.Direction.DESCENDING);
                            } else {
                                mQuery = firestore.collection("snap")
                                        .whereIn("author_id", list)
                                        .whereEqualTo("visibility_private", false)
                                        .orderBy("created_at", Query.Direction.DESCENDING);
                            }
                            mQuery.limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (queryDocumentSnapshots.isEmpty()) {
                                        getUserInterestedTag();
                                        lottieAnimationView.playAnimation();
                                    } else {
                                        lottieAnimationView.pauseAnimation();
                                        snapCons.setVisibility(View.VISIBLE);
                                        userCons.setVisibility(View.GONE);
                                        if (recyclerView.getAdapter() != null) {
                                            updateSnap(mContext);
                                        } else {
                                            getSnap();
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    followingUserIDList.add(mAuth.getCurrentUser().getUid());
                    if (isOrderbyTrending) {
                        mQuery = firestore.collection("snap")
                                .whereIn("author_id", followingUserIDList)
                                .whereEqualTo("visibility_private", false)
                                .orderBy("popularity", Query.Direction.DESCENDING);
                    } else {
                        mQuery = firestore.collection("snap")
                                .whereIn("author_id", followingUserIDList)
                                .whereEqualTo("visibility_private", false)
                                .orderBy("created_at", Query.Direction.DESCENDING);
                    }

                    mQuery.limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                getUserInterestedTag();
                                lottieAnimationView.playAnimation();
                            } else {
                                lottieAnimationView.pauseAnimation();
                                snapCons.setVisibility(View.VISIBLE);
                                userCons.setVisibility(View.GONE);
                                if (recyclerView.getAdapter() != null) {
                                    updateSnap(mContext);
                                } else {
                                    getSnap();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void updateSnap(Context context) {
        final int pageSize = 3;
        final int prefetchDistance = 3;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) context)
                .setQuery(mQuery, pagingConfig, Snap.class)
                .build();
        mAdapter.updateOptions(options);
    }

    public void getSnap() {
        // Init Paging Configuration
        final int pageSize = 3;
        final int prefetchDistance = 3;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Snap>()
                .setLifecycleOwner((LifecycleOwner) getContext())
                .setQuery(mQuery, pagingConfig, Snap.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<Snap, Home_SnapLargeViewHolder>(options) {
            @NonNull
            @Override
            public Home_SnapLargeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_snap_large, parent, false);
                return new Home_SnapLargeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Home_SnapLargeViewHolder viewHolder, int i, @NonNull Snap snap) {
                // Bind to ViewHolder
                viewHolder.bind(snap);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull Home_SnapLargeViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                shimmerLayout.setVisibility(View.GONE);
                shimmerLayout.stopShimmer();
            }
        };

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(R.layout.nativead_snap_large)
                .setTitleTextViewId(R.id.title_text_view)
                .setBodyTextViewId(R.id.body_text_view)
                .setAdvertiserTextViewId(R.id.advertiser_text_view)
                .setIconImageViewId(R.id.icon_image_view)
                .setMediaContentViewGroupId(R.id.media_view_container)
                .setOptionsContentViewGroupId(R.id.options_view)
                .setCallToActionButtonId(R.id.cta_button)
                .build();

        MaxAdPlacerSettings settings = new MaxAdPlacerSettings("9d4d8db70d8d6c95");
        settings.addFixedPosition(1);
        settings.setRepeatingInterval(4);
        maxRecyclerAdapter = new MaxRecyclerAdapter(settings, mAdapter, getActivity());
        maxRecyclerAdapter.getAdPlacer().setNativeAdViewBinder(binder);
        maxRecyclerAdapter.getAdPlacer().setAdSize(-1, -1);

        recyclerView.setAdapter(maxRecyclerAdapter);

        maxRecyclerAdapter.loadAds();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (maxRecyclerAdapter != null) {
            maxRecyclerAdapter.destroy();
        }
    }

    private void getUserInterestedTag() {
        shimmerLayout.setVisibility(View.GONE);
        shimmerLayout.stopShimmer();
        snapCons.setVisibility(View.GONE);
        userCons.setVisibility(View.VISIBLE);
        List<String> interestedTagList = new ArrayList<>();

        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("interested_tag")
                .orderBy("last_interacted", Query.Direction.DESCENDING).limit(10)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // User Interested Tag exists
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot ds : list) {
                                UserInterestedTag tag = ds.toObject(UserInterestedTag.class);
                                interestedTagList.add(tag.getTag());
                            }
                            getRecUser(interestedTagList);
                        } else {
                            // User Interested Tag does not exist
                            getRandomRecUser();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getRecUser(List<String> tags) {
        List<User> userList = new ArrayList<>();
        userLargeAdapter = new UserLargeAdapter(getContext(), userList);
        userRV.setAdapter(userLargeAdapter);
        if (!tags.isEmpty()) {
            firestore.collection("user").whereEqualTo("suspended", false)
                    .whereNotEqualTo("keyword", null)
                    .whereArrayContainsAny("keyword", tags).limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            userList.clear();
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot ds : list) {
                                User user = ds.toObject(User.class);
                                if (!user.getUser_id().equals(mAuth.getCurrentUser().getUid())) {
                                    userList.add(user);
                                }
                            }
                            if (userList.isEmpty()) {
                                getRandomRecUser();
                            } else {
                                userLargeAdapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void getRandomRecUser() {
        List<User> userList = new ArrayList<>();
        userLargeAdapter = new UserLargeAdapter(getContext(), userList);
        userRV.setAdapter(userLargeAdapter);
        firestore.collection("user").whereEqualTo("suspended", false)
                .whereNotEqualTo("keyword", null)/*
                    .whereArrayContainsAny("keyword", tags)*/.limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        userList.clear();
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : list) {
                            User user = ds.toObject(User.class);
                            if (!user.getUser_id().equals(mAuth.getCurrentUser().getUid())) {
                                userList.add(user);
                            }
                        }
                        userLargeAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}