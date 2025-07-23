package com.happinesea.webcrawler.config;

import java.time.LocalDateTime;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class CrawlerComponents {

	@Autowired
	private SiteInfoProcessRepository siteInfoProcessRepository;

	// @Autowired
	// private RestTemplate restTemplate;

	@Bean
	public ItemReader<SiteInfoProcessPool> siteInfoProcessReader() {
		return new ListItemReader<>(siteInfoProcessRepository.findByProcessStatusNot(ProcessStatus.PROCESSING));
	}

	@Bean
	public ItemProcessor<SiteInfoProcessPool, SiteInfoProcessPool> siteInfoProcessor() {
		return process -> {
			process.setProcessStatus(ProcessStatus.PROCESSING);
			siteInfoProcessRepository.save(process); // 状態をPROCESSINGに更新

			try {
				SiteCategory category = process.getSiteCategory();
				Document doc = Jsoup.connect(category.getCategoryUrl()).get();
				// TODO: content を使った実処理
				process.setProcessStatus(ProcessStatus.SUCCESS);
			} catch (Exception e) {
				process.setProcessStatus(ProcessStatus.FAIL);
			}
			process.setProcessTime(LocalDateTime.now());
			return process;
		};
	}

	@Bean
	public ItemWriter<SiteInfoProcessPool> siteInfoProcessWriter() {
		return chunk -> siteInfoProcessRepository.saveAll(chunk.getItems()); // ←修正ポイント
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}