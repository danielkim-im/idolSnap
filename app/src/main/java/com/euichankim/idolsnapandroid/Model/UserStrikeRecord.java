package com.euichankim.idolsnapandroid.Model;

import java.util.HashMap;
import java.util.List;

public class UserStrikeRecord {

    public String strike_id;
    public String strike_type;
    public String striked;
    public Object striked_at;
    public String user_id;
    public HashMap<String, Object> reason;

    public UserStrikeRecord(String strike_id, String strike_type, String striked, Object striked_at, String user_id, HashMap<String, Object> reason) {
        this.strike_id = strike_id;
        this.strike_type = strike_type;
        this.striked = striked;
        this.striked_at = striked_at;
        this.user_id = user_id;
        this.reason = reason;
    }

    public UserStrikeRecord() {
    }

    public String getStrike_id() {
        return strike_id;
    }

    public void setStrike_id(String strike_id) {
        this.strike_id = strike_id;
    }

    public String getStrike_type() {
        return strike_type;
    }

    public void setStrike_type(String strike_type) {
        this.strike_type = strike_type;
    }

    public String getStriked() {
        return striked;
    }

    public void setStriked(String striked) {
        this.striked = striked;
    }

    public Object getStriked_at() {
        return striked_at;
    }

    public void setStriked_at(Object striked_at) {
        this.striked_at = striked_at;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public HashMap<String, Object> getReason() {
        return reason;
    }

    public void setReason(HashMap<String, Object> reason) {
        this.reason = reason;
    }
}
