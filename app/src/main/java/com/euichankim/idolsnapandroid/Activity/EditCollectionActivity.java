package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Model.CollectionItem;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditCollectionActivity extends AppCompatActivity {

    EditText nameEdtx;
    TextView deleteTxt;
    ImageButton backBtn, saveBtn;
    ProgressBar progressBar;
    ImageView imageView;
    String collection_id;
    Switch visibilitySwitch;

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection);

        collection_id = getIntent().getStringExtra("collection_id");
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        nameEdtx = findViewById(R.id.editcollection_nameEdtx);
        deleteTxt = findViewById(R.id.editcollection_deleteTxt);
        backBtn = findViewById(R.id.editcollection_backBtn);
        saveBtn = findViewById(R.id.editcollection_completeBtn);
        visibilitySwitch = findViewById(R.id.editcollection_visibilitySwitch);
        progressBar = findViewById(R.id.editcollection_progressbar);
        imageView = findViewById(R.id.editcollection_imageview);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameEdtx.getText().toString().isEmpty()) {
                    updateCollectionInfo();
                }
            }
        });

        deleteTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCollection();
            }
        });

        getCollectionInfo();
    }

    private void updateCollectionInfo() {
        saveBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        nameEdtx.setEnabled(false);
        deleteTxt.setEnabled(false);
        backBtn.setEnabled(false);
        String visibility;
        if (visibilitySwitch.isChecked()) {
            visibility = "private";
        } else {
            visibility = "public";
        }
        Map<String, Object> collectionMap = new HashMap<>();
        collectionMap.put("title", nameEdtx.getText().toString());
        collectionMap.put("visibility", visibility);
        firestore.collection("collection").document(collection_id).update(collectionMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditCollectionActivity.this, getString(R.string.collectioneditcomplete), Toast.LENGTH_SHORT).show();
                        finish();
                        saveBtn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        nameEdtx.setEnabled(true);
                        deleteTxt.setEnabled(true);
                        backBtn.setEnabled(true);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveBtn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        nameEdtx.setEnabled(true);
                        deleteTxt.setEnabled(true);
                        backBtn.setEnabled(true);
                    }
                });
    }

    private void getCollectionInfo() {
        firestore.collection("collection").document(collection_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                nameEdtx.setText(documentSnapshot.getString("title"));
                if (documentSnapshot.getString("visibility").equals("public")) {
                    visibilitySwitch.setChecked(false);
                } else {
                    visibilitySwitch.setChecked(true);
                }
                getThumbnailImg(collection_id);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void getThumbnailImg(String collection_id) {
        firestore.collection("collection")
                .document(collection_id).collection("collection_item")
                .orderBy("added_at", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // no snap exists in collection_item
                            Glide.with(EditCollectionActivity.this).load(new ColorDrawable(ContextCompat.getColor(EditCollectionActivity.this, R.color.lightgrey)))
                                    .diskCacheStrategy(DiskCacheStrategy.DATA).into(imageView);
                        } else {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot ds : list) {
                                CollectionItem collectionItem = ds.toObject(CollectionItem.class);

                                //getSnapImg
                                firestore.collection("snap").document(collectionItem.getSnap_id()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String author_id, content_id;
                                        author_id = documentSnapshot.getString("author_id");
                                        content_id = documentSnapshot.getString("content_id");
                                        String content_url = "https://cdn.idolsnap.net/snap/" + author_id + "/" + content_id + "/" + content_id + "_512x512";
                                        Glide.with(EditCollectionActivity.this)
                                                .load(content_url)
                                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                                .error(R.drawable.ic_placeholder)
                                                .into(imageView);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCollection() {
        firestore.collection("collection").document(collection_id).collection("collection_item")
                .limit(1)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(EditCollectionActivity.this);
                        bottomSheetDialog.setContentView(R.layout.bottomsheet_alert_delete);
                        bottomSheetDialog.setCanceledOnTouchOutside(true);

                        TextView deleteBtn, cancelBtn, titleTxt, descTxt;
                        ProgressBar progressBar;
                        titleTxt = bottomSheetDialog.findViewById(R.id.alertdelete_titleTxt);
                        descTxt = bottomSheetDialog.findViewById(R.id.alertdelete_descTxt);
                        deleteBtn = bottomSheetDialog.findViewById(R.id.alertdelete_deleteTxt);
                        cancelBtn = bottomSheetDialog.findViewById(R.id.alertdelete_cancelTxt);
                        progressBar = bottomSheetDialog.findViewById(R.id.alertdelete_progressbar);
                        deleteBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        titleTxt.setText(getString(R.string.deletecollection));
                        descTxt.setText(getString(R.string.alert_deletecollectiondesc));

                        deleteBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteBtn.setVisibility(View.GONE);
                                cancelBtn.setVisibility(View.GONE);
                                progressBar.setVisibility(View.VISIBLE);
                                bottomSheetDialog.setCancelable(false);
                                firestore.collection("collection").document(collection_id).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(EditCollectionActivity.this, getString(R.string.collectiondeleted), Toast.LENGTH_SHORT).show();
                                                bottomSheetDialog.dismiss();
                                                startActivity(new Intent(EditCollectionActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                finish();
                                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(EditCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                deleteBtn.setVisibility(View.VISIBLE);
                                                cancelBtn.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                                bottomSheetDialog.setCancelable(true);
                                            }
                                        });
                            }
                        });

                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bottomSheetDialog.dismiss();
                            }
                        });

                        bottomSheetDialog.show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditCollectionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}