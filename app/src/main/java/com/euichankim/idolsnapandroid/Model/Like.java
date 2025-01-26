package com.euichankim.idolsnapandroid.Model;

public class Like {

    public String user_id;
    public Object liked_at;

    public Like(String user_id, Object liked_at) {
        this.user_id = user_id;
        this.liked_at = liked_at;
    }

    public Like() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Object getLiked_at() {
        return liked_at;
    }

    public void setLiked_at(Object liked_at) {
        this.liked_at = liked_at;
    }
}
