package com.happinesea.webcrawler.repository;

import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;

import java.util.List;
import java.util.Queue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteContentsRepository extends JpaRepository<SiteContents, Long> {
	/**
	 * 添加抓取内容
	 * @param contents
	 * @param category
	 * @return
	 */
	//@Query("")
	//public int insertContents(SiteContents contents, SiteCategory category);
	
	/**
	 * 搜索投放内容数据
	 * @param category
	 * @return
	 */
	//@Query("")
	//public List<SiteContents> findContents4Post(SiteCategory category);
}
