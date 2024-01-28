package mcc_2018_g15.chatapp;

import com.google.firebase.database.Exclude;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;

import mcc_2018_g15.chatapp.Author;

public class Dialog implements IDialog {

    public String id;
    public String dialogPhoto;
    public String dialogName;
    public ArrayList<Author> users;
    public ArrayList<String> groupMembers;
    public String isGroup;
    public String admin;

    public IMessage lastMessage;
    public int unreadCount;

    public Dialog(){}

    public Dialog(String id, String dialogPhoto, String dialogName, ArrayList<Author> users, IMessage lastMessage, int unreadCount) {
        this.id = id;
        this.dialogPhoto = dialogPhoto;
        this.dialogName = dialogName;
        this.users = users;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    @Exclude
    public ArrayList<Author> getUsers() {
        return users;
    }

    @Override
    @Exclude
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    @Exclude
    public void setLastMessage(IMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = dialogPhoto;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    @Exclude
    public void setUsers(ArrayList<Author> users) {
        this.users = users;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public String toString() {
        return "Dialog{" +
                "id='" + id + '\'' +
                ", dialogPhoto='" + dialogPhoto + '\'' +
                ", dialogName='" + dialogName + '\'' +
                ", users=" + users +
                ", lastMessage=" + lastMessage +
                ", unreadCount=" + unreadCount +
                '}';
    }

//---------------------------------------------------------------------------------------------

}