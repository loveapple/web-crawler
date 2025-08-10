package com.happinesea.webcrawler.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.happinesea.webcrawler.dto.PostContentsResult;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.exception.PostFailedException;

import lombok.Data;
import lombok.Getter;

@Component
public class ContentsPostRepositoryImpl implements ContentsPostRepository {

	@Getter
	@Autowired
	private final HostInfo hostInfo;

	public ContentsPostRepositoryImpl(HostInfo hostInfo) {
		this.hostInfo = hostInfo;
	}
	@Override
	public SiteContents postContents(SiteContents contents) throws PostFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PostContentsResult postContents(List<SiteContents> contentsList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Data
	@Configuration
	@ConfigurationProperties(prefix = "web-crawler.host-info")
	public static class HostInfo {
		private String url;
		private String port;
		private String loginId;
		private String loginPw;
		private int postContentsLimitCount;
	}
}
