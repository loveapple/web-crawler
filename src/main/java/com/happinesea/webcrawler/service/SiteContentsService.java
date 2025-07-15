package com.happinesea.webcrawler.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.repository.SiteContentsRepository;

import jakarta.transaction.Transactional;

@Service
public class SiteContentsService {
	@Autowired
    private SiteContentsRepository repository;
	
	/**
	 * 重複しないデータだけを一括保存
	 * 
	 * @param newContents
	 */
    @Transactional
    public void bulkInsertIfNotExists(List<SiteContents> newContents) {
        // 1. URLリスト抽出
        List<String> urls = newContents.stream()
                .map(SiteContents::getUrl)
                .toList();

        // 2. 既存データ取得
        List<String> existingUrls = repository.findAllByUrlIn(urls).stream()
                .map(SiteContents::getUrl)
                .toList();

        // 3. 差分だけ抽出
        List<SiteContents> filtered = newContents.stream()
                .filter(c -> !existingUrls.contains(c.getUrl()))
                .toList();

        // 4. 一括保存 (saveAll → バルク insert)
        repository.saveAll(filtered);
    }
}
