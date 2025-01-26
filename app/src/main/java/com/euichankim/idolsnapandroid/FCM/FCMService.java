package com.euichankim.idolsnapandroid.FCM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.euichankim.idolsnapandroid.Activity.SnapActivity;
import com.euichankim.idolsnapandroid.Activity.SnapCommentActivity;
import com.euichankim.idolsnapandroid.Activity.UserProfileActivity;
import com.euichankim.idolsnapandroid.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String channelId = remoteMessage.getData().get("android_channel_id");

        if (channelId.equals("newsnap")) {
            newsnap(remoteMessage, channelId);
        } else if (channelId.equals("newcomment")) {
            newcomment(remoteMessage, channelId);
        } else if (channelId.equals("newsnaplike")) {
            newsnaplike(remoteMessage, channelId);
        } else if (channelId.equals("newfollower")) {
            newfollower(remoteMessage, channelId);
        }
    }

    private void newfollower(RemoteMessage remoteMessage, String channelId) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("user_id", remoteMessage.getData().get("following_user_id"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        String following_username = remoteMessage.getData().get("following_username");

        String channelName = null;
        String body = null;

        if (Locale.getDefault().getLanguage().equals("ko")) {
            body = following_username + "님이 회원님을 팔로우하기 시작했습니다.";
        } else {
            body = following_username + " started following you.";
        }

        channelName = getString(R.string.newfollower);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_custom_notification)
                .setContentText(body)
                .setAutoCancel(true).setContentIntent(pendingIntent)
                //.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_DEFAULT);

        String finalChannelName = channelName;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, finalChannelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    private void newsnaplike(RemoteMessage remoteMessage, String channelId) {
        Intent intent = new Intent(this, SnapActivity.class);
        intent.putExtra("snap_id", remoteMessage.getData().get("snap_id"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        String liked_user_username = remoteMessage.getData().get("liked_user_username");
        String liked_user_id = remoteMessage.getData().get("liked_user_id");

        String channelName = null;
        String body = null;

        if (Locale.getDefault().getLanguage().equals("ko")) {
            body = liked_user_username + "님이 회원님의 스냅을 좋아합니다.";
        } else {
            body = liked_user_username + " liked your snap.";
        }

        channelName = getString(R.string.newsnaplike);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_custom_notification)
                .setContentText(body)
                .setAutoCancel(true).setContentIntent(pendingIntent)
                //.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_LOW);

        String finalChannelName = channelName;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, finalChannelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    private void newcomment(RemoteMessage remoteMessage, String channelId) {
        Intent intent = new Intent(this, SnapCommentActivity.class);
        //intent.putExtra("comment_id", remoteMessage.getData().get("comment_id"));
        intent.putExtra("snap_id", remoteMessage.getData().get("snap_id"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        String author_username = remoteMessage.getData().get("author_username");
        String comment_text = remoteMessage.getData().get("comment_text");

        String channelName = null;
        String body = null;

        if (Locale.getDefault().getLanguage().equals("ko")) {
            body = author_username + "님이 댓글을 남겼습니다: " + comment_text;
        } else {
            body = author_username + " commented: " + comment_text;
        }

        channelName = getString(R.string.newcomment);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_custom_notification)
                .setContentText(body)
                .setAutoCancel(true).setContentIntent(pendingIntent)
                //.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_HIGH);

        String finalChannelName = channelName;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, finalChannelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    private void newsnap(RemoteMessage remoteMessage, String channelId) {
        Intent intent = new Intent(this, SnapActivity.class);
        intent.putExtra("snap_id", remoteMessage.getData().get("snap_id"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        String author_username = remoteMessage.getData().get("author_username");
        String preview_img_id = remoteMessage.getData().get("preview_img_id");
        String author_id = remoteMessage.getData().get("author_id");

        String channelName = null;
        String body = null;
        String imgurl = null;

        if (Locale.getDefault().getLanguage().equals("ko")) {
            body = author_username + "님이 새로운 스냅을 만들었습니다.";
        } else {
            body = author_username + " just made a new snap.";
        }

        channelName = getString(R.string.newsnap);
        imgurl = "https://cdn.idolsnap.net/snap/" + author_id + "/" + preview_img_id + "/" + preview_img_id + "_512x512";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_custom_notification)
                .setContentText(body)
                .setAutoCancel(true).setContentIntent(pendingIntent)
                //.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_DEFAULT);

        String finalChannelName = channelName;

        Glide.with(getApplicationContext()).asBitmap().load(imgurl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        //largeIcon
                        builder.setLargeIcon(resource);
                        //Big Picture
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(resource));

                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(channelId, finalChannelName, NotificationManager.IMPORTANCE_HIGH);
                            channel.setShowBadge(true);
                            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
                            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                            manager.createNotificationChannel(channel);
                        }
                        manager.notify(0, builder.build());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }
}
