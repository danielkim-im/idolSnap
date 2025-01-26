package com.euichankim.idolsnapandroid.Model;

public class Comment {

    public String comment_id;
    public String snap_id;
    public String author_id;
    public String comment_text;
    public int like_count;
    public Object commented_at;
    public boolean visibility_private;

    public Comment(String snap_id, String author_id, String comment_text, Object commented_at, boolean visibility_private) {
        this.snap_id = snap_id;
        this.author_id = author_id;
        this.comment_text = comment_text;
        this.commented_at = commented_at;
        this.visibility_private = visibility_private;
    }

    public Comment() {
    }

    public boolean isVisibility_private() {
        return visibility_private;
    }

    public void setVisibility_private(boolean visibility_private) {
        this.visibility_private = visibility_private;
    }

    public int getLike_count() {
        return like_count;
    }

    public void setLike_count(int like_count) {
        this.like_count = like_count;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
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

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public Object getCommented_at() {
        return commented_at;
    }

    public void setCommented_at(Object commented_at) {
        this.commented_at = commented_at;
    }
}
