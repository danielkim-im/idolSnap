package com.euichankim.idolsnapandroid.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.CollectionSnapActivity;
import com.euichankim.idolsnapandroid.Model.Collection;
import com.euichankim.idolsnapandroid.Model.CollectionItem;
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

public class CollectionLargeAdapter extends RecyclerView.Adapter<CollectionLargeAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Collection> collections;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    public CollectionLargeAdapter(Context mContext, ArrayList<Collection> collections) {
        this.mContext = mContext;
        this.collections = collections;
    }

    @NonNull
    @Override
    public CollectionLargeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_collection_large, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionLargeAdapter.ViewHolder holder, int position) {
        if (collections.get(position).getVisibility().equals("private")){
            Glide.with(mContext).load(R.drawable.ic_lock).into(holder.icon);
        }else{
            Glide.with(mContext).load(R.drawable.ic_people).into(holder.icon);
        }
        holder.titleTxt.setText(collections.get(position).getTitle());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent collectionIntent = new Intent(mContext, CollectionSnapActivity.class);
                collectionIntent.putExtra("collection_id", collections.get(position).getCollection_id());
                collectionIntent.putExtra("collection_title", collections.get(position).getTitle());
                collectionIntent.putExtra("author_id", collections.get(position).getAuthor_id());
                mContext.startActivity(collectionIntent);
                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        getThumbnailImg(collections.get(position).getCollection_id(), holder.imageView);
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView imageView, icon;
        TextView titleTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            cardView = itemView.findViewById(R.id.cil_cardview);
            imageView = itemView.findViewById(R.id.cil_imageview);
            icon = itemView.findViewById(R.id.cil_icon);
            titleTxt = itemView.findViewById(R.id.cil_textview);
        }
    }

    private void getThumbnailImg(String collection_id, ImageView imageView){
        firestore.collection("collection")
                .document(collection_id).collection("collection_item")
                .orderBy("added_at", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()){
                            // no snap exists in collection_item
                            Glide.with(mContext).load(new ColorDrawable(ContextCompat.getColor(mContext, R.color.lightgrey)))
                                    .diskCacheStrategy(DiskCacheStrategy.DATA).into(imageView);
                        }else{
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot ds : list) {
                                CollectionItem collectionItem = ds.toObject(CollectionItem.class);

                                //getSnapImg
                                firestore.collection("snap").document(collectionItem.getSnap_id()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String author_id = documentSnapshot.getString("author_id");
                                        String content_id = documentSnapshot.getString("content_id");
                                        String content_url = "https://cdn.idolsnap.net/snap/"+author_id+"/"+content_id+"/"+content_id+"_512x512";
                                        Glide.with(mContext)
                                                .load(content_url)
                                                .override(512, 512)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                                .error(R.drawable.ic_placeholder)
                                                .into(imageView);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
