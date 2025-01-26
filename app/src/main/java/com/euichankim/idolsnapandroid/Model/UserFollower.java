package com.euichankim.idolsnapandroid.Model;

public class UserFollower {

    public String follower_user_id;
    public Object follower_since;

    public UserFollower(String follower_user_id, Object follower_since) {
        this.follower_user_id = follower_user_id;
        this.follower_since = follower_since;
    }

    public UserFollower() {
    }

    public String getFollower_user_id() {
        return follower_user_id;
    }

    public void setFollower_user_id(String follower_user_id) {
        this.follower_user_id = follower_user_id;
    }

    public Object getFollower_since() {
        return follower_since;
    }

    public void setFollower_since(Object follower_since) {
        this.follower_since = follower_since;
    }
}
