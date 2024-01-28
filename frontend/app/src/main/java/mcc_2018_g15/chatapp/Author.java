package mcc_2018_g15.chatapp;

import com.stfalcon.chatkit.commons.models.IUser;

import java.io.Serializable;


public class Author implements Serializable,IUser {

    private String id;
    private String name;
    private String avatar;
    //private boolean online;

    public Author(){}

    public Author(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        //this.online = online;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }


    public Boolean isBoolean() {
        return true;
    }

    @Override
    public String toString() {
        return "Author [id=" + id + ", name=" + name
        + ", avatar=" + avatar + "]";

    }
}