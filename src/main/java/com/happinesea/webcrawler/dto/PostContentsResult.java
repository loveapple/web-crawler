package com.happinesea.webcrawler.dto;

import java.util.List;
import java.util.Map;

import com.happinesea.webcrawler.entity.SiteContents;

import lombok.Data;

@Data
public class PostContentsResult {
	private List<SiteContents> resultList;
	private List<SiteContents> failedResultList;
	private Map<Long, Exception> failedList;
}
