package com.nguyenquocthai.real_time_tracker.Model;

public class NotificationItem {
    private String userID;
    private String message;
    private Long timestamp;
    private String avatar; // Optional, if you want to show different icons for different notifications

    public NotificationItem() {
    }

    public NotificationItem(String userID, String message, Long timestamp, String avatar) {
        this.userID = userID;
        this.message = message;
        this.timestamp = timestamp;
        this.avatar = avatar;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
