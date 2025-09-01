package com.happinesea.webcrawler.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.dto.PostContentsResult;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.ContentsPostRepository;
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
	@Autowired
	private ContentsPostRepository contentsPostRepository;

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
	    List<SiteInfoProcessPool> result = new ArrayList<>();

	    for (SiteInfoProcessPool pool : pools) {
	        if (ProcessStatus.PROCESSING.equals(pool.getProcessStatus())) {
	            // 处理完成后更新状态
	            pool.setProcessStatus(ProcessStatus.SUCCESS);
	            pool.setProcessTime(LocalDateTime.now());

	            // 发布相关内容
	            List<SiteContents> contentsList = siteContentsRepository.findContents4Post(pool.getSiteCategory(),
	                    ProcessStatus.NONE);

	            if (!CollectionUtils.isEmpty(contentsList)) {
	                PostContentsResult postResult = contentsPostRepository.postContents(contentsList);

	                postResult.getResultList().forEach(this::changeStatus4Sucess);
	                postResult.getFailedResultList().forEach(this::changeStatus4Fail);

	                log.info("Posted {} contents for category {}, {} succeeded, {} failed", contentsList.size(),
	                        pool.getSiteCategory().getCategoryName(), postResult.getResultList().size(),
	                        postResult.getFailedResultList().size());
	            }

	            result.add(siteInfoProcessRepository.save(pool));
	        }
	    }
	    return result;
	}

	private SiteContents changeStatus4Sucess(SiteContents contents) {
		contents.setProcessStatus(ProcessStatus.SUCCESS);
		return siteContentsRepository.save(contents);
	}

	private SiteContents changeStatus4Fail(SiteContents contents) {
		contents.setProcessStatus(ProcessStatus.FAIL);
		return siteContentsRepository.save(contents);
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
