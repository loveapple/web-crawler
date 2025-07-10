package com.happinesea.webcrawler.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;

@Repository
public interface SiteContentsRepository extends JpaRepository<SiteContents, Long> {
	/**
	 * 添加抓取内容
	 * @param contents
	 * @param category
	 * @return
	 */
	List<SiteContents> findAllByUrlIn(Collection<String> urls);
	
	/**
	 * 搜索投放内容数据
	 * @param category
	 * @return
	 */
	@Query("select sc from SiteContents sc where sc.siteCategory = :category and sc.processStatus = :status")
	public List<SiteContents> findContents4Post(@Param("category") SiteCategory category,@Param("status")  String status);
}
