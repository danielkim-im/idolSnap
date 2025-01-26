package com.euichankim.idolsnapandroid.ViewHolder;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.SnapCommentActivity;
import com.euichankim.idolsnapandroid.Activity.EditSnapActivity;
import com.euichankim.idolsnapandroid.Activity.HashtagActivity;
import com.euichankim.idolsnapandroid.Activity.MainActivity;
import com.euichankim.idolsnapandroid.Activity.SnapFullScreenImgActivity;
import com.euichankim.idolsnapandroid.Activity.SnapLikeActivity;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
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
import com.google.firebase.storage.FirebaseStorage;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;

public class Home_SnapLargeViewHolder extends RecyclerView.ViewHolder {

    TextView usernameTxt, likeTxt, dateTxt, unhideSnapTxt;
    CircleImageView profileImage;
    ImageView verifiedIcon;
    ReadMoreTextView descTxt;
    PhotoView imageView;
    ImageButton moreBtn, likeBtn, commentBtn, shareBtn, saveBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseStorage firebaseStorage;
    public static BottomSheetDialog collectionBottomSheet;
    public static ImageButton bscloseBtn, bsCreateBtn;
    public static RecyclerView bsrecyclerView;
    public static ProgressBar bsProgressbar;
    long snap_like_count;
    String author_name;
    ConstraintLayout blurCons;
    ArrayList<String> tagList;

    public Home_SnapLargeViewHolder(@NonNull View itemView) {
        super(itemView);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        usernameTxt = itemView.findViewById(R.id.snap_username);
        descTxt = itemView.findViewById(R.id.snap_description);
        tagList = new ArrayList<>();
        dateTxt = itemView.findViewById(R.id.snap_dateTxt);
        imageView = itemView.findViewById(R.id.snap_imageview);
        likeTxt = itemView.findViewById(R.id.snap_likeTxt);
        profileImage = itemView.findViewById(R.id.snap_profilePic);
        moreBtn = itemView.findViewById(R.id.snap_moreBtn);
        likeBtn = itemView.findViewById(R.id.snap_likeBtn);
        commentBtn = itemView.findViewById(R.id.snap_commentBtn);
        shareBtn = itemView.findViewById(R.id.snap_shareBtn);
        saveBtn = itemView.findViewById(R.id.snap_saveBtn);
        blurCons = itemView.findViewById(R.id.snap_blurCons);
        unhideSnapTxt = itemView.findViewById(R.id.snap_unhideTxt);
        verifiedIcon = itemView.findViewById(R.id.snap_verifiedicon);
        verifiedIcon.setVisibility(View.GONE);
    }

    public void bind(Snap snap) {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                .collection("hide").document(snap.getSnap_id()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            loadSnap(snap, true);
                            blurCons.setVisibility(View.VISIBLE);
                        } else {
                            loadSnap(snap, false);
                            blurCons.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSnap(Snap snap, boolean isHidden) {
        if (isHidden == true) {
            likeTxt.setText("-");
            descTxt.setText(itemView.getContext().getString(R.string.hiddensnap));
            dateTxt.setVisibility(View.GONE);
        } else if (isHidden == false) {
            getLikeCount(likeTxt, snap.getSnap_id());
            getLikeStatus(snap.getSnap_id(), likeBtn);

            setTags(descTxt, snap.getDescription());
            if (snap.getSource() == null || snap.getSource().isEmpty()) {
                dateTxt.setText(timestampToString(((Timestamp) snap.getCreated_at()).getSeconds() * 1000));
            } else {
                dateTxt.setText(timestampToString(((Timestamp) snap.getCreated_at()).getSeconds() * 1000) + " · " + snap.getSource());
            }
        }

        getAuthorInfo(profileImage, usernameTxt, snap.getAuthor_id());
        checkIfVerified(snap.getAuthor_id());

        String size = "_1080x1080";
        if (isHidden == true) {
            size = "_512x512";
        } else {
            size = "_1080x1080";
        }
        String content_url = "https://cdn.idolsnap.net/snap/" + snap.getAuthor_id() + "/" + snap.getContent_id() + "/" + snap.getContent_id() + size;
        Glide.with(itemView)
                .load(content_url)
                .override(1080, 1080)
                .placeholder(R.drawable.ic_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                //.error(R.drawable.ic_placeholder)
                .into(imageView);

        tagList.addAll(snap.getTag());

        likeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(itemView.getContext(), SnapLikeActivity.class);
                userIntent.putExtra("snap_id", snap.getSnap_id());
                itemView.getContext().startActivity(userIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                userIntent.putExtra("user_id", snap.getAuthor_id());
                itemView.getContext().startActivity(userIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        usernameTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                userIntent.putExtra("user_id", snap.getAuthor_id());
                itemView.getContext().startActivity(userIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHidden == false) {
                    Like like = new Like(mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp());

                    if (likeBtn.getTag().equals("like")) {
                        likeBtn.setBackgroundResource(R.drawable.ic_heart_filled);
                        likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.red));
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
                        firestore.collection("snap").document(snap.snap_id).collection("like")
                                .document(mAuth.getCurrentUser().getUid()).set(like).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        getLikeStatus(snap.getSnap_id(), likeBtn);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        likeBtn.setBackgroundResource(R.drawable.ic_heart_outline);
                        int nightModeFlags =
                                itemView.getContext().getApplicationContext().getResources().getConfiguration().uiMode &
                                        Configuration.UI_MODE_NIGHT_MASK;
                        switch (nightModeFlags) {
                            case Configuration.UI_MODE_NIGHT_YES:
                                likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.white));
                                break;

                            case Configuration.UI_MODE_NIGHT_NO:
                                likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.black));
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
                        firestore.collection("snap").document(snap.snap_id).collection("like").document(mAuth.getCurrentUser().getUid())
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        getLikeStatus(snap.getSnap_id(), likeBtn);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
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

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHidden == false) {
                    Intent commentIntent = new Intent(itemView.getContext(), SnapCommentActivity.class);
                    commentIntent.putExtra("snap_id", snap.getSnap_id());
                    itemView.getContext().startActivity(commentIntent);
                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        imageView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (isHidden == false) {
                    String content_url = "https://cdn.idolsnap.net/snap/" + snap.getAuthor_id() + "/" + snap.getContent_id() + "/" + snap.getContent_id() + "_1080x1080";
                    Intent fullScreenIntent = new Intent(itemView.getContext(), SnapFullScreenImgActivity.class);
                    fullScreenIntent.putExtra("content_url", content_url);
                    fullScreenIntent.putStringArrayListExtra("taglist", tagList);
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) itemView.getContext(), imageView, ViewCompat.getTransitionName(imageView));
                    itemView.getContext().startActivity(fullScreenIntent, optionsCompat.toBundle());
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                if (isHidden == false) {
                    Like like = new Like(mAuth.getCurrentUser().getUid(), FieldValue.serverTimestamp());
                    if (likeBtn.getTag().equals("like")) {
                        likeBtn.setBackgroundResource(R.drawable.ic_heart_filled);
                        likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.red));
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
                        LottieAnimationView lottieAnimationView = itemView.findViewById(R.id.snap_lottie);
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
                        firestore.collection("snap").document(snap.snap_id).collection("like")
                                .document(mAuth.getCurrentUser().getUid()).set(like).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        getLikeStatus(snap.getSnap_id(), likeBtn);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHidden == false) {
                    shareSnap(snap, "share");
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHidden == false) {
                    showCollection(snap.getSnap_id());
                }
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
    }

    private void checkIfVerified(String author_id) {
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
                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareSnap(Snap snap, String mode) {
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
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        if (Locale.getDefault().getLanguage().equals("ko")) {
                            likeTxt.setText("좋아요 -개");
                        } else {
                            likeTxt.setText("- Like");
                        }
                    }
                });
    }

    private void setTags(ReadMoreTextView editText, String description) {
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
                            Intent intent = new Intent(itemView.getContext(), HashtagActivity.class);
                            intent.putExtra("tag", tag);
                            itemView.getContext().startActivity(intent);
                            ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
                        if (HomeFragment.mContext != null) {
                            HomeFragment.updateSnap(HomeFragment.mContext);
                        } else {
                            itemView.getContext().startActivity(new Intent(itemView.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            ((Activity) itemView.getContext()).finish();
                            ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
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

    private void getAuthorInfo(CircleImageView profileImg, TextView usernameTxt, String user_id) {
        firestore.collection("user").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                usernameTxt.setText(documentSnapshot.get("username").toString());
                author_name = documentSnapshot.get("name").toString();
                if (documentSnapshot.get("profile_img_id").equals("default")) {
                    Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                } else {
                    String profile_img_id = documentSnapshot.getString("profile_img_id");
                    String user_id = documentSnapshot.getString("user_id");
                    String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                    Glide.with(itemView).load(profileImgUrl).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.red));
                            likeBtn.setTag("liked");
                        } else {
                            likeBtn.setBackgroundResource(R.drawable.ic_heart_outline);
                            int nightModeFlags =
                                    itemView.getContext().getApplicationContext().getResources().getConfiguration().uiMode &
                                            Configuration.UI_MODE_NIGHT_MASK;
                            switch (nightModeFlags) {
                                case Configuration.UI_MODE_NIGHT_YES:
                                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.white));
                                    break;

                                case Configuration.UI_MODE_NIGHT_NO:
                                    likeBtn.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.black));
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
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
