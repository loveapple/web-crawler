package com.happinesea.webcrawler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
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
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteCategoryRepository;
import com.happinesea.webcrawler.repository.SiteContentsRepository;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;
import com.happinesea.webcrawler.repository.SiteInfoRepository;

import jakarta.transaction.Transactional;

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

	@Autowired
	private SiteInfoProcessRepository siteInfoProcessRepository;

	List<SiteContents> contentsList;

	private SiteCategory category1;

	private SiteCategory category2;

	private SiteInfoProcessPool pool;

	@BeforeEach
	void setUp() throws Exception {
		contentsList = new ArrayList<SiteContents>();

		SiteInfo site = new SiteInfo();
		category1 = new SiteCategory();
		category1.setCategoryName("name1");
		category1.setSiteInfo(siteInfoRepository.save(site));
		category1 = siteCategoryRepository.save(category1);

		category2 = new SiteCategory();
		category2.setCategoryName("name2");
		category2.setSiteInfo(siteInfoRepository.save(site));
		category2 = siteCategoryRepository.save(category2);

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

		pool = new SiteInfoProcessPool();
		pool.setSiteInfoProcessId(1);
		pool.setProcessStatus(ProcessStatus.NONE);
		pool.setProcessTime(LocalDateTime.now());
		pool.setSiteCategory(category1);

		pool = siteInfoProcessRepository.save(pool);
	}

	@Test
	void testBulkInsertIfNotExists() {
		siteContentsService.bulkInsertIfNotExists(contentsList);

		List<SiteContents> result = siteContentsRepository.findAll();

		assertEquals(4, result.size());
	}

	@Test
	@Transactional
	void testFindAliveProcess() {
		SiteInfoProcessPool pool2 = new SiteInfoProcessPool();
		pool2.setSiteInfoProcessId(2);
		pool2.setProcessStatus(ProcessStatus.PROCESSING);
		pool2.setProcessTime(LocalDateTime.now());
		pool2.setSiteCategory(category2);
		siteInfoProcessRepository.save(pool2);

		List<SiteInfoProcessPool> result = siteContentsService.findAliveProcess();

		assertEquals(1, result.size());
		SiteInfoProcessPool v = result.get(0);
		assertEquals(1, v.getSiteInfoProcessId());
		assertEquals(ProcessStatus.NONE, v.getProcessStatus());
		assertEquals("name1", v.getSiteCategory().getCategoryName());
	}

	@Test
	void testChangSiteInfoProcess2Processing() {
		SiteInfoProcessPool result = siteContentsService.changSiteInfoProcess2Processing(pool);
		assertEquals(ProcessStatus.PROCESSING, result.getProcessStatus());
	}

	@Test
	void testChangSiteInfoProcess2Sucess() {
		SiteInfoProcessPool result = siteContentsService.changSiteInfoProcess2Sucess(pool);
		assertEquals(ProcessStatus.SUCCESS, result.getProcessStatus());

	}

	@Test
	void testChangSiteInfoProcess2Fail() {
		SiteInfoProcessPool result = siteContentsService.changSiteInfoProcess2Fail(pool);
		assertEquals(ProcessStatus.FAIL, result.getProcessStatus());

	}

	@Test
	void testSaveAllProcessPools() {
		SiteInfoProcessPool pool2 = new SiteInfoProcessPool();
		pool2.setSiteInfoProcessId(2);
		pool2.setProcessStatus(ProcessStatus.PROCESSING);
		pool2.setProcessTime(LocalDateTime.now());
		pool2.setSiteCategory(category2);
		pool2 = siteInfoProcessRepository.save(pool2);
		
		List<SiteInfoProcessPool> p = new ArrayList<SiteInfoProcessPool>();
		p.add(pool);
		p.add(pool2);
		
		List<SiteInfoProcessPool> result = siteContentsService.saveAllProcessPools(p);
		
		assertEquals(1, result.size());
		assertEquals(ProcessStatus.SUCCESS, result.get(0).getProcessStatus());
		
	}

}
