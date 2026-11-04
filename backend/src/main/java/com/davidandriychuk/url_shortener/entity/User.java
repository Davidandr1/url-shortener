package com.davidandriychuk.url_shortener.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
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
    public User(String email, String password, long tenantId, LocalDateTime createdAt){
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

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public long getTenantId(){
        return tenantId;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

}