package com.davidandriychuk.url_shortener.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Map;


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
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

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

    public Link(String originalUrl, long userId, LocalDateTime createdAt, LocalDateTime expiresAt){
        this.originalUrl = originalUrl;
        this.userId = userId;
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

    public void setShortCode(String shortCode){
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }

    public static String generateShortCode(long id){
        if (id < 0) throw new IllegalArgumentException("negative: " + id);
        if (id == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(ALPHABET.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    public static long decodeShortCode(String s) {
        if (s.isEmpty()) throw new IllegalArgumentException("empty input");
        long result = 0;
        for (int i = 0; i < s.length(); i++) {
            int digit = ALPHABET.indexOf(s.charAt(i));
            if (digit < 0) throw new IllegalArgumentException("invalid Base62 character: " + s.charAt(i));
            result = Math.addExact(Math.multiplyExact(result, 62), digit);
        }
        return result;
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