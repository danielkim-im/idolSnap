package com.euichankim.idolsnapandroid.ViewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.EditSnapActivity;
import com.euichankim.idolsnapandroid.Activity.MainActivity;
import com.euichankim.idolsnapandroid.Activity.SnapActivity;
import com.euichankim.idolsnapandroid.Adapter.CollectionSmallAdapter;
import com.euichankim.idolsnapandroid.Model.Collection;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Home_SnapSmallViewHolder extends RecyclerView.ViewHolder {
    CardView cardView;
    ImageView imageView;
    TextView textView, unhideSnapTxt;
    ImageButton moreBtn;
    public static BottomSheetDialog collectionBottomSheet;
    public static RecyclerView bsrecyclerView;
    public static ProgressBar bsProgressbar;
    public static ImageButton bscloseBtn, bsCreateBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseStorage firebaseStorage;
    String author_name;
    ConstraintLayout blurCons;
    boolean isHidden = false;

    public Home_SnapSmallViewHolder(@NonNull View itemView) {
        super(itemView);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        cardView = itemView.findViewById(R.id.searchsnap_cardview);
        imageView = itemView.findViewById(R.id.searchsnap_img);
        textView = itemView.findViewById(R.id.searchsnap_textview);
        moreBtn = itemView.findViewById(R.id.searchsnap_moreBtn);
        blurCons = itemView.findViewById(R.id.searchsnap_blurCons);
        unhideSnapTxt = itemView.findViewById(R.id.searchsnap_unhidesnapTxt);
    }

    public void bind(Snap snap) {
        loadSnap(snap);
    }

    private void loadSnap(Snap snap) {
        String content_url = "https://cdn.idolsnap.net/snap/" + snap.getAuthor_id() + "/" + snap.getContent_id() + "/" + snap.getContent_id() + "_512x512";
        Glide.with(itemView)
                .load(content_url)
                .override(512, 512)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.drawable.ic_placeholder)
                .into(imageView);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHidden == false) {
                    Intent fullScreenIntent = new Intent(itemView.getContext(), SnapActivity.class);
                    fullScreenIntent.putExtra("snap_id", snap.getSnap_id());
                    itemView.getContext().startActivity(fullScreenIntent);
                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isHidden == false) {
                    snapMoreOption(snap);
                }
                return false;
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHidden == false) {
                    snapMoreOption(snap);
                }
            }
        });

        unhideSnapTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHidden == true) {
                    firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                            .collection("hide").document(snap.getSnap_id()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    bind(snap);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        checkIfHidden(snap.getSnap_id(), snap.getDescription());
    }

    private void checkIfHidden(String snap_id, String description) {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("hide").document(snap_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //hidden
                    blurCons.setVisibility(View.VISIBLE);
                    isHidden = true;
                    textView.setText(itemView.getContext().getString(R.string.hiddensnap));
                } else {
                    // not hidden
                    blurCons.setVisibility(View.GONE);
                    isHidden = false;
                    textView.setText(description);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void snapMoreOption(Snap snap) {
        BottomSheetDialog moreBottomSheet = new BottomSheetDialog(itemView.getContext());
        moreBottomSheet.setContentView(R.layout.bottomsheet_snap_more);
        moreBottomSheet.setCanceledOnTouchOutside(true);

        LinearLayout editLin, shareLin, linkLin, saveLin, deleteLin, reportLin, hideLin;
        editLin = moreBottomSheet.findViewById(R.id.snap_more_edit);
        shareLin = moreBottomSheet.findViewById(R.id.snap_more_share);
        linkLin = moreBottomSheet.findViewById(R.id.snap_more_link);
        saveLin = moreBottomSheet.findViewById(R.id.snap_more_save);
        deleteLin = moreBottomSheet.findViewById(R.id.snap_more_delete);
        reportLin = moreBottomSheet.findViewById(R.id.snap_more_report);
        hideLin = moreBottomSheet.findViewById(R.id.snap_more_hide);

        if (mAuth.getCurrentUser().getUid().equals(snap.getAuthor_id())) {
            editLin.setVisibility(View.VISIBLE);
            shareLin.setVisibility(View.VISIBLE);
            linkLin.setVisibility(View.VISIBLE);
            saveLin.setVisibility(View.VISIBLE);
            deleteLin.setVisibility(View.VISIBLE);
            reportLin.setVisibility(View.GONE);
            hideLin.setVisibility(View.GONE);
        } else {
            editLin.setVisibility(View.GONE);
            shareLin.setVisibility(View.VISIBLE);
            linkLin.setVisibility(View.VISIBLE);
            saveLin.setVisibility(View.VISIBLE);
            deleteLin.setVisibility(View.GONE);
            reportLin.setVisibility(View.VISIBLE);
            hideLin.setVisibility(View.VISIBLE);
        }

        editLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreBottomSheet.dismiss();
                Intent editSnapIntent = new Intent(itemView.getContext(), EditSnapActivity.class);
                editSnapIntent.putExtra("snap_id", snap.getSnap_id());
                itemView.getContext().startActivity(editSnapIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        shareLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareSnap(snap, "share");
                moreBottomSheet.dismiss();
            }
        });

        linkLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareSnap(snap, "copylink");
                moreBottomSheet.dismiss();
            }
        });

        saveLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreBottomSheet.dismiss();
                showCollection(snap.getSnap_id());
            }
        });

        deleteLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreBottomSheet.dismiss();
                deleteSnap(snap);
            }
        });

        reportLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("reporter_id", mAuth.getCurrentUser().getUid());
                hashMap.put("reported_at", FieldValue.serverTimestamp());
                firestore.collection("report_snap").document(snap.getSnap_id())
                        .collection("reporter")
                        .document(mAuth.getCurrentUser().getUid()).set(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.reportsent), Toast.LENGTH_SHORT).show();
                                moreBottomSheet.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                firestore.collection("report_snap").document(snap.getSnap_id())
                                        .collection("reporter")
                                        .document(mAuth.getCurrentUser().getUid())
                                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.alreadyreported), Toast.LENGTH_SHORT).show();
                                                    moreBottomSheet.dismiss();
                                                } else {
                                                    Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });

        hideLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("type", "snap");
                hashMap.put("snap_id", snap.getSnap_id());
                hashMap.put("hidden_since", FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                        .collection("hide").document(snap.getSnap_id()).set(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                bind(snap);
                                moreBottomSheet.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        moreBottomSheet.show();
    }

    private void shareSnap(Snap snap, String mode) {
        firestore.collection("user").document(snap.getAuthor_id()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                author_name = documentSnapshot.getString("name");
                String imageurl = "https://cdn.idolsnap.net/snap/" + snap.getAuthor_id() + "/" + snap.getContent_id() + "/" + snap.getContent_id() + "_512x512";
                String title;
                if (Locale.getDefault().getLanguage().equals("ko")) {
                    title = "IdolSnap의 " + author_name + "님";
                } else {
                    title = author_name + " on IdolSnap";
                }
                String description = snap.getDescription();
                Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse("https://www.idolsnap.net?snap_id=" + snap.getSnap_id()))
                        .setDomainUriPrefix("https://idolsnapapp.page.link")
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(title).setDescription(description).setImageUrl(Uri.parse(imageurl)).build())
                        //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                        .buildShortDynamicLink()
                        .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                            @Override
                            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                if (task.isSuccessful()) {
                                    // Short link created
                                    Uri shortLink = task.getResult().getShortLink();
                                    //Uri flowchartLink = task.getResult().getPreviewLink();

                                    if (mode.equals("share")) {
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                        sendIntent.setType("text/plain");

                                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                                        itemView.getContext().startActivity(shareIntent);
                                    } else if (mode.equals("copylink")) {
                                        copyClipboard(itemView.getContext(), shortLink.toString());
                                    }
                                } else {
                                    // Error
                                    // ...
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                author_name = null;
            }
        });

    }

    private void copyClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private void deleteSnap(Snap snap) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(itemView.getContext());
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
        titleTxt.setText(itemView.getContext().getString(R.string.alert_deletesnap));
        descTxt.setText(itemView.getContext().getString(R.string.alert_deletesnapdesc));

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                bottomSheetDialog.setCancelable(false);

                firestore.collection("snap").document(snap.getSnap_id()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.snapdeleted), Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();

                        /*if (HomeRecommendFragment.mContext != null) {
                            HomeRecommendFragment.get(HomeRecommendFragment.mContext);
                        } else {
                            itemView.getContext().startActivity(new Intent(itemView.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            ((Activity) itemView.getContext()).finish();
                            ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }*/
                        itemView.getContext().startActivity(new Intent(itemView.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        ((Activity) itemView.getContext()).finish();
                        ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                snapMoreOption(snap);
            }
        });

        bottomSheetDialog.show();
    }

    private void showCollection(String snap_id) {
        collectionBottomSheet = new BottomSheetDialog(itemView.getContext());
        collectionBottomSheet.setContentView(R.layout.bottomsheet_collection_list);
        collectionBottomSheet.setCanceledOnTouchOutside(true);
        ArrayList<Collection> collectionList;
        CollectionSmallAdapter collectionAdapter;
        collectionList = new ArrayList<>();
        bsrecyclerView = collectionBottomSheet.findViewById(R.id.largesnapbs_rv);
        bscloseBtn = collectionBottomSheet.findViewById(R.id.largesnapbs_closeBtn);
        bsCreateBtn = collectionBottomSheet.findViewById(R.id.largesnapbs_addBtn);
        bsProgressbar = collectionBottomSheet.findViewById(R.id.largesnapbs_progressbar);
        collectionAdapter = new CollectionSmallAdapter(itemView.getContext(), collectionList, snap_id, bsrecyclerView, bsProgressbar, bscloseBtn, bsCreateBtn, collectionBottomSheet);
        bsrecyclerView.setAdapter(collectionAdapter);
        bsProgressbar.setVisibility(View.VISIBLE);
        bsrecyclerView.setVisibility(View.GONE);

        firestore.collection("collection")
                .whereEqualTo("author_id", mAuth.getCurrentUser().getUid())
                .orderBy("last_added_at", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : list) {
                            Collection collection = ds.toObject(Collection.class);
                            collectionList.add(collection);
                        }
                        if (collectionList.size() == 0) {
                            collectionBottomSheet.dismiss();
                            showCreateCollectionBS(snap_id);
                        }
                        collectionAdapter.notifyDataSetChanged();
                        bsProgressbar.setVisibility(View.GONE);
                        bsrecyclerView.setVisibility(View.VISIBLE);
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        bsCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectionBottomSheet.dismiss();
                showCreateCollectionBS(snap_id);
            }
        });

        bscloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectionBottomSheet.dismiss();
            }
        });

        collectionBottomSheet.show();
    }

    private void showCreateCollectionBS(String snap_id) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(itemView.getContext());
        bottomSheetDialog.setContentView(R.layout.bottomsheet_collection_create);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        ImageButton closeBtn, createBtn;
        ProgressBar progressBar;
        EditText editText;
        closeBtn = bottomSheetDialog.findViewById(R.id.createcollectionbs_closeBtn);
        createBtn = bottomSheetDialog.findViewById(R.id.createcollectionbs_createBtn);
        editText = bottomSheetDialog.findViewById(R.id.createcollectionbs_edtx);
        progressBar = bottomSheetDialog.findViewById(R.id.createcollectionbs_progressbar);

        InputMethodManager imm = (InputMethodManager) itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().isEmpty()) {
                    createBtn.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    editText.setEnabled(false);
                    closeBtn.setEnabled(false);

                    Collection collection = new Collection(editText.getText().toString(), mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp(), FieldValue.serverTimestamp(), "public");
                    firestore.collection("collection").add(collection)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Map<String, Object> collection_id = new HashMap<>();
                                    collection_id.put("collection_id", documentReference.getId());
                                    firestore.collection("collection").document(documentReference.getId()).update(collection_id)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        bottomSheetDialog.dismiss();
                                                        showCollection(snap_id);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    createBtn.setVisibility(View.VISIBLE);
                                                    progressBar.setVisibility(View.GONE);
                                                    editText.setEnabled(true);
                                                    closeBtn.setEnabled(true);
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    createBtn.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    editText.setEnabled(true);
                                    closeBtn.setEnabled(true);
                                }
                            });
                }
            }
        });

        bottomSheetDialog.show();
    }
}
