package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Adapter.CollectionSmallAdapter;
import com.euichankim.idolsnapandroid.Fragment.HomeFragment;
import com.euichankim.idolsnapandroid.Model.Collection;
import com.euichankim.idolsnapandroid.Model.Like;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.R;;
import com.github.chrisbanes.photoview.PhotoView;
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

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;

public class SnapActivity extends AppCompatActivity {

    TextView usernameTxt, likeTxt;
    CircleImageView profileImage;
    TextView descTxt, dateTxt;
    PhotoView imageView;
    ImageButton moreBtn, likeBtn, commentBtn, shareBtn, saveBtn, backBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    String snap_id, author_id, description, content_id, author_name;
    long snap_like_count;
    List<String> snap_tag = new ArrayList<>();
    BottomSheetDialog collectionBottomSheet;
    ImageButton bscloseBtn, bsCreateBtn;
    RecyclerView bsrecyclerView;
    ProgressBar bsProgressbar;
    ArrayList<String> tagList;
    ImageView verifiedIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap);

        snap_id = getIntent().getExtras().getString("snap_id");

        tagList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        backBtn = findViewById(R.id.snap_backBtn);
        usernameTxt = findViewById(R.id.snap_username);
        descTxt = findViewById(R.id.snap_description);
        dateTxt = findViewById(R.id.snap_dateTxt);
        imageView = findViewById(R.id.snap_imageview);
        likeTxt = findViewById(R.id.snap_likeTxt);
        profileImage = findViewById(R.id.snap_profilePic);
        moreBtn = findViewById(R.id.snap_moreBtn);
        likeBtn = findViewById(R.id.snap_likeBtn);
        commentBtn = findViewById(R.id.snap_commentBtn);
        verifiedIcon = findViewById(R.id.snap_verifiedicon);
        shareBtn = findViewById(R.id.snap_shareBtn);
        saveBtn = findViewById(R.id.snap_saveBtn);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(SnapActivity.this, UserProfileActivity.class);
                userIntent.putExtra("user_id", author_id);
                startActivity(userIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        likeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(SnapActivity.this, SnapLikeActivity.class);
                userIntent.putExtra("snap_id", snap_id);
                startActivity(userIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        usernameTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(SnapActivity.this, UserProfileActivity.class);
                userIntent.putExtra("user_id", author_id);
                startActivity(userIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Like like = new Like(mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp());

                if (likeBtn.getTag().equals("like")) {
                    likeBtn.setBackgroundResource(R.drawable.ic_heart_filled);
                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(SnapActivity.this, R.color.red));
                    likeBtn.setTag("liked");
                    snap_like_count = snap_like_count + 1;
                    if (Locale.getDefault().getLanguage().equals("ko")) {
                        likeTxt.setText("좋아요 " + snap_like_count + "개");
                    } else {
                        if (snap_like_count <= 1) {
                            likeTxt.setText(snap_like_count + " like");
                        } else {
                            likeTxt.setText(snap_like_count + " likes");
                        }
                    }
                    firestore.collection("snap").document(snap_id).collection("like")
                            .document(mAuth.getCurrentUser().getUid()).set(like).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    getLikeStatus(snap_id, likeBtn);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    likeBtn.setBackgroundResource(R.drawable.ic_heart_outline);
                    int nightModeFlags =
                            getApplicationContext().getResources().getConfiguration().uiMode &
                                    Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(SnapActivity.this, R.color.white));
                            break;

                        case Configuration.UI_MODE_NIGHT_NO:
                            likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(SnapActivity.this, R.color.black));
                            break;

                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            //no color change
                            break;
                    }
                    snap_like_count = snap_like_count - 1;
                    if (Locale.getDefault().getLanguage().equals("ko")) {
                        likeTxt.setText("좋아요 " + snap_like_count + "개");
                    } else {
                        if (snap_like_count <= 1) {
                            likeTxt.setText(snap_like_count + " like");
                        } else {
                            likeTxt.setText(snap_like_count + " likes");
                        }
                    }
                    likeBtn.setTag("like");
                    firestore.collection("snap").document(snap_id).collection("like").document(mAuth.getCurrentUser().getUid())
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    getLikeStatus(snap_id, likeBtn);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(SnapActivity.this, SnapCommentActivity.class);
                commentIntent.putExtra("snap_id", snap_id);
                startActivity(commentIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        imageView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                String content_url = "https://cdn.idolsnap.net/snap/" + author_id + "/" + content_id + "/" + content_id + "_1080x1080";
                Intent fullScreenIntent = new Intent(SnapActivity.this, SnapFullScreenImgActivity.class);
                fullScreenIntent.putStringArrayListExtra("taglist", tagList);
                fullScreenIntent.putExtra("content_url", content_url);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(SnapActivity.this, imageView, ViewCompat.getTransitionName(imageView));
                startActivity(fullScreenIntent, optionsCompat.toBundle());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                Like like = new Like(mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp());
                if (likeBtn.getTag().equals("like")) {
                    likeBtn.setBackgroundResource(R.drawable.ic_heart_filled);
                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(SnapActivity.this, R.color.red));
                    likeBtn.setTag("liked");
                    snap_like_count = snap_like_count + 1;
                    if (Locale.getDefault().getLanguage().equals("ko")) {
                        likeTxt.setText("좋아요 " + snap_like_count + "개");
                    } else {
                        if (snap_like_count <= 1) {
                            likeTxt.setText(snap_like_count + " like");
                        } else {
                            likeTxt.setText(snap_like_count + " likes");
                        }
                    }
                    LottieAnimationView lottieAnimationView = findViewById(R.id.snap_lottie);
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                    lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                            lottieAnimationView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animation) {
                            lottieAnimationView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animation) {

                        }
                    });
                    firestore.collection("snap").document(snap_id).collection("like")
                            .document(mAuth.getCurrentUser().getUid()).set(like).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    getLikeStatus(snap_id, likeBtn);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareSnap("share");
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCollection(snap_id);
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapMoreOption();
            }
        });

        loadSnapInfo();
    }

    private void snapMoreOption() {
        BottomSheetDialog moreBottomSheet = new BottomSheetDialog(SnapActivity.this);
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

        if (mAuth.getCurrentUser().getUid().equals(author_id)) {
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
                Intent editSnapIntent = new Intent(SnapActivity.this, EditSnapActivity.class);
                editSnapIntent.putExtra("snap_id", snap_id);
                startActivity(editSnapIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        shareLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareSnap("share");
                moreBottomSheet.dismiss();
            }
        });

        linkLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareSnap("copylink");
                moreBottomSheet.dismiss();
            }
        });

        saveLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreBottomSheet.dismiss();
                showCollection(snap_id);
            }
        });

        deleteLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreBottomSheet.dismiss();
                deleteSnap();
            }
        });

        reportLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("reporter_id", mAuth.getCurrentUser().getUid());
                hashMap.put("reported_at", FieldValue.serverTimestamp());
                firestore.collection("report_snap").document(snap_id)
                        .collection("reporter")
                        .document(mAuth.getCurrentUser().getUid()).set(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SnapActivity.this, getString(R.string.reportsent), Toast.LENGTH_SHORT).show();
                                moreBottomSheet.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                firestore.collection("report_snap").document(snap_id)
                                        .collection("reporter")
                                        .document(mAuth.getCurrentUser().getUid())
                                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    Toast.makeText(SnapActivity.this, getString(R.string.alreadyreported), Toast.LENGTH_SHORT).show();
                                                    moreBottomSheet.dismiss();
                                                } else {
                                                    Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });

        hideLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SnapActivity.this, "hide", Toast.LENGTH_SHORT).show();
            }
        });

        moreBottomSheet.show();
    }

    private void deleteSnap() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SnapActivity.this);
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
        titleTxt.setText(getString(R.string.alert_deletesnap));
        descTxt.setText(getString(R.string.alert_deletesnapdesc));

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                bottomSheetDialog.setCancelable(false);

                firestore.collection("snap").document(snap_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SnapActivity.this, getString(R.string.snapdeleted), Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        if (HomeFragment.mContext != null) {
                            HomeFragment.updateSnap(HomeFragment.mContext);
                        } else {
                            startActivity(new Intent(SnapActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                snapMoreOption();
            }
        });

        bottomSheetDialog.show();
    }

    private void showCollection(String snap_id) {
        collectionBottomSheet = new BottomSheetDialog(SnapActivity.this);
        collectionBottomSheet.setContentView(R.layout.bottomsheet_collection_list);
        collectionBottomSheet.setCanceledOnTouchOutside(true);
        ArrayList<Collection> collectionList;
        CollectionSmallAdapter collectionAdapter;
        collectionList = new ArrayList<>();
        bsrecyclerView = collectionBottomSheet.findViewById(R.id.largesnapbs_rv);
        bscloseBtn = collectionBottomSheet.findViewById(R.id.largesnapbs_closeBtn);
        bsCreateBtn = collectionBottomSheet.findViewById(R.id.largesnapbs_addBtn);
        bsProgressbar = collectionBottomSheet.findViewById(R.id.largesnapbs_progressbar);
        collectionAdapter = new CollectionSmallAdapter(SnapActivity.this, collectionList, snap_id, bsrecyclerView, bsProgressbar, bscloseBtn, bsCreateBtn, collectionBottomSheet);
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
                        Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SnapActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_collection_create);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        ImageButton closeBtn, createBtn;
        ProgressBar progressBar;
        EditText editText;
        closeBtn = bottomSheetDialog.findViewById(R.id.createcollectionbs_closeBtn);
        createBtn = bottomSheetDialog.findViewById(R.id.createcollectionbs_createBtn);
        editText = bottomSheetDialog.findViewById(R.id.createcollectionbs_edtx);
        progressBar = bottomSheetDialog.findViewById(R.id.createcollectionbs_progressbar);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                                                    Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void shareSnap(String mode) {
        String imageurl = "https://cdn.idolsnap.net/snap/" + author_id + "/" + content_id + "/" + content_id + "_512x512";
        String title;
        if (Locale.getDefault().getLanguage().equals("ko")) {
            title = "IdolSnap의 " + author_name + "님";
        } else {
            title = author_name + " on IdolSnap";
        }
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.idolsnap.net?snap_id=" + snap_id))
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
                                startActivity(shareIntent);
                            } else if (mode.equals("copylink")) {
                                copyClipboard(SnapActivity.this, shortLink.toString());
                            }
                        } else {
                            // Error
                            // ...
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loadSnapInfo() {
        firestore.collection("snap").document(snap_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Activity activity = SnapActivity.this;
                    if (activity.isFinishing())
                        return;

                    DocumentSnapshot documentSnapshot = task.getResult();
                    author_id = documentSnapshot.getString("author_id");
                    description = documentSnapshot.getString("description");
                    content_id = documentSnapshot.getString("content_id");
                    snap_tag = (List<String>) documentSnapshot.get("tag");
                    tagList.addAll((List<String>) documentSnapshot.get("keyword"));

                    getAuthorInfo(profileImage, usernameTxt, author_id);
                    getLikeCount(likeTxt, snap_id);
                    setTags(descTxt, description);
                    checkIfVerified();

                    //descTxt.setText(description);
                    dateTxt.setText(timestampToString(((Timestamp) documentSnapshot.get("created_at")).getSeconds() * 1000));

                    String content_url = "https://cdn.idolsnap.net/snap/" + author_id + "/" + content_id + "/" + content_id + "_1080x1080";
                    Glide.with(SnapActivity.this)
                            .load(content_url)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .error(R.drawable.ic_placeholder)
                            .into(imageView);

                    getLikeStatus(snap_id, likeBtn);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfVerified() {
        firestore.collection("user").document(author_id).collection("public_info").document("count").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.get("verified") != null) {
                    if (documentSnapshot.getBoolean("verified") == true) {
                        verifiedIcon.setVisibility(View.VISIBLE);
                    } else {
                        verifiedIcon.setVisibility(View.GONE);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLikeCount(TextView likeTxt, String snap_id) {
        firestore.collection("snap").document(snap_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.get("like_count") != null) {
                            snap_like_count = (long) documentSnapshot.get("like_count");
                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                likeTxt.setText("좋아요 " + snap_like_count + "개");
                            } else {
                                if (snap_like_count <= 1) {
                                    likeTxt.setText(snap_like_count + " like");
                                } else {
                                    likeTxt.setText(snap_like_count + " likes");
                                }
                            }
                        } else {
                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                likeTxt.setText("좋아요 0개");
                            } else {
                                likeTxt.setText("0 Like");
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        if (Locale.getDefault().getLanguage().equals("ko")) {
                            likeTxt.setText("좋아요 -개");
                        } else {
                            likeTxt.setText("- Like");
                        }
                    }
                });
    }

    private void getAuthorInfo(CircleImageView profileImg, TextView usernameTxt, String user_id) {
        firestore.collection("user").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                usernameTxt.setText(documentSnapshot.get("username").toString());
                author_name = documentSnapshot.get("name").toString();
                if (documentSnapshot.get("profile_img_id").equals("default")) {
                    Glide.with(SnapActivity.this).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                } else {
                    String profile_img_id = documentSnapshot.getString("profile_img_id");
                    String user_id = documentSnapshot.getString("user_id");
                    String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                    Glide.with(SnapActivity.this).load(profileImgUrl).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLikeStatus(String snap_id, ImageButton likeBtn) {
        firestore.collection("snap").document(snap_id).collection("like").document(mAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            likeBtn.setBackgroundResource(R.drawable.ic_heart_filled);
                            likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(SnapActivity.this, R.color.red));
                            likeBtn.setTag("liked");
                        } else {
                            likeBtn.setBackgroundResource(R.drawable.ic_heart_outline);
                            int nightModeFlags =
                                    getApplicationContext().getResources().getConfiguration().uiMode &
                                            Configuration.UI_MODE_NIGHT_MASK;
                            switch (nightModeFlags) {
                                case Configuration.UI_MODE_NIGHT_YES:
                                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(SnapActivity.this, R.color.white));
                                    break;

                                case Configuration.UI_MODE_NIGHT_NO:
                                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(SnapActivity.this, R.color.black));
                                    break;

                                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                                    //no color change
                                    break;
                            }
                            likeBtn.setTag("like");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SnapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        if (isTaskRoot()) {
            startActivity(new Intent(SnapActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            super.finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void setTags(TextView editText, String description) {
        SpannableString string = new SpannableString(description);
        int start = -1;
        for (int i = 0; i < description.length(); i++) {
            if (description.charAt(i) == '#') {
                start = i;
            } else if (description.charAt(i) == ' ' || description.charAt(i) == '\n' || (i == description.length() - 1 && start != -1)) {
                if (start != -1) {
                    if (i == description.length() - 1) {
                        i++; // case for if hash is last word and there is no
                        // space after word
                    }

                    final String tag = description.substring(start, i);
                    string.setSpan(new ClickableSpan() {

                        @Override
                        public void onClick(View widget) {
                            Intent intent = new Intent(SnapActivity.this, HashtagActivity.class);
                            intent.putExtra("tag", tag);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            // link color
                            ds.setColor(Color.parseColor("#FB3958"));
                            ds.setUnderlineText(false);
                        }
                    }, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    start = -1;
                }
            }
        }

        editText.setMovementMethod(LinkMovementMethod.getInstance());
        editText.setText(string);
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date;
        if (Locale.getDefault().getLanguage().equals("ko")) {
            date = DateFormat.format("M월 d일", calendar).toString();
            //date = DateFormat.format("MMM d", calendar).toString();
        } else {
            date = DateFormat.format("MMM d", calendar).toString();
        }
        return date;
    }

}