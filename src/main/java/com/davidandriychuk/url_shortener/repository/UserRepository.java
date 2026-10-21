package com.davidandriychuk.url_shortener.repository;

import com.davidandriychuk.url_shortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}