package com.happinesea.webcrawler.repository;

import com.happinesea.webcrawler.entity.SiteContents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteContentsRepository extends JpaRepository<SiteContents, Long> {
}
