package com.happinesea.webcrawler.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

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

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SiteContentsRepositoryTest {

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

		contentsList.add(siteContentsRepository.save(s1));
		contentsList.add(siteContentsRepository.save(s2));
		contentsList.add(siteContentsRepository.save(s3));
		contentsList.add(siteContentsRepository.save(s4));
	}

	@Test
	void testFindAllByUrlIn() {
		List<String> urlList = new ArrayList<String>();
		urlList.add("http://url3");// target. status is none
		urlList.add("http://url4");// target. status is processing
		urlList.add("http://url5");// not target
		List<SiteContents> resultList = siteContentsRepository.findAllByUrlIn(urlList);
		assertEquals(2, resultList.size());
		assertTrue(resultList.contains(contentsList.get(2)));
		assertTrue(resultList.contains(contentsList.get(3)));

		assertFalse(resultList.contains(contentsList.get(0)));
		assertFalse(resultList.contains(contentsList.get(1)));
	}

	@Test
	void testFindContents4Post() {
		List<SiteContents> result1 = siteContentsRepository.findContents4Post(category2, ProcessStatus.NONE);
		assertEquals(0, result1.size());
		
		List<SiteContents> result2 = siteContentsRepository.findContents4Post(category1, ProcessStatus.NONE);
		assertEquals(1, result2.size());
		assertTrue(result2.contains(contentsList.get(0)));
		assertFalse(result2.contains(contentsList.get(1)));
		assertFalse(result2.contains(contentsList.get(2)));
		assertFalse(result2.contains(contentsList.get(3)));

	}

}
