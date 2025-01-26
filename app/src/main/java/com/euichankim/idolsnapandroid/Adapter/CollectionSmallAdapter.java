package com.euichankim.idolsnapandroid.Adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Model.Collection;
import com.euichankim.idolsnapandroid.Model.CollectionItem;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CollectionSmallAdapter extends RecyclerView.Adapter<CollectionSmallAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Collection> collections;
    private String snap_id;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    RecyclerView bsrecyclerView;
    ProgressBar bsProgressbar;
    ImageButton bscloseBtn, bsCreateBtn;
    BottomSheetDialog collectionBottomSheet;

    public CollectionSmallAdapter(Context mContext, ArrayList<Collection> collections, String snap_id,
                                  RecyclerView bsrecyclerView, ProgressBar bsProgressbar,
                                  ImageButton bscloseBtn, ImageButton bsCreateBtn, BottomSheetDialog collectionBottomSheet) {
        this.mContext = mContext;
        this.collections = collections;
        this.snap_id = snap_id;
        this.bsrecyclerView = bsrecyclerView;
        this.bsProgressbar = bsProgressbar;
        this.bscloseBtn = bscloseBtn;
        this.bsCreateBtn = bsCreateBtn;
        this.collectionBottomSheet = collectionBottomSheet;
    }

    @NonNull
    @Override
    public CollectionSmallAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_collection_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionSmallAdapter.ViewHolder holder, int position) {
        if (collections.get(position).getVisibility().equals("private")){
            Glide.with(mContext).load(R.drawable.ic_lock).into(holder.icon);
        }else{
            Glide.with(mContext).load(R.drawable.ic_people).into(holder.icon);
        }
        holder.textView.setText(collections.get(position).getTitle());

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCollection(collections.get(position).getCollection_id(), collections.get(position).getTitle());
            }
        });

        getThumbnailImg(collections.get(position).getCollection_id(), holder.imageView);
        checkIfAlreadyAdded(collections.get(position).getCollection_id(), holder.checkIcon);
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        ImageView imageView, icon;
        TextView textView;
        ImageButton checkIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            constraintLayout = itemView.findViewById(R.id.collectionitem_cons);
            imageView = itemView.findViewById(R.id.collectionitem_img);
            icon = itemView.findViewById(R.id.collectionitem_icon);
            textView = itemView.findViewById(R.id.collectionitem_text);
            checkIcon = itemView.findViewById(R.id.collectionitem_checkIndicator);
        }
    }

    private void checkIfAlreadyAdded(String collection_id, ImageButton imageButton) {
        firestore.collection("collection").document(collection_id)
                .collection("collection_item").whereEqualTo("snap_id", snap_id)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                            imageButton.setVisibility(View.GONE);
                        } else {
                            imageButton.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        imageButton.setVisibility(View.GONE);
                    }
                });
    }

    private void addToCollection(String collection_id, String collection_title) {
        firestore.collection("collection").document(collection_id)
                .collection("collection_item").whereEqualTo("snap_id", snap_id)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                            //add to collection
                            bsrecyclerView.setVisibility(View.GONE);
                            bsProgressbar.setVisibility(View.VISIBLE);
                            bscloseBtn.setVisibility(View.GONE);
                            bsCreateBtn.setVisibility(View.GONE);

                            CollectionItem collectionItem = new CollectionItem(snap_id, mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp());
                            firestore.collection("collection").document(collection_id).collection("collection_item").document(snap_id)
                                    .set(collectionItem).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                                Toast.makeText(mContext, collection_title + "에 저장됨", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(mContext, "Saved to " + collection_title, Toast.LENGTH_SHORT).show();
                                            }
                                            collectionBottomSheet.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            //remove from collection
                            bsrecyclerView.setVisibility(View.GONE);
                            bsProgressbar.setVisibility(View.VISIBLE);
                            bscloseBtn.setVisibility(View.GONE);
                            bsCreateBtn.setVisibility(View.GONE);
                            CollectionItem collectionItem = new CollectionItem(snap_id, mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp());
                            firestore.collection("collection").document(collection_id).collection("collection_item").document(snap_id)
                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                                Toast.makeText(mContext, collection_title + "에서 삭제됨", Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(mContext, "Removed from " + collection_title, Toast.LENGTH_SHORT).show();
                                            }
                                            collectionBottomSheet.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getThumbnailImg(String collection_id, ImageView imageView) {
        firestore.collection("collection")
                .document(collection_id).collection("collection_item")
                .orderBy("added_at", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // no snap exists in collection_item
                            Glide.with(mContext).load(new ColorDrawable(ContextCompat.getColor(mContext, R.color.lightgrey)))
                                    .diskCacheStrategy(DiskCacheStrategy.DATA).into(imageView);
                        } else {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot ds : list) {
                                CollectionItem collectionItem = ds.toObject(CollectionItem.class);

                                //getSnapImg
                                firestore.collection("snap").document(collectionItem.getSnap_id()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String author_id = documentSnapshot.getString("author_id");
                                        String content_id = documentSnapshot.getString("content_id");
                                        String content_url = "https://cdn.idolsnap.net/snap/" + author_id + "/" + content_id + "/" + content_id + "_512x512";
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
