package com.euichankim.idolsnapandroid.Model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Collection {

    public String author_id;
    public String collection_id;
    public String title;
    public Object created_at;
    public Object last_added_at;
    public String visibility;

    public Collection(String title, String author_id, Object created_at, Object last_added_at, String visibility) {
        this.title = title;
        this.author_id = author_id;
        this.created_at = created_at;
        this.last_added_at = last_added_at;
        this.visibility = visibility;
    }

    public Collection() {
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getCollection_id() {
        return collection_id;
    }

    public void setCollection_id(String collection_id) {
        this.collection_id = collection_id;
    }

    public Object getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Object created_at) {
        this.created_at = created_at;
    }

    public Object getLast_added_at() {
        return last_added_at;
    }

    public void setLast_added_at(Object last_added_at) {
        this.last_added_at = last_added_at;
    }
}
