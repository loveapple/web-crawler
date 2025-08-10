package com.happinesea.webcrawler.repository;

import java.util.List;

import com.happinesea.webcrawler.dto.PostContentsResult;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.exception.PostFailedException;

public interface ContentsPostRepository {
	public SiteContents postContents(SiteContents contents) throws PostFailedException;
	public PostContentsResult postContents(List<SiteContents> contentsList) ;
}
