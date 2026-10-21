package com.davidandriychuk.url_shortener.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;


@Entity
public class Link{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    private String originalUrl;
    private String shortCode;
    private long userId;
    private long tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    protected Link() {}

    public Link(long id, String originalUrl, String shortCode, long userId, long tenantId, LocalDateTime createdAt, LocalDateTime expiresAt){
        this.id = id;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.userId = userId;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString(){return String.format("Fill in later");}

    public long getId(){
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public long getUserId() {
        return userId;
    }

    public long getTenantId() {
        return tenantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}