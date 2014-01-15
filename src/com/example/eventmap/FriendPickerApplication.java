package com.example.eventmap;

import java.util.List;

import android.app.Application;

import com.facebook.model.GraphUser;

public class FriendPickerApplication extends Application {
    private List<GraphUser> selectedUsers;

    public List<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<GraphUser> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }
}
