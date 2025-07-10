package com.happinesea.webcrawler.repository;

import com.happinesea.webcrawler.entity.SiteInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteInfoRepository extends JpaRepository<SiteInfo, Integer> {
}
