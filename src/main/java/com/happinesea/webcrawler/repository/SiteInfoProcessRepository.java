package com.happinesea.webcrawler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;

@Repository
public interface SiteInfoProcessRepository extends JpaRepository<SiteInfoProcessPool, Integer> {
	List<SiteInfoProcessPool> findByProcessStatusNot(ProcessStatus status);
}
