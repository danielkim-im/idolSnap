package com.euichankim.idolsnapandroid.ViewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.SnapCommentActivity;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.Model.Comment;
import com.euichankim.idolsnapandroid.Model.Like;
import com.euichankim.idolsnapandroid.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SnapComment_ViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    TextView usernameTxt, commentTxt, likeCountTxt;
    CircleImageView profileImg;
    ImageButton moreBtn, likeBtn;
    ImageView verifiedIcon;
    long snap_like_count;

    public SnapComment_ViewHolder(@NonNull View itemView) {
        super(itemView);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        mContext = SnapCommentActivity.mContext;
        usernameTxt = itemView.findViewById(R.id.commentitem_username);
        commentTxt = itemView.findViewById(R.id.comment_commentTxt);
        likeCountTxt = itemView.findViewById(R.id.comment_likeCountTxt);
        profileImg = itemView.findViewById(R.id.commentitem_profileImg);
        moreBtn = itemView.findViewById(R.id.comment_moreBtn);
        likeBtn = itemView.findViewById(R.id.comment_likeBtn);
        verifiedIcon = itemView.findViewById(R.id.commentitem_verifiedIcon);
        verifiedIcon.setVisibility(View.GONE);
    }

    public void bind(Comment comment, String snap_id, String snap_author_id) {
        getAuthorInfo(usernameTxt, profileImg, comment.getAuthor_id());
        commentTxt.setText(comment.getComment_text());
        getLikeCount(likeCountTxt, comment.getSnap_id(), comment.getComment_id());
        getLikeStatus(comment.getSnap_id(), likeBtn, comment.getComment_id());
        checkVerificationStatus(comment.getAuthor_id(), verifiedIcon);

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) SnapCommentActivity.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((Activity) SnapCommentActivity.mContext).getWindow().getDecorView().getRootView().getWindowToken(), 0);
                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                userIntent.putExtra("user_id", comment.getAuthor_id());
                itemView.getContext().startActivity(userIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        usernameTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) SnapCommentActivity.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((Activity) SnapCommentActivity.mContext).getWindow().getDecorView().getRootView().getWindowToken(), 0);
                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                userIntent.putExtra("user_id", comment.getAuthor_id());
                itemView.getContext().startActivity(userIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Like like = new Like(mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp());
                if (likeBtn.getTag().equals("like")) {
                    likeBtn.setBackgroundResource(R.drawable.ic_heart_filled);
                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.red));
                    likeBtn.setTag("liked");
                    snap_like_count = snap_like_count + 1;
                    likeCountTxt.setText(snap_like_count + "");
                    firestore.collection("snap").document(comment.snap_id).collection("comment")
                            .document(comment.getComment_id()).collection("like")
                            .document(mAuth.getCurrentUser().getUid()).set(like).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    getLikeStatus(comment.getSnap_id(), likeBtn, comment.getComment_id());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    likeBtn.setBackgroundResource(R.drawable.ic_heart_outline);
                    int nightModeFlags =
                            mContext.getApplicationContext().getResources().getConfiguration().uiMode &
                                    Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.white));
                            break;

                        case Configuration.UI_MODE_NIGHT_NO:
                            likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.black));
                            break;

                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            //no color change
                            break;
                    }
                    likeBtn.setTag("like");
                    snap_like_count = snap_like_count - 1;
                    likeCountTxt.setText(snap_like_count + "");
                    firestore.collection("snap").document(comment.snap_id).collection("comment")
                            .document(comment.getComment_id()).collection("like").document(mAuth.getCurrentUser().getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    getLikeStatus(comment.getSnap_id(), likeBtn, comment.getComment_id());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) SnapCommentActivity.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((Activity) SnapCommentActivity.mContext).getWindow().getDecorView().getRootView().getWindowToken(), 0);
                BottomSheetDialog bottomSheetDialog;
                bottomSheetDialog = new BottomSheetDialog(SnapCommentActivity.mContext);
                bottomSheetDialog.setContentView(R.layout.bottomdialog_comment);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                bottomSheetDialog.show();

                LinearLayout deleteCommentLin, reportLin;
                deleteCommentLin = bottomSheetDialog.findViewById(R.id.comment_bottomdialog_deleteComment);
                reportLin = bottomSheetDialog.findViewById(R.id.comment_bottomdialog_report);

                if (comment.getAuthor_id().equals(mAuth.getCurrentUser().getUid())) {
                    deleteCommentLin.setVisibility(View.VISIBLE);
                    reportLin.setVisibility(View.GONE);
                } else if (snap_author_id.equals(mAuth.getCurrentUser().getUid())) {
                    deleteCommentLin.setVisibility(View.VISIBLE);
                    reportLin.setVisibility(View.VISIBLE);
                } else {
                    deleteCommentLin.setVisibility(View.GONE);
                    reportLin.setVisibility(View.VISIBLE);
                }

                deleteCommentLin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        firestore.collection("snap")
                                .document(comment.getSnap_id())
                                .collection("comment")
                                .document(comment.getComment_id())
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        SnapCommentActivity.updateComment();
                                        bottomSheetDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                reportLin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("reporter_id", mAuth.getCurrentUser().getUid());
                        hashMap.put("reported_at", FieldValue.serverTimestamp());
                        hashMap.put("snap_id", snap_id);
                        firestore.collection("report_comment").document(comment.getComment_id())
                                .collection("reporter")
                                .document(mAuth.getCurrentUser().getUid()).set(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.reportsent), Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        firestore.collection("report_comment").document(comment.getComment_id())
                                                .collection("reporter")
                                                .document(mAuth.getCurrentUser().getUid())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        if (documentSnapshot.exists()) {
                                                            Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.alreadyreported), Toast.LENGTH_SHORT).show();
                                                            bottomSheetDialog.dismiss();
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

            }
        });
    }

    private void checkVerificationStatus(String user_id, ImageView icon) {
        firestore.collection("user").document(user_id).collection("public_info").document("count")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            if (documentSnapshot.getBoolean("verified") == true) {
                                icon.setVisibility(View.VISIBLE);
                            } else {
                                icon.setVisibility(View.GONE);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        icon.setVisibility(View.GONE);
                    }
                });
    }

    private void getLikeCount(TextView countTxt, String snap_id, String comment_id) {
        firestore.collection("snap").document(snap_id).collection("comment").document(comment_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.get("like_count") != null) {
                            snap_like_count = (long) documentSnapshot.get("like_count");
                            countTxt.setText(snap_like_count + "");
                        } else {
                            countTxt.setText("0");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        countTxt.setText("-");
                    }
                });
    }

    private void getAuthorInfo(TextView usernameTxt, CircleImageView profileImg, String author_id) {
        firestore.collection("user").document(author_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    usernameTxt.setText(documentSnapshot.get("username").toString());
                    if (documentSnapshot.get("profile_img_id").equals("default")) {
                        Glide.with(mContext).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                    } else {
                        String profile_img_id = documentSnapshot.getString("profile_img_id");
                        String user_id = documentSnapshot.getString("user_id");
                        String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                        Glide.with(mContext).load(profileImgUrl).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
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

    private void getLikeStatus(String snap_id, ImageButton likeBtn, String comment_id) {
        firestore.collection("snap").document(snap_id).collection("comment").document(comment_id).collection("like")
                .document(mAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            likeBtn.setBackgroundResource(R.drawable.ic_heart_filled);
                            likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.red));
                            likeBtn.setTag("liked");
                        } else {
                            likeBtn.setBackgroundResource(R.drawable.ic_heart_outline);
                            int nightModeFlags =
                                    mContext.getApplicationContext().getResources().getConfiguration().uiMode &
                                            Configuration.UI_MODE_NIGHT_MASK;
                            switch (nightModeFlags) {
                                case Configuration.UI_MODE_NIGHT_YES:
                                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.white));
                                    break;

                                case Configuration.UI_MODE_NIGHT_NO:
                                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.black));
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
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
