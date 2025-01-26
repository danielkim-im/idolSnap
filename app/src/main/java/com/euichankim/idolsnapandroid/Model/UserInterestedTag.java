package com.euichankim.idolsnapandroid.Model;

import java.util.List;

public class UserInterestedTag {
    public String tag;
    public Object last_interacted;

    public UserInterestedTag(String tag, Object last_interacted) {
        this.tag = tag;
        this.last_interacted = last_interacted;
    }

    public UserInterestedTag() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getLast_interacted() {
        return last_interacted;
    }

    public void setLast_interacted(Object last_interacted) {
        this.last_interacted = last_interacted;
    }
}
