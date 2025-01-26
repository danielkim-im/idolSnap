package com.euichankim.idolsnapandroid.Model;

import java.util.List;

public class User {
    public String description;
    public String name;
    public String profile_img_id;
    public String user_id;
    public String username;
    public boolean suspended;
    public List<String> keyword;

    public User(String description, String name, String profile_img_id, String user_id, String username, boolean suspended, List<String> keyword) {
        this.description = description;
        this.name = name;
        this.profile_img_id = profile_img_id;
        this.user_id = user_id;
        this.username = username;
        this.suspended = suspended;
        this.keyword = keyword;
    }

    public User() {
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_img_id() {
        return profile_img_id;
    }

    public void setProfile_img_id(String profile_img_id) {
        this.profile_img_id = profile_img_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
