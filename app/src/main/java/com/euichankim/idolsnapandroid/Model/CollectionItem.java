package com.euichankim.idolsnapandroid.Model;

public class CollectionItem {
    public String snap_id;
    public String added_by;
    public Object added_at;

    public CollectionItem(String snap_id, String added_by, Object added_at) {
        this.snap_id = snap_id;
        this.added_by = added_by;
        this.added_at = added_at;
    }

    public CollectionItem() {
    }

    public String getAdded_by() {
        return added_by;
    }

    public void setAdded_by(String added_by) {
        this.added_by = added_by;
    }

    public String getSnap_id() {
        return snap_id;
    }

    public void setSnap_id(String snap_id) {
        this.snap_id = snap_id;
    }

    public Object getAdded_at() {
        return added_at;
    }

    public void setAdded_at(Object added_at) {
        this.added_at = added_at;
    }
}
