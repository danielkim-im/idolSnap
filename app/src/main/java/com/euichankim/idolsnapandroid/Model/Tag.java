package com.euichankim.idolsnapandroid.Model;

public class Tag {
    public String tag;
    public boolean available;

    public Tag(String tag, boolean available) {
        this.tag = tag;
        this.available = available;
    }

    public Tag() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
