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

import com.euichankim.idolsnapandroid.Activity.SearchResultActivity;
import com.euichankim.idolsnapandroid.Model.Tag;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.ViewHolder.Tag_ViewHolder;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchResultTagFragment extends Fragment {

    String searchedTag;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    List<Tag> tagList;
    FirebaseFirestore firestore;
    Query query;
    FirestorePagingAdapter<Tag, Tag_ViewHolder> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result_tag, container, false);

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
        query = firestore.collection("tag")
                .whereEqualTo("tag", keyword_list.get(0))
//                .whereArrayContainsAny("tag", keyword_list)
                .whereEqualTo("available", true);
        swipeRefreshLayout = view.findViewById(R.id.srt_swiperefresh);
        recyclerView = view.findViewById(R.id.srt_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        tagList = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateTag();
            }
        });

        getHashTag();

        return view;
    }

    private void updateTag() {
        final int pageSize = 3;
        final int prefetchDistance = 3;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Tag>()
                .setLifecycleOwner((LifecycleOwner) getContext())
                .setQuery(query, pagingConfig, Tag.class)
                .build();
        mAdapter.updateOptions(options);
    }

    private void getHashTag(){
        // Init Paging Configuration
        final int pageSize = 3;
        final int prefetchDistance = 3;
        final boolean enablePlaceholder = false;
        PagingConfig pagingConfig = new PagingConfig(pageSize, prefetchDistance, enablePlaceholder);

        // Init Adapter Configuration
        FirestorePagingOptions options = new FirestorePagingOptions.Builder<Tag>()
                .setLifecycleOwner((LifecycleOwner) getContext())
                .setQuery(query, pagingConfig, Tag.class)
                .build();

        // Instantiate Paging Adapter
        mAdapter = new FirestorePagingAdapter<Tag, Tag_ViewHolder>(options) {
            @NonNull
            @Override
            public Tag_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_tag, parent, false);
                return new Tag_ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Tag_ViewHolder viewHolder, int i, @NonNull Tag tag) {
                // Bind to ViewHolder
                viewHolder.bind(tag);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull Tag_ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                swipeRefreshLayout.setRefreshing(false);
                //shimmerLayout.setVisibility(View.GONE);
                //shimmerLayout.stopShimmer();
            }
        };
        recyclerView.setAdapter(mAdapter);
    }
}