package com.euichankim.idolsnapandroid.Model;

public class UserFollowing {
    public String followed_by;
    public String following_user_id;
    public Object following_since;
    public boolean receive_notification;

    public UserFollowing() {
    }

    public UserFollowing(String followed_by, String following_user_id, Object following_since, boolean receive_notification) {
        this.followed_by = followed_by;
        this.following_user_id = following_user_id;
        this.following_since = following_since;
        this.receive_notification = receive_notification;
    }

    public boolean isReceive_notification() {
        return receive_notification;
    }

    public void setReceive_notification(boolean receive_notification) {
        this.receive_notification = receive_notification;
    }

    public String getFollowed_by() {
        return followed_by;
    }

    public void setFollowed_by(String followed_by) {
        this.followed_by = followed_by;
    }

    public String getFollowing_user_id() {
        return following_user_id;
    }

    public void setFollowing_user_id(String following_user_id) {
        this.following_user_id = following_user_id;
    }

    public Object getFollowing_since() {
        return following_since;
    }

    public void setFollowing_since(Object following_since) {
        this.following_since = following_since;
    }
}
