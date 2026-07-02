package com.Ram.shorter.repository;

import com.Ram.shorter.model.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    List<UrlEntity> findByFullUrl(String fullUrl);

}