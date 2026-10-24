package com.davidandriychuk.url_shortener.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ClickEvent{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    private long linkId;
    private String timestamp;
    private String ipAddress;
    private String userAgent;
    private String referrer;

    protected ClickEvent() {}

    public ClickEvent(long id, long linkId, String timestamp, String ipAddress, String userAgent,String referrer){
        this.id = id;
        this.linkId = linkId;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referrer = referrer;
    }

    @Override
    public String toString(){
        return "Do later";
    }

    public long getId(){return id;}

    public long getLinkId(){return linkId;}

    public String getTimestamp(){return timestamp;}

    public String getIpAddress() {return ipAddress;}

    public String getUserAgent() {return userAgent;}

    public String getReferrer() {return referrer;}


}
