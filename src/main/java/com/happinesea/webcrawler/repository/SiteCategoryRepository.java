package com.happinesea.webcrawler.repository;

import com.happinesea.webcrawler.entity.SiteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteCategoryRepository extends JpaRepository<SiteCategory, Integer> {
}
