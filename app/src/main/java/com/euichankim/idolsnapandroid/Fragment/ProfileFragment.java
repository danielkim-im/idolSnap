package com.euichankim.idolsnapandroid.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.EditProfileActivity;
import com.euichankim.idolsnapandroid.Activity.HashtagActivity;
import com.euichankim.idolsnapandroid.Activity.SettingsActivity;
import com.euichankim.idolsnapandroid.Activity.SnapFullScreenImgActivity;
import com.euichankim.idolsnapandroid.Activity.UserFollowerActivity;
import com.euichankim.idolsnapandroid.Activity.UserFollowingActivity;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.Adapter.ProfileTabAdapter;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.R;;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    TabLayout tabLayout;
    public static ViewPager2 viewPager2;
    public static int currentViewPagerPosition;
    ProfileTabAdapter profileTabAdapter;
    public static CircleImageView profileImg;
    public static TextView snapNumTxt, followerNumTxt, followingNumTxt;
    ImageButton moreBtn;
    public static TextView nameTxt, usernameTxt, descTxt;
    public static FirebaseAuth mAuth;
    public static FirebaseFirestore firestore;
    public static Context context;
    public static String profileImgUrl = "default";
    public static User user;
    public static ArrayList<String> tagList;
    public static ConstraintLayout verifiedCons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ProfileFragment.context = getActivity().getApplicationContext();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        profileImg = view.findViewById(R.id.profile_profileImg);
        tagList = new ArrayList<>();
        nameTxt = view.findViewById(R.id.profile_nameTxt);
        usernameTxt = view.findViewById(R.id.profile_usernameTxt);
        descTxt = view.findViewById(R.id.profile_descTxt);
        moreBtn = view.findViewById(R.id.profile_usermoreBtn);
        verifiedCons = view.findViewById(R.id.profileimg_verifiedCons);
        snapNumTxt = view.findViewById(R.id.profile_snapNumTxt);
        followerNumTxt = view.findViewById(R.id.profile_FollowerNumTxt);
        followingNumTxt = view.findViewById(R.id.profile_FollowingNumTxt);
        tabLayout = view.findViewById(R.id.profile_tablayout);
        viewPager2 = view.findViewById(R.id.profile_viewpager);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.snap)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.collection)));

        FragmentManager fragmentManager = getParentFragmentManager();
        profileTabAdapter = new ProfileTabAdapter(fragmentManager, getLifecycle(), mAuth.getCurrentUser().getUid(), "ProfileFragment");
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

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileImgUrl.equals("default") || profileImgUrl == null || profileImgUrl.replaceAll("\\s", "").isEmpty()) {
                    //
                } else {
                    Intent fullScreenIntent = new Intent(getContext(), SnapFullScreenImgActivity.class);
                    fullScreenIntent.putExtra("content_url", profileImgUrl);
                    fullScreenIntent.putStringArrayListExtra("taglist", tagList);
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(), profileImg, ViewCompat.getTransitionName(profileImg));
                    startActivity(fullScreenIntent, optionsCompat.toBundle());
                }
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(view.getContext());
                bottomSheetDialog.setContentView(R.layout.bottomsheet_user_more);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                LinearLayout settingLin, shareLin, linkLin, editLin, hideLin, reportLin;
                settingLin = bottomSheetDialog.findViewById(R.id.user_more_setting);
                shareLin = bottomSheetDialog.findViewById(R.id.user_more_share);
                linkLin = bottomSheetDialog.findViewById(R.id.user_more_link);
                editLin = bottomSheetDialog.findViewById(R.id.user_more_edit);
                hideLin = bottomSheetDialog.findViewById(R.id.user_more_hide);
                reportLin = bottomSheetDialog.findViewById(R.id.user_more_report);

                settingLin.setVisibility(View.VISIBLE);
                shareLin.setVisibility(View.VISIBLE);
                linkLin.setVisibility(View.VISIBLE);
                editLin.setVisibility(View.VISIBLE);
                hideLin.setVisibility(View.GONE);
                reportLin.setVisibility(View.GONE);

                settingLin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        startActivity(new Intent(view.getContext(), SettingsActivity.class));
                        ((Activity) view.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });

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

                editLin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                        startActivity(new Intent(view.getContext(), EditProfileActivity.class));
                        ((Activity) view.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });

                bottomSheetDialog.show();
            }
        });

        followingNumTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(getContext(), UserFollowingActivity.class);
                fullScreenIntent.putExtra("user_id", mAuth.getCurrentUser().getUid());
                startActivity(fullScreenIntent);
                ((Activity) getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        followerNumTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(getContext(), UserFollowerActivity.class);
                fullScreenIntent.putExtra("user_id", mAuth.getCurrentUser().getUid());
                startActivity(fullScreenIntent);
                ((Activity) getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        getUserInfo();
        getUserCount();

        return view;
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
                                copyClipboard(getContext(), shortLink.toString());
                            }
                        } else {
                            // Error
                            // ...
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public static void getUserInfo() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
            }
        });
    }

    public static void getUserCount() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("public_info").document("count")
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
}