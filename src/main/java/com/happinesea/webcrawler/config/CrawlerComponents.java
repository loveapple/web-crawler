package com.happinesea.webcrawler.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.SynchronizedItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.ContentsParser;
import com.happinesea.webcrawler.NotFoundContentsException;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.service.SiteContentsService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Data
public class CrawlerComponents {

	@Autowired
	private SiteContentsService siteContentsService;

	@Autowired
	private ContentsParser contentsParser;

	@Bean
	public ItemReader<SiteInfoProcessPool> siteInfoProcessReader() {
		List<SiteInfoProcessPool> aliveList = siteContentsService.findAliveProcess();
		return new SynchronizedItemReader<>(new ListItemReader<>(aliveList));
	}

	@Bean
	public ItemProcessor<SiteInfoProcessPool, SiteInfoProcessPool> siteInfoProcessor() {
		return process -> {
			siteContentsService.changSiteInfoProcess2Processing(process); // 状態を更新

			try {
				SiteCategory category = process.getSiteCategory();

				// コンテンツ一覧を取得
				List<SiteContents> contentsList = contentsParser.loadCategoryContentsList(category);
				if (contentsList == null || contentsList.isEmpty()) {
					log.warn("No contents found for category: {}", category.getCategoryUrl());
					siteContentsService.changSiteInfoProcess2Fail(process);
					return process;
				}

				// 各コンテンツの詳細を取得
				List<SiteContents> loadedContents = new ArrayList<>();
				for (SiteContents contents : contentsList) {
					try {
						SiteContents fullContents = contentsParser.loadContents(contents);
						loadedContents.add(fullContents);
					} catch (NotFoundContentsException e) {
						log.warn("Content not found: {}", contents.getUrl(), e);
					}
				}

				// DBに保存（重複除外）
				siteContentsService.bulkInsertIfNotExists(loadedContents);
				return process;
			} catch (Exception e) {
				log.warn("Error processing siteInfoProcessPool: {}", process.getSiteInfoProcessId(), e);
				return siteContentsService.changSiteInfoProcess2Fail(process);
			}
		};
	}

	@Bean
	public ItemWriter<SiteInfoProcessPool> siteInfoProcessWriter() {
		return chunk -> siteContentsService.saveAllProcessPools(chunk.getItems());
	}
}