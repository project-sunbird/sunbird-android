package org.sunbird.models;

import org.ekstep.genieservices.commons.bean.CorrelationData;

import java.util.List;

/**
 * Created by vinay.narayana on 18/08/17.
 */

public class CurrentGame {
    private String identifier;
    private String startTime;
    private String mediaType;
    private List<CorrelationData> cData;

    public CurrentGame(String identifier, String startTime, String mediaType) {
        this.identifier = identifier;
        this.startTime = startTime;
        this.mediaType = mediaType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getMediaType() {
        return mediaType;
    }

    public List<CorrelationData> getcData() {
        return cData;
    }

    public void setcData(List<CorrelationData> cData) {
        this.cData = cData;
    }
}
