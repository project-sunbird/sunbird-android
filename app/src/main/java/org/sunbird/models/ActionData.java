package org.sunbird.models;

import java.io.Serializable;

/**
 * Created by JUSPAY\nikith.shetty on 24/11/17.
 */

public class ActionData implements Serializable {
    private String announcementId;

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }
}
