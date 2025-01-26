package com.euichankim.idolsnapandroid.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.euichankim.idolsnapandroid.Adapter.CollectionLargeAdapter;
import com.euichankim.idolsnapandroid.Model.Collection;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileCollectionFragment extends Fragment {

    public static RecyclerView recyclerView;
    TextView noCollectionTxt;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<Collection> collectionList;
    CollectionLargeAdapter collectionAdapter;
    String user_id;

    public ProfileCollectionFragment(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_collection, container, false);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        collectionList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.profile_collection_rv);
        noCollectionTxt = view.findViewById(R.id.profile_collection_nocollectionTxt);
        swipeRefreshLayout = view.findViewById(R.id.profilecollection_swiperefresh);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(false);
        collectionAdapter = new CollectionLargeAdapter(getContext(), collectionList);
        recyclerView.setAdapter(collectionAdapter);
        swipeRefreshLayout.setRefreshing(true);
        noCollectionTxt.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCollection();
            }
        });

        getCollection();

        return view;
    }

    private void getCollection() {
        collectionList.clear();

        Query query;
        if (user_id.equals(mAuth.getCurrentUser().getUid())){
            query = firestore.collection("collection")
                    .whereEqualTo("author_id", user_id)
                    .orderBy("last_added_at", Query.Direction.DESCENDING);
        }else{
            query = firestore.collection("collection")
                    .whereEqualTo("author_id", user_id)
                    .whereEqualTo("visibility", "public")
                    .orderBy("last_added_at", Query.Direction.DESCENDING);
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : list) {
                            Collection collection = ds.toObject(Collection.class);
                            collectionList.add(collection);
                        }
                        collectionAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }
}