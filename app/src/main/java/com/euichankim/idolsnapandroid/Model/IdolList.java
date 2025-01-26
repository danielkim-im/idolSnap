package com.euichankim.idolsnapandroid.Model;

public class IdolList {
    public String name_eng;
    public String name_kr;
    public String tag_eng;
    public String tag_kr;
    public Boolean isSelected = false;

    public IdolList(String name_eng, String name_kr, String tag_eng, String tag_kr, Boolean isSelected) {
        this.name_eng = name_eng;
        this.name_kr = name_kr;
        this.tag_eng = tag_eng;
        this.tag_kr = tag_kr;
        this.isSelected = isSelected;
    }

    public IdolList() {

    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getName_eng() {
        return name_eng;
    }

    public void setName_eng(String name_eng) {
        this.name_eng = name_eng;
    }

    public String getName_kr() {
        return name_kr;
    }

    public void setName_kr(String name_kr) {
        this.name_kr = name_kr;
    }

    public String getTag_eng() {
        return tag_eng;
    }

    public void setTag_eng(String tag_eng) {
        this.tag_eng = tag_eng;
    }

    public String getTag_kr() {
        return tag_kr;
    }

    public void setTag_kr(String tag_kr) {
        this.tag_kr = tag_kr;
    }
}
