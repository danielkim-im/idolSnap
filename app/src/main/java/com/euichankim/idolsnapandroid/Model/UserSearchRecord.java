package com.euichankim.idolsnapandroid.Model;

public class UserSearchRecord {
    public String searched;
    public String searched_key;
    public Object searched_at;
    public Object delete_after;

    public UserSearchRecord(String searched, String searched_key, Object searched_at, Object delete_after) {
        this.searched = searched;
        this.searched_key = searched_key;
        this.searched_at = searched_at;
        this.delete_after = delete_after;
    }

    public UserSearchRecord() {
    }

    public Object getDelete_after() {
        return delete_after;
    }

    public void setDelete_after(Object delete_after) {
        this.delete_after = delete_after;
    }

    public Object getSearched_at() {
        return searched_at;
    }

    public void setSearched_at(Object searched_at) {
        this.searched_at = searched_at;
    }

    public String getSearched_key() {
        return searched_key;
    }

    public void setSearched_key(String searched_key) {
        this.searched_key = searched_key;
    }

    public String getSearched() {
        return searched;
    }

    public void setSearched(String searched) {
        this.searched = searched;
    }
}
