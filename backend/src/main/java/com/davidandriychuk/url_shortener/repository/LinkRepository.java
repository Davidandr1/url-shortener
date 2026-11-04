package com.davidandriychuk.url_shortener.repository;

import com.davidandriychuk.url_shortener.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findByUserId(Long userId);

}