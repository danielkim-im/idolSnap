package com.euichankim.idolsnapandroid.ViewHolder;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.Model.Like;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class User_SmallViewHolder extends RecyclerView.ViewHolder {

    CircleImageView circleImageView;
    TextView usernameTxt, nameTxt;
    ImageView verifiedIcon;
    FirebaseFirestore firestore;

    public User_SmallViewHolder(@NonNull View itemView) {
        super(itemView);
        firestore = FirebaseFirestore.getInstance();
        circleImageView = itemView.findViewById(R.id.itemuser_profileImg);
        usernameTxt = itemView.findViewById(R.id.itemuser_username);
        nameTxt = itemView.findViewById(R.id.itemuser_name);
        verifiedIcon = itemView.findViewById(R.id.itemuser_verifiedicon);
    }

    public void searchresultuserfragment_bind(User user) {
        if (user.getProfile_img_id().equals("default") || user.getProfile_img_id() == null || user.getProfile_img_id().replaceAll("\\s", "").isEmpty()) {
            Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(circleImageView);
        } else {
            String profile_img_id = user.getProfile_img_id();
            String user_id = user.getUser_id();
            String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_480x480";
            Glide.with(itemView).load(profileImgUrl).override(480, 480).diskCacheStrategy(DiskCacheStrategy.DATA).into(circleImageView);
        }
        usernameTxt.setText("@" + user.getUsername());
        nameTxt.setText(user.getName());


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                userIntent.putExtra("user_id", user.getUser_id());
                itemView.getContext().startActivity(userIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        checkVerificationStatus(user.getUser_id(), verifiedIcon);
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

    public void snaplikefollowuser_bind(String user_id) {
        firestore.collection("user").document(user_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user.getProfile_img_id().equals("default") || user.getProfile_img_id() == null || user.getProfile_img_id().replaceAll("\\s", "").isEmpty()) {
                            Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(circleImageView);
                        } else {
                            String profile_img_id = user.getProfile_img_id();
                            String user_id = user.getUser_id();
                            String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_480x480";
                            Glide.with(itemView).load(profileImgUrl).override(480, 480).diskCacheStrategy(DiskCacheStrategy.DATA).into(circleImageView);
                        }
                        usernameTxt.setText("@" + user.getUsername());
                        nameTxt.setText(user.getName());


                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                userIntent.putExtra("user_id", user.getUser_id());
                                itemView.getContext().startActivity(userIntent);
                                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });

                        checkVerificationStatus(user.getUser_id(), verifiedIcon);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
