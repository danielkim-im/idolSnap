package com.euichankim.idolsnapandroid.Model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Snap {
    public String snap_id;
    public String author_id;
    public String description;
    public Object created_at;
    public String content_type;
    public String content_id;
    public String source;
    public int like_count;
    public boolean visibility_private;
    public List<String> tag;
    public int popularity;
    public Object lifespan;
    public boolean allow_comment;

    public Snap(String author_id, String description, Object created_at, String content_type, String content_id, boolean visibility_private, String source, boolean allow_comment) {
        this.author_id = author_id;
        this.description = description;
        this.created_at = created_at;
        this.content_type = content_type;
        this.content_id = content_id;
        this.visibility_private = visibility_private;
        this.source = source;
        this.allow_comment = allow_comment;
    }

    public Snap() {
    }

    public boolean isAllow_comment() {
        return allow_comment;
    }

    public void setAllow_comment(boolean allow_comment) {
        this.allow_comment = allow_comment;
    }

    public boolean isVisibility_private() {
        return visibility_private;
    }

    public void setVisibility_private(boolean visibility_private) {
        this.visibility_private = visibility_private;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public Object getLifespan() {
        return lifespan;
    }

    public void setLifespan(Object lifespan) {
        this.lifespan = lifespan;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getLike_count() {
        return like_count;
    }

    public void setLike_count(int like_count) {
        this.like_count = like_count;
    }

    public String getContent_id() {
        return content_id;
    }

    public void setContent_id(String content_id) {
        this.content_id = content_id;
    }

    public String getSnap_id() {
        return snap_id;
    }

    public void setSnap_id(String snap_id) {
        this.snap_id = snap_id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Object created_at) {
        this.created_at = created_at;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }
}
