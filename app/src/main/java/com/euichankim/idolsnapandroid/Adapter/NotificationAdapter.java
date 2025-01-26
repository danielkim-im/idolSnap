package com.euichankim.idolsnapandroid.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.euichankim.idolsnapandroid.Activity.NotificationActivity;
import com.euichankim.idolsnapandroid.Activity.SnapActivity;
import com.euichankim.idolsnapandroid.Activity.SnapCommentActivity;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.Fragment.NotificationFragment;
import com.euichankim.idolsnapandroid.Model.Notification;
import com.euichankim.idolsnapandroid.Model.UserStrikeRecord;
import com.euichankim.idolsnapandroid.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {


    private Context mContext;
    private List<Notification> notification;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    public NotificationAdapter(Context mContext, List<Notification> notification) {
        this.mContext = mContext;
        this.notification = notification;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        View itemView = holder.itemView;
        TextView dateTxt = holder.dateTxt;
        TextView bodyTxt = holder.bodyTxt;
        ImageView imageView = holder.imageView;
        CircleImageView profileImg = holder.profileImg;
        ConstraintLayout mediaCons = holder.mediaCons;
        int position_a = holder.getBindingAdapterPosition();
        if (notification.get(position).getType().equals("new_comment")) {
            //newcomment(notification, holder.dateTxt, holder.bodyTxt, holder.imageView, holder.profileImg, holder.itemView);
            String comment_id = notification.get(position_a).getComment_id();
            String snap_id = notification.get(position_a).getSnap_id();
            String comment_author_id = notification.get(position_a).getAuthor_id();
            String comment_text = notification.get(position_a).getComment_text();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userIntent = new Intent(mContext, SnapCommentActivity.class);
                    userIntent.putExtra("snap_id", snap_id);
                    mContext.startActivity(userIntent);
                    ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

            dateTxt.setText(timestampToString(((Timestamp) notification.get(position_a).getNotified_at()).getSeconds() * 1000));
            firestore.collection("snap").document(snap_id).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String snap_author_id = documentSnapshot.getString("author_id");
                            String content_id = documentSnapshot.getString("content_id");
                            String content_url = "https://cdn.idolsnap.net/snap/" + snap_author_id + "/" + content_id + "/" + content_id + "_512x512";
                            Glide.with(mContext)
                                    .load(content_url)
                                    .override(512, 512)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .error(R.drawable.ic_placeholder)
                                    .into(imageView);

                            profileImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent userIntent = new Intent(mContext, UserProfileActivity.class);
                                    userIntent.putExtra("user_id", snap_author_id);
                                    mContext.startActivity(userIntent);
                                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            });

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent userIntent = new Intent(mContext, SnapActivity.class);
                                    userIntent.putExtra("snap_id", snap_id);
                                    mContext.startActivity(userIntent);
                                    ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            firestore.collection("user").document(comment_author_id).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String profile_img_id = documentSnapshot.getString("profile_img_id");
                            if (profile_img_id.equals("default") || profile_img_id == null || profile_img_id.replaceAll("\\s", "").isEmpty()) {
                                Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                            } else {
                                String content_url = "https://cdn.idolsnap.net/user/" + comment_author_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                                Glide.with(itemView)
                                        .load(content_url)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                                        .error(R.drawable.ic_placeholder)
                                        .into(profileImg);
                            }
                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                bodyTxt.setText(documentSnapshot.getString("username") + "님이 댓글을 남겼습니다: " + comment_text);
                            } else {
                                bodyTxt.setText(documentSnapshot.getString("username") + " commented: " + comment_text);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (notification.get(position).getType().equals("new_follower")) {
            //newfollower(notification);
            String new_follower_id = notification.get(position_a).getNew_follower_id();
            String received_user_id = notification.get(position_a).getReceived_user_id();

            mediaCons.setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                    userIntent.putExtra("user_id", new_follower_id);
                    itemView.getContext().startActivity(userIntent);
                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

            dateTxt.setText(timestampToString(((Timestamp) notification.get(position_a).getNotified_at()).getSeconds() * 1000));

            firestore.collection("user").document(new_follower_id).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String profile_img_id = documentSnapshot.getString("profile_img_id");
                            if (profile_img_id.equals("default") || profile_img_id == null || profile_img_id.replaceAll("\\s", "").isEmpty()) {
                                Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                            } else {
                                String content_url = "https://cdn.idolsnap.net/user/" + new_follower_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                                Glide.with(itemView)
                                        .load(content_url)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                                        .error(R.drawable.ic_placeholder)
                                        .into(profileImg);
                            }
                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                bodyTxt.setText(documentSnapshot.getString("username") + "님이 회원님을 팔로우하기 시작했습니다.");
                            } else {
                                bodyTxt.setText(documentSnapshot.getString("username") + " started following you.");
                            }
                            profileImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                    userIntent.putExtra("user_id", new_follower_id);
                                    itemView.getContext().startActivity(userIntent);
                                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (notification.get(position).getType().equals("new_snap_like")) {
            //newsnaplike(notification);
            String snap_id = notification.get(position_a).getSnap_id();
            String snap_author_id = notification.get(position_a).getSnap_author_id();
            String liked_user_id = notification.get(position_a).getLiked_user_id();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent snapIntent = new Intent(itemView.getContext(), SnapActivity.class);
                    snapIntent.putExtra("snap_id", snap_id);
                    itemView.getContext().startActivity(snapIntent);
                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

            dateTxt.setText(timestampToString(((Timestamp) notification.get(position_a).getNotified_at()).getSeconds() * 1000));

            firestore.collection("user").document(liked_user_id).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String profile_img_id = documentSnapshot.getString("profile_img_id");
                            if (profile_img_id.equals("default") || profile_img_id == null || profile_img_id.replaceAll("\\s", "").isEmpty()) {
                                Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                            } else {
                                String content_url = "https://cdn.idolsnap.net/user/" + liked_user_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                                Glide.with(itemView)
                                        .load(content_url)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                                        .error(R.drawable.ic_placeholder)
                                        .into(profileImg);
                            }
                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                bodyTxt.setText(documentSnapshot.getString("username") + "님이 회원님의 스냅을 좋아합니다.");
                            } else {
                                bodyTxt.setText(documentSnapshot.getString("username") + " liked your snap.");
                            }
                            profileImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                    userIntent.putExtra("user_id", liked_user_id);
                                    itemView.getContext().startActivity(userIntent);
                                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            firestore.collection("snap").document(snap_id).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String content_id = documentSnapshot.getString("content_id");
                            String content_url = "https://cdn.idolsnap.net/snap/" + snap_author_id + "/" + content_id + "/" + content_id + "_512x512";
                            Glide.with(itemView)
                                    .load(content_url)
                                    .override(512, 512)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .error(R.drawable.ic_placeholder)
                                    .into(imageView);

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent userIntent = new Intent(itemView.getContext(), SnapActivity.class);
                                    userIntent.putExtra("snap_id", snap_id);
                                    itemView.getContext().startActivity(userIntent);
                                    ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deleteBottomSheet(notification.get(position).getNotification_id());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImg;
        TextView bodyTxt, dateTxt;
        ImageView imageView;
        ConstraintLayout mediaCons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            profileImg = itemView.findViewById(R.id.notification_profileimg);
            bodyTxt = itemView.findViewById(R.id.notification_bodyTxt);
            dateTxt = itemView.findViewById(R.id.notification_dateTxt);
            imageView = itemView.findViewById(R.id.notification_img);
            mediaCons = itemView.findViewById(R.id.notification_mediacons);
        }
    }

    private void deleteBottomSheet(String notification_id) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
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
        deleteBtn.setText(mContext.getString(R.string.deleteanyway));
        titleTxt.setText(mContext.getString(R.string.alert_deleteNotification));
        descTxt.setText(mContext.getString(R.string.notificationsautomaticallydeleted));

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                bottomSheetDialog.setCancelable(false);

                firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                        .collection("notification").document(notification_id)
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.snapdeleted), Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                                //NotificationFragment.updateNotifications();
                                NotificationFragment.getNotifications();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            }
        });

        bottomSheetDialog.show();
    }

    /*private void newcomment(Notification notification, TextView dateTxt, TextView bodyTxt, ImageView imageView, CircleImageView profileImg, View itemView) {
        String comment_id = notification.getComment_id();
        String snap_id = notification.getSnap_id();
        String comment_author_id = notification.getAuthor_id();
        String comment_text = notification.getComment_text();

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(mContext, SnapCommentActivity.class);
                userIntent.putExtra("snap_id", snap_id);
                mContext.startActivity(userIntent);
                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        dateTxt.setText(timestampToString(((Timestamp) notification.getNotified_at()).getSeconds() * 1000));
        firestore.collection("snap").document(snap_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String snap_author_id = documentSnapshot.getString("author_id");
                        String content_id = documentSnapshot.getString("content_id");
                        String content_url = "https://cdn.idolsnap.net/snap/" + snap_author_id + "/" + content_id + "/" + content_id + "_512x512";
                        Glide.with(mContext)
                                .load(content_url)
                                .override(512, 512)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .error(R.drawable.ic_placeholder)
                                .into(imageView);

                        profileImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(mContext, UserProfileActivity.class);
                                userIntent.putExtra("user_id", snap_author_id);
                                mContext.startActivity(userIntent);
                                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });

                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(mContext, SnapActivity.class);
                                userIntent.putExtra("snap_id", snap_id);
                                mContext.startActivity(userIntent);
                                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        firestore.collection("user").document(comment_author_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String profile_img_id = documentSnapshot.getString("profile_img_id");
                        if (profile_img_id.equals("default") || profile_img_id == null || profile_img_id.replaceAll("\\s", "").isEmpty()) {
                            Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                        } else {
                            String content_url = "https://cdn.idolsnap.net/user/" + comment_author_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                            Glide.with(itemView)
                                    .load(content_url)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .error(R.drawable.ic_placeholder)
                                    .into(profileImg);
                        }
                        if (Locale.getDefault().getLanguage().equals("ko")) {
                            bodyTxt.setText(documentSnapshot.getString("username") + "님이 댓글을 남겼습니다: " + comment_text);
                        } else {
                            bodyTxt.setText(documentSnapshot.getString("username") + " commented: " + comment_text);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

    /*private void newfollower(Notification notification, ConstraintLayout mediaCons, View itemView, TextView dateTxt, CircleImageView profileImg, TextView bodyTxt) {
        String new_follower_id = notification.getNew_follower_id();
        String received_user_id = notification.getReceived_user_id();

        mediaCons.setVisibility(View.GONE);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                userIntent.putExtra("user_id", new_follower_id);
                itemView.getContext().startActivity(userIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        dateTxt.setText(timestampToString(((Timestamp) notification.getNotified_at()).getSeconds() * 1000));

        firestore.collection("user").document(new_follower_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String profile_img_id = documentSnapshot.getString("profile_img_id");
                        if (profile_img_id.equals("default") || profile_img_id == null || profile_img_id.replaceAll("\\s", "").isEmpty()) {
                            Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                        } else {
                            String content_url = "https://cdn.idolsnap.net/user/" + new_follower_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                            Glide.with(itemView)
                                    .load(content_url)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .error(R.drawable.ic_placeholder)
                                    .into(profileImg);
                        }
                        if (Locale.getDefault().getLanguage().equals("ko")) {
                            bodyTxt.setText(documentSnapshot.getString("username") + "님이 회원님을 팔로우하기 시작했습니다.");
                        } else {
                            bodyTxt.setText(documentSnapshot.getString("username") + " started following you.");
                        }
                        profileImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                userIntent.putExtra("user_id", new_follower_id);
                                itemView.getContext().startActivity(userIntent);
                                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

    /*private void newsnaplike(Notification notification, View itemView, TextView dateTxt, CircleImageView profileImg, TextView bodyTxt, ImageView imageView) {
        String snap_id = notification.getSnap_id();
        String snap_author_id = notification.getSnap_author_id();
        String liked_user_id = notification.getLiked_user_id();

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent snapIntent = new Intent(itemView.getContext(), SnapActivity.class);
                snapIntent.putExtra("snap_id", snap_id);
                itemView.getContext().startActivity(snapIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        dateTxt.setText(timestampToString(((Timestamp) notification.getNotified_at()).getSeconds() * 1000));

        firestore.collection("user").document(liked_user_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String profile_img_id = documentSnapshot.getString("profile_img_id");
                        if (profile_img_id.equals("default") || profile_img_id == null || profile_img_id.replaceAll("\\s", "").isEmpty()) {
                            Glide.with(itemView).load(R.drawable.ic_profile_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(profileImg);
                        } else {
                            String content_url = "https://cdn.idolsnap.net/user/" + liked_user_id + "/" + profile_img_id + "/" + profile_img_id + "_128x128";
                            Glide.with(itemView)
                                    .load(content_url)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .error(R.drawable.ic_placeholder)
                                    .into(profileImg);
                        }
                        if (Locale.getDefault().getLanguage().equals("ko")) {
                            bodyTxt.setText(documentSnapshot.getString("username") + "님이 회원님의 스냅을 좋아합니다.");
                        } else {
                            bodyTxt.setText(documentSnapshot.getString("username") + " liked your snap.");
                        }
                        profileImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                userIntent.putExtra("user_id", liked_user_id);
                                itemView.getContext().startActivity(userIntent);
                                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        firestore.collection("snap").document(snap_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String content_id = documentSnapshot.getString("content_id");
                        String content_url = "https://cdn.idolsnap.net/snap/" + snap_author_id + "/" + content_id + "/" + content_id + "_512x512";
                        Glide.with(itemView)
                                .load(content_url)
                                .override(512, 512)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .error(R.drawable.ic_placeholder)
                                .into(imageView);

                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(itemView.getContext(), SnapActivity.class);
                                userIntent.putExtra("snap_id", snap_id);
                                itemView.getContext().startActivity(userIntent);
                                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

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
