package com.happinesea.webcrawler.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.happinesea.webcrawler.entity.SiteContents;

import lombok.Data;

@Data
public class PostContentsResult {
	private List<SiteContents> resultList;
	private List<SiteContents> failedResultList;
	private Map<Long, Exception> failedList;

	public PostContentsResult() {
		resultList = new ArrayList<>();
		failedResultList = new ArrayList<>();
		failedList = new HashMap<>();
	}

	public boolean addResult(SiteContents contents) {
		return this.resultList.add(contents);
	}

	public boolean addFailedResultList(SiteContents contents) {
		return this.failedResultList.add(contents);
	}

	public Exception addFailedList(Long id, Exception ex) {
		return this.failedList.put(id, ex);
	}
}
