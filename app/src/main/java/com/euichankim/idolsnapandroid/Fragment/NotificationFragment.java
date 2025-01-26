package com.euichankim.idolsnapandroid.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Activity.NotificationActivity;
import com.euichankim.idolsnapandroid.Adapter.NotificationAdapter;
import com.euichankim.idolsnapandroid.Adapter.UserLargeAdapter;
import com.euichankim.idolsnapandroid.Model.Notification;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.Model.UserInterestedTag;
import com.euichankim.idolsnapandroid.Model.UserStrikeRecord;
import com.euichankim.idolsnapandroid.R;
import com.euichankim.idolsnapandroid.ViewHolder.Home_SnapSmallViewHolder;
import com.euichankim.idolsnapandroid.ViewHolder.Notification_ViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;;import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class NotificationFragment extends Fragment {

    public static SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView notiRV, userRV;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    public static Query mQuery;
    public static NotificationAdapter notificationAdapter;
    public static List<Notification> notificationList;
    public static Context mContext;
    ImageButton recentNotiMoreBtn;
    public static LinearLayout recentNotiLin;
    LinearLayout recUserLin;
    UserLargeAdapter userLargeAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        NotificationFragment.mContext = getActivity();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mQuery = firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                .collection("notification")
                .orderBy("notified_at", Query.Direction.DESCENDING)
                .limit(4);
        recentNotiLin = view.findViewById(R.id.notification_recentNotiLin);
        recUserLin = view.findViewById(R.id.notification_recUserLin);
        swipeRefreshLayout = view.findViewById(R.id.notification_swiperefresh);
        recentNotiMoreBtn = view.findViewById(R.id.notification_recentMoreBtn);
        notiRV = view.findViewById(R.id.notification_recyclerview);
        userRV = view.findViewById(R.id.notification_recUserRV);
        notiRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        userRV.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        notiRV.setAdapter(notificationAdapter);
        recentNotiLin.setVisibility(View.GONE);
        recUserLin.setVisibility(View.GONE);
        userRV.setNestedScrollingEnabled(false);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNotifications();
                getUserInterestedTag();
            }
        });

        recentNotiMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(getContext(), NotificationActivity.class));
                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        getNotifications();
        getUserInterestedTag();

        return view;
    }

    public static void getNotifications() {
        notificationList.clear();
        mQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot ds : list) {
                    Notification notification = ds.toObject(Notification.class);
                    notificationList.add(notification);
                }
                notificationAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

                if (notificationList.isEmpty()) {
                    recentNotiLin.setVisibility(View.GONE);
                } else {
                    recentNotiLin.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getUserInterestedTag() {
        //shimmerLayout.setVisibility(View.GONE);
        //shimmerLayout.stopShimmer();
        //snapCons.setVisibility(View.GONE);
        //userCons.setVisibility(View.VISIBLE);
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
                                recUserLin.setVisibility(View.VISIBLE);
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
                        recUserLin.setVisibility(View.VISIBLE);
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