package com.euichankim.idolsnapandroid.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.Model.UserFollowing;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserLargeAdapter extends RecyclerView.Adapter<UserLargeAdapter.ViewHolder> {

    private Context mContext;
    private List<User> user;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    boolean isFollowing;

    public UserLargeAdapter(Context mContext, List<User> user) {
        this.mContext = mContext;
        this.user = user;
    }

    @NonNull
    @Override
    public UserLargeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user_large, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserLargeAdapter.ViewHolder holder, int position) {
        if (user.get(position).getProfile_img_id().equals("default") || user.get(position).getProfile_img_id() == null || user.get(position).getProfile_img_id().replaceAll("\\s", "").isEmpty()) {
            Glide.with(holder.itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.circleImageView);
        } else {
            String profile_img_id = user.get(position).getProfile_img_id();
            String user_id = user.get(position).getUser_id();
            String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_480x480";
            Glide.with(holder.itemView).load(profileImgUrl).override(480, 480).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.circleImageView);
        }

        holder.nameTxt.setText(user.get(position).getName());
        holder.usernameTxt.setText("@" + user.get(position).getUsername());

        //get follow status
        checkifHidden(user.get(position).getUser_id(), holder.mainCons);
        getFollowStatus(user.get(position).getUser_id(), holder.followBtn, holder.followStatusTxt, holder.followProgressbar, holder.followBtnBG);
        getVerificationStatus(user.get(position).getUser_id(), holder.verifiedIcon);

        holder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.followBtn.setVisibility(View.INVISIBLE);
                holder.followProgressbar.setVisibility(View.VISIBLE);
                if (isFollowing) {
                    //remove follow
                    firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following")
                            .document(user.get(position).getUser_id()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    getFollowStatus(user.get(position).getUser_id(), holder.followBtn, holder.followStatusTxt, holder.followProgressbar, holder.followBtnBG);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    holder.followBtn.setVisibility(View.VISIBLE);
                                    holder.followProgressbar.setVisibility(View.GONE);
                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    UserFollowing userFollowing = new UserFollowing(mAuth.getCurrentUser().getUid(), user.get(position).getUser_id(), FieldValue.serverTimestamp(), true);
                    /*Map<String, Object> map = new HashMap<>();
                    map.put("following_user_id", user.get(position).getUser_id());
                    map.put("followed_by", mAuth.getCurrentUser().getUid());
                    map.put("following_since", FieldValue.serverTimestamp());*/
                    firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following")
                            .document(user.get(position).getUser_id()).set(userFollowing).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    getFollowStatus(user.get(position).getUser_id(), holder.followBtn, holder.followStatusTxt, holder.followProgressbar, holder.followBtnBG);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    holder.followBtn.setVisibility(View.VISIBLE);
                                    holder.followProgressbar.setVisibility(View.GONE);
                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void checkifHidden(String author_id, ConstraintLayout mainCons) {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("hide").document(author_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            mainCons.setVisibility(View.GONE);
                        } else {
                            mainCons.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getVerificationStatus(String user_id, ImageView icon) {
        firestore.collection("user").document(user_id).collection("public_info").document("count").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        icon.setVisibility(View.GONE);
                    }
                });
    }

    private void getFollowStatus(String user_id, CardView followBtn, TextView followingStatusTxt, ProgressBar followProgressbar, LinearLayout followBtnBG) {
        firestore.collection("user").document(mAuth.getCurrentUser().getUid()).collection("following")
                .document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // user is following
                            followBtn.setVisibility(View.VISIBLE);
                            followProgressbar.setVisibility(View.GONE);
                            isFollowing = true;
                            followingStatusTxt.setText(mContext.getString(R.string.following));
                            followingStatusTxt.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                            followBtnBG.setBackground(mContext.getDrawable(R.drawable.background_followbtn));
                        } else {
                            // user is not following
                            followBtn.setVisibility(View.VISIBLE);
                            followProgressbar.setVisibility(View.GONE);
                            isFollowing = false;
                            followingStatusTxt.setText(mContext.getString(R.string.follow));
                            followingStatusTxt.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                            followBtnBG.setBackground(mContext.getDrawable(R.color.red));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        followBtn.setVisibility(View.VISIBLE);
                        followProgressbar.setVisibility(View.GONE);
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return user.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView nameTxt, usernameTxt, followStatusTxt;
        CardView followBtn;
        LinearLayout followBtnBG;
        ConstraintLayout mainCons;
        ImageView verifiedIcon;
        ProgressBar followProgressbar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();

            isFollowing = false;
            followProgressbar = itemView.findViewById(R.id.userlarge_progressbar);
            circleImageView = itemView.findViewById(R.id.userlarge_profileimg);
            nameTxt = itemView.findViewById(R.id.userlarge_nameTxt);
            followBtnBG = itemView.findViewById(R.id.userlarge_followBtnBG);
            mainCons = itemView.findViewById(R.id.userlarge_mainCons);
            verifiedIcon = itemView.findViewById(R.id.userlarge_verifiedicon);
            verifiedIcon.setVisibility(View.GONE);
            usernameTxt = itemView.findViewById(R.id.userlarge_usernameTxt);
            followBtn = itemView.findViewById(R.id.userlarge_followcardview);
            followStatusTxt = itemView.findViewById(R.id.userlarge_followtxtview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                    userIntent.putExtra("user_id", user.get(getAdapterPosition()).getUser_id());
                    itemView.getContext().startActivity(userIntent);
                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }
}
