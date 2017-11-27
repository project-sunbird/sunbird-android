package org.sunbird.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by JUSPAY\nikith.shetty on 24/11/17.
 */

public class Notification implements Serializable {
    private long expiryTime;
    private String displayTime;
    private Date receivedAt;
    private String notificationJson;
    private String status;
    private int msgid;
    private String title;
    private String msg;
    private int relativetime;
    private String icon;
    private String time;
    private int validity;
    private int actionid;
    private ActionData actiondata;
    private String dispbehavior;
    private int isRead;

    public Notification() {
    }

    public Date getReceivedAt() {
        return this.receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getNotificationJson() {
        return this.notificationJson;
    }

    public void setNotificationJson(String notificationJson) {
        this.notificationJson = notificationJson;
    }

    public int getMsgid() {
        return this.msgid;
    }

    public void setMsgid(int msgid) {
        this.msgid = msgid;
    }

    public long getExpiryTime() {
        return this.expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getDisplayTime() {
        return this.displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRelativetime() {
        return this.relativetime;
    }

    public void setRelativetime(int relativetime) {
        this.relativetime = relativetime;
    }

    public int getActionid() {
        return this.actionid;
    }

    public void setActionid(int actionid) {
        this.actionid = actionid;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getValidity() {
        return this.validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public ActionData getActiondata() {
        return this.actiondata;
    }

    public void setActiondata(ActionData actiondata) {
        this.actiondata = actiondata;
    }

    public String getDispbehavior() {
        return this.dispbehavior;
    }

    public void setDispbehavior(String dispbehavior) {
        this.dispbehavior = dispbehavior;
    }

    public int isRead() {
        return this.isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }
}