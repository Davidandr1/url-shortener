package com.davidandriychuk.url_shortener.repository;

import com.davidandriychuk.url_shortener.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

}