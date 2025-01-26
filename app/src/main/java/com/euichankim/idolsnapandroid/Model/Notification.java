package com.euichankim.idolsnapandroid.Model;

public class Notification {
    public String notification_id;
    public String type;
    public Object notified_at;
    public Object notification_expire;

    //new_comment
    public String comment_id;
    public String snap_id;
    public String author_id;
    public String comment_text;

    //new_follower
    public String new_follower_id;
    public String received_user_id;

    //new_snaplike
    //public String snap_id; duplicate exists in 'new_comment'
    public String snap_author_id;
    public String liked_user_id;

    public Notification(String notification_id, String type, Object notified_at, Object notification_expire, String comment_id, String snap_id, String author_id, String comment_text, String new_follower_id, String received_user_id, String snap_author_id, String liked_user_id) {
        this.notification_id = notification_id;
        this.type = type;
        this.notified_at = notified_at;
        this.notification_expire = notification_expire;
        this.comment_id = comment_id;
        this.snap_id = snap_id;
        this.author_id = author_id;
        this.comment_text = comment_text;
        this.new_follower_id = new_follower_id;
        this.received_user_id = received_user_id;
        this.snap_author_id = snap_author_id;
        this.liked_user_id = liked_user_id;
    }

    public Notification() {
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getNotified_at() {
        return notified_at;
    }

    public void setNotified_at(Object notified_at) {
        this.notified_at = notified_at;
    }

    public Object getNotification_expire() {
        return notification_expire;
    }

    public void setNotification_expire(Object notification_expire) {
        this.notification_expire = notification_expire;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getSnap_id() {
        return snap_id;
    }

    public void setSnap_id(String snap_id) {
        this.snap_id = snap_id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getNew_follower_id() {
        return new_follower_id;
    }

    public void setNew_follower_id(String new_follower_id) {
        this.new_follower_id = new_follower_id;
    }

    public String getReceived_user_id() {
        return received_user_id;
    }

    public void setReceived_user_id(String received_user_id) {
        this.received_user_id = received_user_id;
    }

    public String getSnap_author_id() {
        return snap_author_id;
    }

    public void setSnap_author_id(String snap_author_id) {
        this.snap_author_id = snap_author_id;
    }

    public String getLiked_user_id() {
        return liked_user_id;
    }

    public void setLiked_user_id(String liked_user_id) {
        this.liked_user_id = liked_user_id;
    }
}
