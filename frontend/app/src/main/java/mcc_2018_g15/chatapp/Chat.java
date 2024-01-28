package mcc_2018_g15.chatapp;

import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.io.Serializable;
import java.util.Date;

import mcc_2018_g15.chatapp.Author;

public class Chat implements IMessage,MessageContentType.Image {


    public String id;

    public String text;

    public String type;
    public Date createdAt;
    public Author user;
    public Image imageurl;
    public Chat(){}

    public Chat(String id, Author author, String text) {

        this(id,author, text, new Date());
    }

    public Chat( String id, Author user, String text, Date createdAt) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    public void setID(String new_id){ this.id = new_id;}


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String new_text){ this.text = new_text;}


    @Exclude
    public Author getUser() {
        return user;
    }

    public void setUser(Author new_author){ this.user = new_author;}


    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public String print(){
        return getId() + ", " + getText() + ", " + getCreatedAt().toString() + ", " + getUser().toString();
    }

    public void setCreatedAt(Date new_createdAt){ this.createdAt = new_createdAt;}

    @Nullable
    @Override
    @Exclude
    public String getImageUrl() {
        return imageurl == null ? null : imageurl.url;
    }
    public void setImage(Image image) {
        this.imageurl = image;
    }


    public static class Image implements Serializable {

        private String url;
        public Image(){}

        public Image(String url) {
            this.url = url;
        }

        public String getUrl(){
            return url;
        }
    }

}

