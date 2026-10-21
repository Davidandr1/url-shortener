package com.davidandriychuk.url_shortener.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class User{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    private String email;
    private String password;
    private long tenantId;
    private LocalDateTime createdAt;

    protected User() {}

    public User(long id, String email, String password, long tenantId, LocalDateTime createdAt){
        this.id = id;
        this.email = email;
        this.password = password;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, email='%s', tenantId=%d]", id, email, tenantId);
    }

    public long getId(){
        return id;
    }

    public String getemail(){
        return email;
    }

    public String getpassword(){
        return password;
    }

    public long getTenantId(){
        return tenantId;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

}