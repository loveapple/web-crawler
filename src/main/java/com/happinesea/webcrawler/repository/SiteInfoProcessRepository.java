package com.happinesea.webcrawler.repository;

import com.happinesea.webcrawler.entity.SiteInfo;
import com.happinesea.webcrawler.entity.SiteInfoProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteInfoProcessRepository extends JpaRepository<SiteInfoProcess, Integer> {
	/**
	 * 更新目标网站开始处理状态
	 * @param siteInfo
	 * @return
	 */
	//@Query("SELECT s FROM SiteInfo s WHERE s.deleteFlg = '0'")
	//public int updateStatus2ProcessSite(SiteInfo siteInfo);
}
