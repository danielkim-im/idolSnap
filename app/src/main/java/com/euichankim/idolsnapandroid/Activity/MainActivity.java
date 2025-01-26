package com.euichankim.idolsnapandroid.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.euichankim.idolsnapandroid.Fragment.HomeFragment;
import com.euichankim.idolsnapandroid.Fragment.NotificationFragment;
import com.euichankim.idolsnapandroid.Fragment.ProfileFragment;
import com.euichankim.idolsnapandroid.Fragment.ProfileSnapFragment;
import com.euichankim.idolsnapandroid.Model.Collection;
import com.euichankim.idolsnapandroid.Model.UserInterestedTag;
import com.euichankim.idolsnapandroid.R;;
import com.euichankim.idolsnapandroid.Fragment.DiscoverFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;
    public static FragmentManager fragmentManager;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    boolean isConnected;
    ReviewManager reviewManager;
    ReviewInfo reviewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        frameLayout = findViewById(R.id.main_framelayout);
        bottomNavigationView = findViewById(R.id.main_bottomnav);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag("home") != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("home")).commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fragmentManager.beginTransaction().add(R.id.main_framelayout, new HomeFragment(), "home").commit();
        }
        if (fragmentManager.findFragmentByTag("search") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("search")).commit();
        }
        if (fragmentManager.findFragmentByTag("notification") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("notification")).commit();
        }
        if (fragmentManager.findFragmentByTag("profile") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("profile")).commit();
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        checkUserBias();
        uploadFCMToken();
        countAppOpen();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Permissions.check(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    //followBtnAction();
                }

                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    super.onDenied(context, deniedPermissions);
                    /*Toast.makeText(MainActivity.this, getString(R.string.permissionfornotificationsrequired), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);*/
                }
            });
        } else {
            //followBtnAction();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
        checkServerStatus();
    }

    private void checkUserAdConsent() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("private_info")
                .document("applovin_consent").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getBoolean("has_consent") == true) {
                                AppLovinPrivacySettings.setHasUserConsent(true, MainActivity.this);
                            } else {
                                AppLovinPrivacySettings.setHasUserConsent(false, MainActivity.this);
                            }
                        } else {
                            AppLovinPrivacySettings.setHasUserConsent(false, MainActivity.this);
                            askApplovinConsent();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        AppLovinPrivacySettings.setHasUserConsent(false, MainActivity.this);
                    }
                });
    }

    private void askApplovinConsent() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_applovin_consent);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);

        CardView agree;
        TextView donotagree;
        ProgressBar progressBar;
        agree = bottomSheetDialog.findViewById(R.id.consent_agreeCardview);
        donotagree = bottomSheetDialog.findViewById(R.id.consent_donotagree);
        progressBar = bottomSheetDialog.findViewById(R.id.consent_progressbar);

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree.setVisibility(View.GONE);
                donotagree.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("has_consent", true);
                hashMap.put("last_updated_at", FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("private_info")
                        .document("applovin_consent").set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                AppLovinPrivacySettings.setHasUserConsent(true, MainActivity.this);
                                bottomSheetDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AppLovinPrivacySettings.setHasUserConsent(false, MainActivity.this);
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        });
            }
        });

        donotagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree.setVisibility(View.GONE);
                donotagree.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("has_consent", false);
                hashMap.put("last_updated_at", FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("private_info")
                        .document("applovin_consent").set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                AppLovinPrivacySettings.setHasUserConsent(false, MainActivity.this);
                                bottomSheetDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                AppLovinPrivacySettings.setHasUserConsent(false, MainActivity.this);
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        });
            }
        });

        bottomSheetDialog.show();
    }

    private void checkServerStatus() {
        firestore.collection("maintenance").document("server_status")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getBoolean("in_service") == false) {
                            // Server under maintenance
                            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                            bottomSheetDialog.setContentView(R.layout.bottomsheet_serverdown);
                            bottomSheetDialog.setCanceledOnTouchOutside(false);
                            bottomSheetDialog.setCancelable(false);
                            TextView descTxt, refresthTxt, closeTxt;
                            ImageView twitterImg, instagramImg;
                            ProgressBar progressBar;

                            descTxt = bottomSheetDialog.findViewById(R.id.sd_desc);
                            refresthTxt = bottomSheetDialog.findViewById(R.id.sd_refreshtxt);
                            closeTxt = bottomSheetDialog.findViewById(R.id.sd_closetxt);
                            twitterImg = bottomSheetDialog.findViewById(R.id.sd_twitterlogo);
                            instagramImg = bottomSheetDialog.findViewById(R.id.sd_instagramlogo);
                            progressBar = bottomSheetDialog.findViewById(R.id.sd_progressbar);
                            progressBar.setVisibility(View.GONE);

                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                descTxt.setText(documentSnapshot.getString("message_kr"));
                            } else {
                                descTxt.setText(documentSnapshot.getString("message_eng"));
                            }

                            refresthTxt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    refresthTxt.setText("");
                                    progressBar.setVisibility(View.VISIBLE);
                                    firestore.collection("maintenance").document("server_status").get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.getBoolean("in_service") == true) {
                                                        bottomSheetDialog.dismiss();
                                                    } else {
                                                        refresthTxt.setText(getString(R.string.refresh));
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });

                            closeTxt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });

                            twitterImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://twitter.com/IdolSnapApp"));
                                    startActivity(i);
                                }
                            });

                            instagramImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://www.instagram.com/idolsnap/"));
                                    startActivity(i);
                                }
                            });

                            bottomSheetDialog.show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("token", s);
                map.put("updated_at", FieldValue.serverTimestamp());
                firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                        .collection("private_info").document("fcm_token")
                        .set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Updated FCM Token to Firestore DB
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void checkUserBias() {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("interested_tag")
                .orderBy("last_interacted", Query.Direction.DESCENDING).limit(1)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // User Interested Tag does not exist
                            startActivity(new Intent(MainActivity.this, InterestedTagActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } else {
                            //checkDynamicLink();
                            checkUserAdConsent();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserStatus() {
        checkConnection();
        if (isConnected) {
            if (mAuth.getCurrentUser() != null) {
                mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                            // No problem
                        } else if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified() == false) {
                            //Toast.makeText(IntroActivity.this, "VERIFY EMAIL", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, EmailVerificationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } else if (mAuth.getCurrentUser() == null) {
                            Toast.makeText(MainActivity.this, getString(R.string.main_failedtoauthenticateuser), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, AuthActivity.class));
                        finish();
                    }
                });
            } else {
                //user has not signed in
                startActivity(new Intent(MainActivity.this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        } else {
            // Do not check user status if not connected
        }
    }

    public void checkConnection() {
        ConnectivityManager manager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                isConnected = true;
            }

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                isConnected = true;
            }
        } else {
            isConnected = false;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //Fragment selectedFragment = null;


                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            if (fragmentManager.findFragmentByTag("home") != null) {
                                //if the fragment exists, show it.
                                HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
                                if (homeFragment != null && homeFragment.isVisible()) {
                                    HomeFragment.recyclerView.smoothScrollToPosition(0);
                                }
                                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("home")).commit();
                            } else {
                                //if the fragment does not exist, add it to fragment manager.
                                fragmentManager.beginTransaction().add(R.id.main_framelayout, new HomeFragment(), "home").commit();
                            }
                            if (fragmentManager.findFragmentByTag("search") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("search")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("notification") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("notification")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("profile") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("profile")).commit();
                            }
                            break;
                        case R.id.nav_discover:
                            if (fragmentManager.findFragmentByTag("search") != null) {
                                //if the fragment exists, show it.
                                DiscoverFragment discoverFragment = (DiscoverFragment) getSupportFragmentManager().findFragmentByTag("search");
                                if (discoverFragment != null && discoverFragment.isVisible()) {
                                    DiscoverFragment.tagRV.smoothScrollToPosition(0);
                                    DiscoverFragment.recyclerView.smoothScrollToPosition(0);
                                }
                                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("search")).commit();
                            } else {
                                //if the fragment does not exist, add it to fragment manager.
                                fragmentManager.beginTransaction().add(R.id.main_framelayout, new DiscoverFragment(), "search").commit();
                            }
                            if (fragmentManager.findFragmentByTag("home") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("home")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("notification") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("notification")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("profile") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("profile")).commit();
                            }
                            break;
                        case R.id.nav_create:
                            startActivity(new Intent(MainActivity.this, GalleryActivity.class));
                            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                            return false;
                        case R.id.nav_notification:
                            if (fragmentManager.findFragmentByTag("notification") != null) {
                                //if the fragment exists, show it.
                                NotificationFragment notificationFragment = (NotificationFragment) getSupportFragmentManager().findFragmentByTag("notification");
                                if (notificationFragment != null && notificationFragment.isVisible()) {
                                    //SearchFragment.discoverNested.smoothScrollTo(0, 0, 750);
                                }
                                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("notification")).commit();
                            } else {
                                //if the fragment does not exist, add it to fragment manager.
                                fragmentManager.beginTransaction().add(R.id.main_framelayout, new NotificationFragment(), "notification").commit();
                            }
                            if (fragmentManager.findFragmentByTag("home") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("home")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("search") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("search")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("profile") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("profile")).commit();
                            }
                            break;
                        case R.id.nav_profile:
                            if (fragmentManager.findFragmentByTag("profile") != null) {
                                //if the fragment exists, show it.
                                ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profile");
                                if (profileFragment != null && profileFragment.isVisible()) {
                                    ProfileSnapFragment.recyclerView.smoothScrollToPosition(0);
                                }
                                fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("profile")).commit();
                            } else {
                                //if the fragment does not exist, add it to fragment manager.
                                fragmentManager.beginTransaction().add(R.id.main_framelayout, new ProfileFragment(), "profile").commit();
                            }
                            if (fragmentManager.findFragmentByTag("home") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("home")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("search") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("search")).commit();
                            }
                            if (fragmentManager.findFragmentByTag("notification") != null) {
                                //if the other fragment is visible, hide it.
                                fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("notification")).commit();
                            }
                            break;
                    }
                    return true;
                }
            };

    private void countAppOpen() {
        //Count App Open
        SharedPreferences appOpen = getSharedPreferences("APPOPEN", 0);
        int appOpened = appOpen.getInt("appOpenedInt", 0);

        if (appOpened < 15) {
            SharedPreferences.Editor editor = appOpen.edit();
            editor.putInt("appOpenedInt", appOpened += 1);
            editor.apply();

            if (appOpened == 4 || appOpened == 15) {
                showReviewPref();
            }
        } else {
            // stop counting app opened
        }
    }

    private void showReviewPref() {
        //Show In App Review
        reviewManager = ReviewManagerFactory.create(this);

        reviewManager.requestReviewFlow().addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    reviewInfo = task.getResult();

                    Task<Void> flow = reviewManager.launchReviewFlow(MainActivity.this, reviewInfo);
                    flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }
            }
        });
    }
}