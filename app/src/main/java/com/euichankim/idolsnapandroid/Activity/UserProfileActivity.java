package com.euichankim.idolsnapandroid.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Adapter.ProfileTabAdapter;
import com.euichankim.idolsnapandroid.Fragment.ProfileFragment;
import com.euichankim.idolsnapandroid.Model.Snap;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.Model.UserFollowing;
import com.euichankim.idolsnapandroid.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    public static String user_id;
    CardView followBtn;
    ProgressBar followProgressbar;
    ImageButton backBtn, moreBtn, notificationBtn;
    public static CircleImageView profileImg;
    public static TextView nameTxt, usernameTxt, descTxt, snapNumTxt, followerNumTxt, followingNumTxt, followingStatusTxt;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    LinearLayout followBtnBG;
    ProfileTabAdapter profileTabAdapter;
    int currentViewPagerPosition;
    public static FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    public static String profileImgUrl = "default";
    boolean isFollowing = false;
    public static Context context;
    public static User user;
    public static ArrayList<String> tagList;
    public static ConstraintLayout verifiedCons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        UserProfileActivity.context = getApplicationContext();

        user_id = getIntent().getStringExtra("user_id");
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        tagList = new ArrayList<>();
        followProgressbar = findViewById(R.id.profile_followprogress);
        backBtn = findViewById(R.id.profile_backBtn);
        moreBtn = findViewById(R.id.profile_usermoreBtn);
        followBtn = findViewById(R.id.profile_followBtn);
        notificationBtn = findViewById(R.id.profile_notificationBtn);
        followBtnBG = findViewById(R.id.profile_followbg);
        verifiedCons = findViewById(R.id.profileimg_verifiedCons);
        verifiedCons.setVisibility(View.GONE);
        followingStatusTxt = findViewById(R.id.profile_followingStatusTxt);
        profileImg = findViewById(R.id.profile_profileImg);
        nameTxt = findViewById(R.id.profile_nameTxt);
        usernameTxt = findViewById(R.id.profile_usernameTxt);
        descTxt = findViewById(R.id.profile_descTxt);
        snapNumTxt = findViewById(R.id.profile_snapNumTxt);
        followerNumTxt = findViewById(R.id.profile_FollowerNumTxt);
        followingNumTxt = findViewById(R.id.profile_FollowingNumTxt);
        tabLayout = findViewById(R.id.profile_tablayout);
        viewPager2 = findViewById(R.id.profile_viewpager);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.snap)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.collection)));

        if (mAuth.getCurrentUser().getUid().equals(user_id)) {
            followBtn.setVisibility(View.GONE);
            followProgressbar.setVisibility(View.GONE);
        } else {
            followBtn.setVisibility(View.VISIBLE);
            followProgressbar.setVisibility(View.GONE);
            checkFollowingStatus();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        profileTabAdapter = new ProfileTabAdapter(fragmentManager, getLifecycle(), user_id, "UserProfileActivity");
        viewPager2.setAdapter(profileTabAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
                currentViewPagerPosition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileImgUrl.equals("default") || profileImgUrl == null || profileImgUrl.replaceAll("\\s", "").isEmpty()) {
                    //
                } else {
                    Intent fullScreenIntent = new Intent(UserProfileActivity.this, SnapFullScreenImgActivity.class);
                    fullScreenIntent.putExtra("content_url", profileImgUrl);
                    fullScreenIntent.putStringArrayListExtra("taglist", tagList);
                    startActivity(fullScreenIntent);
                }
            }
        });

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Permissions.check(UserProfileActivity.this, Manifest.permission.POST_NOTIFICATIONS, null, new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            followBtnAction();
                        }

                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            super.onDenied(context, deniedPermissions);
                            Toast.makeText(UserProfileActivity.this, getString(R.string.permissionfornotificationsrequired), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                } else {
                    followBtnAction();
                }
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreBottomSheet();
            }
        });

        followingNumTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(UserProfileActivity.this, UserFollowingActivity.class);
                fullScreenIntent.putExtra("user_id", user_id);
                startActivity(fullScreenIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        followerNumTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(UserProfileActivity.this, UserFollowerActivity.class);
                fullScreenIntent.putExtra("user_id", user_id);
                startActivity(fullScreenIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        getProfileInfo();
        getProfileCount();
    }

    private void followBtnAction() {
        if (isFollowing && mAuth.getCurrentUser().getUid() != user_id) {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(UserProfileActivity.this);
            bottomSheetDialog.setContentView(R.layout.bottomsheet_follow);
            bottomSheetDialog.setCanceledOnTouchOutside(true);

            LinearLayout notiOnLin, notiOffLin, unfollowLin;
            ImageView notiOnCheck, notiOffCheck;
            notiOnLin = bottomSheetDialog.findViewById(R.id.bf_notification_on);
            notiOffLin = bottomSheetDialog.findViewById(R.id.bf_notification_off);
            unfollowLin = bottomSheetDialog.findViewById(R.id.bf_unfollow);
            notiOnCheck = bottomSheetDialog.findViewById(R.id.bf_noti_on_check);
            notiOffCheck = bottomSheetDialog.findViewById(R.id.bf_noti_off_check);

            firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.getBoolean("receive_notification") != null) {
                            if (documentSnapshot.getBoolean("receive_notification") == true) {
                                notiOnCheck.setVisibility(View.VISIBLE);
                                notiOffCheck.setVisibility(View.GONE);
                            } else if (documentSnapshot.getBoolean("receive_notification") == false) {
                                notiOnCheck.setVisibility(View.GONE);
                                notiOffCheck.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            notiOnLin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("receive_notification", true);
                    firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                            .collection("following").document(user_id).update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    notificationBtn.setImageResource(R.drawable.ic_notification);
                                    bottomSheetDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            notiOffLin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("receive_notification", false);
                    firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                            .collection("following").document(user_id).update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    notificationBtn.setImageResource(R.drawable.ic_notification_off);
                                    bottomSheetDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            unfollowLin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followBtn.setVisibility(View.INVISIBLE);
                    followProgressbar.setVisibility(View.VISIBLE);
                    //remove follow
                    firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following")
                            .document(user_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    checkFollowingStatus();
                                    bottomSheetDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    followBtn.setVisibility(View.VISIBLE);
                                    followProgressbar.setVisibility(View.GONE);
                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
            bottomSheetDialog.show();
        } else if (mAuth.getCurrentUser().getUid() != user_id) {
            followBtn.setVisibility(View.INVISIBLE);
            followProgressbar.setVisibility(View.VISIBLE);
            UserFollowing userFollowing = new UserFollowing(mAuth.getCurrentUser().getUid(), user_id, FieldValue.serverTimestamp(), true);
            firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following")
                    .document(user_id).set(userFollowing).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkFollowingStatus();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AggregateQuery countQuery = firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following").count();
                            countQuery.get(AggregateSource.SERVER).addOnSuccessListener(new OnSuccessListener<AggregateQuerySnapshot>() {
                                @Override
                                public void onSuccess(AggregateQuerySnapshot aggregateQuerySnapshot) {
                                    long following_count = aggregateQuerySnapshot.getCount();
                                    if (following_count >= 250) {
                                        Toast.makeText(UserProfileActivity.this, getString(R.string.followupto250), Toast.LENGTH_SHORT).show();
                                    }
                                    followBtn.setVisibility(View.VISIBLE);
                                    followProgressbar.setVisibility(View.GONE);
                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception ea) {
                                    followBtn.setVisibility(View.VISIBLE);
                                    followProgressbar.setVisibility(View.GONE);
                                    Toast.makeText(UserProfileActivity.this, ea.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        }
    }

    private void showMoreBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(UserProfileActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_user_more);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        LinearLayout settingLin, shareLin, linkLin, editLin, hideLin, reportLin;
        settingLin = bottomSheetDialog.findViewById(R.id.user_more_setting);
        shareLin = bottomSheetDialog.findViewById(R.id.user_more_share);
        linkLin = bottomSheetDialog.findViewById(R.id.user_more_link);
        editLin = bottomSheetDialog.findViewById(R.id.user_more_edit);
        hideLin = bottomSheetDialog.findViewById(R.id.user_more_hide);
        reportLin = bottomSheetDialog.findViewById(R.id.user_more_report);

        settingLin.setVisibility(View.GONE);
        shareLin.setVisibility(View.VISIBLE);
        linkLin.setVisibility(View.VISIBLE);
        editLin.setVisibility(View.GONE);
        if (user_id.equals(mAuth.getCurrentUser().getUid())) {
            hideLin.setVisibility(View.GONE);
            reportLin.setVisibility(View.GONE);
        } else {
            hideLin.setVisibility(View.VISIBLE);
            reportLin.setVisibility(View.VISIBLE);
        }

        shareLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareUser(user, "share");
                bottomSheetDialog.dismiss();
            }
        });

        linkLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareUser(user, "copylink");
                bottomSheetDialog.dismiss();
            }
        });

        hideLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing == true) {
                    bottomSheetDialog.dismiss();
                    Toast.makeText(UserProfileActivity.this, getString(R.string.unfollowbeforehide), Toast.LENGTH_SHORT).show();
                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("type", "user");
                    hashMap.put("user_id", user_id);
                    hashMap.put("hidden_since", FieldValue.serverTimestamp());
                    firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                            .collection("hide").document(user_id).set(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    bottomSheetDialog.dismiss();
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.userhiddencomplete), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        reportLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("reporter_id", mAuth.getCurrentUser().getUid());
                hashMap.put("reported_at", FieldValue.serverTimestamp());
                firestore.collection("report_user").document(user_id)
                        .collection("reporter")
                        .document(mAuth.getCurrentUser().getUid()).set(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(UserProfileActivity.this, getString(R.string.reportsent), Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                firestore.collection("report_user").document(user_id)
                                        .collection("reporter")
                                        .document(mAuth.getCurrentUser().getUid())
                                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    Toast.makeText(UserProfileActivity.this, getString(R.string.alreadyreported), Toast.LENGTH_SHORT).show();
                                                    bottomSheetDialog.dismiss();
                                                } else {
                                                    Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });

        bottomSheetDialog.show();
    }

    private void shareUser(User user, String mode) {
        String title;
        if (Locale.getDefault().getLanguage().equals("ko")) {
            title = "IdolSnap의 " + user.getName() + "(@" + user.getName() + ") 님";
        } else {
            title = user.getName() + "(@" + user.getUsername() + ") on IdolSnap";
        }
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.idolsnap.net?user_id=" + user.getUser_id()))
                .setDomainUriPrefix("https://idolsnapapp.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(title).setImageUrl(Uri.parse(profileImgUrl)).build())
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
                                copyClipboard(UserProfileActivity.this, shortLink.toString());
                            }
                        } else {
                            // Error
                            // ...
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void checkFollowingStatus() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following")
                .document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // user is following
                            followBtn.setVisibility(View.VISIBLE);
                            followProgressbar.setVisibility(View.GONE);
                            isFollowing = true;
                            followingStatusTxt.setText(getString(R.string.following));
                            followingStatusTxt.setTextColor(ContextCompat.getColor(context, R.color.red));
                            followBtnBG.setBackground(getDrawable(R.drawable.background_followbtn));
                            notificationBtn.setVisibility(View.VISIBLE);
                            if (documentSnapshot.getBoolean("receive_notification") != null) {
                                if (documentSnapshot.getBoolean("receive_notification") == true) {
                                    notificationBtn.setImageResource(R.drawable.ic_notification);
                                } else if (documentSnapshot.getBoolean("receive_notification") == false) {
                                    notificationBtn.setImageResource(R.drawable.ic_notification_off);
                                }
                            }
                        } else {
                            // user is not following
                            followBtn.setVisibility(View.VISIBLE);
                            followProgressbar.setVisibility(View.GONE);
                            isFollowing = false;
                            followingStatusTxt.setText(getString(R.string.follow));
                            followingStatusTxt.setTextColor(ContextCompat.getColor(context, R.color.white));
                            followBtnBG.setBackground(getDrawable(R.color.red));
                            notificationBtn.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        followBtn.setVisibility(View.VISIBLE);
                        followProgressbar.setVisibility(View.GONE);
                        Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void getProfileInfo() {
        firestore.collection("user").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.get("profile_img_id").equals("default") || documentSnapshot.get("profile_img_id") == null || documentSnapshot.get("profile_img_id").toString().replaceAll("\\s", "").isEmpty()) {
                    Glide.with(context).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                    profileImgUrl = "default";
                } else {
                    String profile_img_id = documentSnapshot.getString("profile_img_id");
                    String user_id = documentSnapshot.getString("user_id");
                    profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_480x480";
                    Glide.with(context).load(profileImgUrl).override(480, 480).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                }
                nameTxt.setText(documentSnapshot.get("name").toString());
                usernameTxt.setText("@" + documentSnapshot.get("username").toString());
                if (documentSnapshot.get("description").toString().replaceAll("\\s", "").isEmpty()) {
                    descTxt.setVisibility(View.GONE);
                } else {
                    descTxt.setVisibility(View.VISIBLE);
                    setTags(descTxt, documentSnapshot.getString("description"));
                }
                tagList.addAll((List<String>) documentSnapshot.get("keyword"));
                user = documentSnapshot.toObject(User.class);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                snapNumTxt.setText("0" + "\n" + context.getString(R.string.snap));
            }
        });
    }

    public static void getProfileCount() {
        firestore.collection("user").document(user_id).collection("public_info").document("count")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.get("num_snap") != null) {
                            long snap_count = (long) documentSnapshot.get("num_snap");
                            if (snap_count <= 1) {
                                snapNumTxt.setText(snap_count + "\n" + context.getString(R.string.snap));
                            } else {
                                snapNumTxt.setText(snap_count + "\n" + context.getString(R.string.snaps));
                            }
                        } else {
                            snapNumTxt.setText("0" + "\n" + context.getString(R.string.snap));
                        }
                        if (documentSnapshot.get("num_follower") != null) {
                            long num_follower = (long) documentSnapshot.get("num_follower");
                            followerNumTxt.setText(num_follower + "\n" + context.getString(R.string.followers));
                        }
                        if (documentSnapshot.get("num_following") != null) {
                            long num_following = (long) documentSnapshot.get("num_following");
                            followingNumTxt.setText(num_following + "\n" + context.getString(R.string.following));
                        }
                        if (documentSnapshot.get("verified") != null) {
                            if (documentSnapshot.getBoolean("verified") == true) {
                                verifiedCons.setVisibility(View.VISIBLE);
                            } else {
                                verifiedCons.setVisibility(View.GONE);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        snapNumTxt.setText("-" + "\n" + context.getString(R.string.snap));
                        followerNumTxt.setText("-" + "\n" + context.getString(R.string.followers));
                        followingNumTxt.setText("-" + "\n" + context.getString(R.string.following));
                    }
                });
    }

    public static void setTags(TextView editText, String description) {
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
                            Intent intent = new Intent(widget.getContext(), HashtagActivity.class);
                            intent.putExtra("tag", tag);
                            widget.getContext().startActivity(intent);
                            ((Activity) widget.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    @Override
    public void finish() {
        if (isTaskRoot()) {
            startActivity(new Intent(UserProfileActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            super.finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}