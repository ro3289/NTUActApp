package com.example.eventmap;

import java.util.List;

import android.app.Application;

import com.facebook.model.GraphUser;

public class FriendPickerApplication extends Application {
    private List<GraphUser> selectedUsers;
    private List<GraphUser> inviteUsers;

    public List<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }
    
    public List<GraphUser> getInviteUsers() {
        return inviteUsers;
    }

    public void setSelectedUsers(List<GraphUser> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }
    
    public void setInviteUsers(List<GraphUser> inviteUsers) 
    {
    	this.inviteUsers = inviteUsers;
    }
}
