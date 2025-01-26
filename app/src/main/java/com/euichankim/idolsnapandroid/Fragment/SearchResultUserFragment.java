package com.euichankim.idolsnapandroid.Fragment;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Activity.SearchResultActivity;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.User_SmallViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchResultUserFragment extends Fragment {

    String searchedTag;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    Query query;
    FirestorePagingAdapter<User, User_SmallViewHolder> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result_user, container, false);

        searchedTag = SearchResultActivity.searchedTag;

        String[] words = searchedTag.split("\\s+");
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
        query = firestore.collection("user")
                .whereArrayContainsAny("keyword", keyword_list)
                .whereEqualTo("suspended", false);
        swipeRefreshLayout = view.findViewById(R.id.sru_swiperefresh);
        recyclerView = view.findViewById(R.id.sru_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateUser();
            }
        });

        getUser();

        return view;
    }

    private void updateUser() {
        final int pageSize = 3;
        final int prefetchDistance = 3;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner((LifecycleOwner) getContext())
                .setQuery(query, pagingConfig, User.class)
                .build();
        mAdapter.updateOptions(options);
    }

    private void getUser() {
        // Init Paging Configuration
        final int pageSize = 3;
        final int prefetchDistance = 3;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner((LifecycleOwner) getContext())
                .setQuery(query, pagingConfig, User.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<User, User_SmallViewHolder>(options) {
            @NonNull
            @Override
            public User_SmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_user_small, parent, false);
                return new User_SmallViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull User_SmallViewHolder viewHolder, int i, @NonNull User user) {
                // Bind to ViewHolder
                viewHolder.searchresultuserfragment_bind(user);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull User_SmallViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                //shimmerLayout.setVisibility(View.GONE);
                //shimmerLayout.stopShimmer();
            }
        };
        recyclerView.setAdapter(mAdapter);
    }
}