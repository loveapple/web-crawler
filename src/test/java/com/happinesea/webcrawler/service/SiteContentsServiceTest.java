package com.happinesea.webcrawler.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.entity.SiteInfo;
import com.happinesea.webcrawler.repository.SiteCategoryRepository;
import com.happinesea.webcrawler.repository.SiteContentsRepository;
import com.happinesea.webcrawler.repository.SiteInfoRepository;

@SpringBootTest
@ActiveProfiles("test")
class SiteContentsServiceTest {

	@Autowired
	private SiteContentsService siteContentsService;

	@Autowired
	private SiteContentsRepository siteContentsRepository;

	@Autowired
	private SiteCategoryRepository siteCategoryRepository;

	@Autowired
	private SiteInfoRepository siteInfoRepository;
	
	List<SiteContents> contentsList;

	private SiteCategory category1;

	private SiteCategory category2;

	@BeforeEach
	void setUp() throws Exception {
		contentsList = new ArrayList<SiteContents>();

		SiteInfo site = new SiteInfo();
		category1 = new SiteCategory();
		category1.setSiteInfo(siteInfoRepository.save(site));
		category1 = siteCategoryRepository.save(category1);

		category2 = new SiteCategory();
		category2.setSiteInfo(siteInfoRepository.save(site));
		siteCategoryRepository.save(category2);

		SiteContents s1 = new SiteContents();
		s1.setProcessStatus(ProcessStatus.NONE);
		s1.setUrl("http://url1");
		s1.setSiteCategory(category1);

		SiteContents s2 = new SiteContents();
		s2.setProcessStatus(ProcessStatus.PROCESSING);
		s2.setUrl("http://url2");
		s2.setSiteCategory(category1);

		SiteContents s3 = new SiteContents();
		s3.setProcessStatus(ProcessStatus.SUCCESS);
		s3.setUrl("http://url3");
		s3.setSiteCategory(category1);

		SiteContents s4 = new SiteContents();
		s4.setProcessStatus(ProcessStatus.PROCESSING);
		s4.setUrl("http://url4");
		s4.setSiteCategory(category1);

		contentsList.add(s1);
		contentsList.add(s2);
		contentsList.add(s3);
		contentsList.add(s4);

	}

	@Test
	void testBulkInsertIfNotExists() {
		siteContentsService.bulkInsertIfNotExists(contentsList);

		List<SiteContents> result = siteContentsRepository.findAll();
		
		assertEquals(4, result.size());
	}

}
