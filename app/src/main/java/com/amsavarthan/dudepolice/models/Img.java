package com.amsavarthan.dudepolice.models;

public class Img {

    private String url,id,color;

    public Img(String id, String url, String color){
        this.id=id;
        this.url=url;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
