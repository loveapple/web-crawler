package com.happinesea.webcrawler.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteContentsRepository;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SiteContentsService {
	@Autowired
	private SiteContentsRepository siteContentsRepository;
	@Autowired
	private SiteInfoProcessRepository siteInfoProcessRepository;
	@Value("${web-crawler.post-contents-limit-count}")
	private int postContentsLimitCount;

	public List<SiteInfoProcessPool> findAliveProcess() {
		return siteInfoProcessRepository.findByProcessStatusNot(ProcessStatus.PROCESSING);
	}

	@Transactional
	public SiteInfoProcessPool changSiteInfoProcess2Processing(SiteInfoProcessPool process) {
		process.setProcessStatus(ProcessStatus.PROCESSING);
		process.setProcessTime(LocalDateTime.now());
		return siteInfoProcessRepository.save(process);
	}

	@Transactional
	public SiteInfoProcessPool changSiteInfoProcess2Sucess(SiteInfoProcessPool process) {
		process.setProcessStatus(ProcessStatus.SUCCESS);
		process.setProcessTime(LocalDateTime.now());
		return siteInfoProcessRepository.save(process);

	}

	@Transactional
	public SiteInfoProcessPool changSiteInfoProcess2Fail(SiteInfoProcessPool process) {
		process.setProcessStatus(ProcessStatus.FAIL);
		process.setProcessTime(LocalDateTime.now());
		return siteInfoProcessRepository.save(process);

	}

	@Transactional
	public List<SiteInfoProcessPool> saveAllProcessPools(List<? extends SiteInfoProcessPool> pools) {

		List<SiteInfoProcessPool> result = new ArrayList<SiteInfoProcessPool>();
		for (SiteInfoProcessPool siteInfoProcessPool : pools) {
			if (ProcessStatus.PROCESSING.equals(siteInfoProcessPool.getProcessStatus())) {
				// TODO CMSにコンテンツ登録
				SiteCategory category = siteInfoProcessPool.getSiteCategory();
				List<SiteContents> contentsList = siteContentsRepository.findContents4Post(category,
						ProcessStatus.NONE);
				int limit = contentsList.size() < postContentsLimitCount ? contentsList.size() : postContentsLimitCount;
				for (int i = 0; i < limit; i++) {
					
				}

				log.debug("send cms  : " + result);
				// 処理後、成功状態更新
				result.add(changSiteInfoProcess2Sucess(siteInfoProcessPool));
			}
		}
		return result;
	}

	/**
	 * 重複しないデータだけを一括保存
	 * 
	 * @param newContents
	 */
	@Transactional
	public List<SiteContents> bulkInsertIfNotExists(List<SiteContents> newContents) {
		// 1. URLリスト抽出
		List<String> urls = newContents.stream().map(SiteContents::getUrl).toList();

		// 2. 既存データ取得
		List<String> existingUrls = siteContentsRepository.findAllByUrlIn(urls).stream().map(SiteContents::getUrl)
				.toList();

		// 3. 差分だけ抽出
		List<SiteContents> filtered = newContents.stream().filter(c -> !existingUrls.contains(c.getUrl())).toList();

		// 4. 一括保存 (saveAll → バルク insert)
		return siteContentsRepository.saveAll(filtered);
	}
}
