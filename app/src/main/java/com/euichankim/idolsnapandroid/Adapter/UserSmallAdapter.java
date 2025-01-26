package com.euichankim.idolsnapandroid.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.Model.User;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSmallAdapter extends RecyclerView.Adapter<UserSmallAdapter.ViewHolder> {

    private Context mContext;
    private List<User> user;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    public UserSmallAdapter(Context mContext, List<User> user) {
        this.mContext = mContext;
        this.user = user;
    }

    @NonNull
    @Override
    public UserSmallAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSmallAdapter.ViewHolder holder, int position) {
        if (user.get(position).getProfile_img_id().equals("default") || user.get(position).getProfile_img_id() == null || user.get(position).getProfile_img_id().replaceAll("\\s", "").isEmpty()) {
            Glide.with(holder.itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.profileImg);
        } else {
            String profile_img_id = user.get(position).getProfile_img_id();
            String user_id = user.get(position).getUser_id();
            String profileImgUrl = "https://cdn.idolsnap.net/user/" + user_id + "/" + profile_img_id + "/" + profile_img_id + "_480x480";
            Glide.with(holder.itemView).load(profileImgUrl).override(480, 480).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.profileImg);
        }
        holder.usernameTxt.setText("@" + user.get(position).getUsername());
        holder.nameTxt.setText(user.get(position).getName());

        checkVerificationStatus(user.get(position).getUser_id(), holder.verifiedIcon);
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
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        icon.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImg;
        TextView usernameTxt, nameTxt;
        ImageView verifiedIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImg = itemView.findViewById(R.id.itemuser_profileImg);
            usernameTxt = itemView.findViewById(R.id.itemuser_username);
            nameTxt = itemView.findViewById(R.id.itemuser_name);
            verifiedIcon = itemView.findViewById(R.id.itemuser_verifiedicon);
            verifiedIcon.setVisibility(View.GONE);

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
