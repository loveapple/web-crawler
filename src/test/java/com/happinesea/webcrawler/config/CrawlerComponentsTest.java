package com.happinesea.webcrawler.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.happinesea.webcrawler.Const.ContentsType;
import com.happinesea.webcrawler.Const.DeleteFlg;
import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteInfo;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteCategoryRepository;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;
import com.happinesea.webcrawler.repository.SiteInfoRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
class CrawlerComponentsTest {

    @Autowired
    private CrawlerComponents crawlerComponents;

    @Autowired
    private SiteInfoProcessRepository siteInfoProcessRepository;

    @Autowired
    private SiteCategoryRepository siteCategoryRepository;

    @Autowired
    private SiteInfoRepository siteInfoRepository;

    @BeforeEach
    void setUp() {
        SiteInfo siteInfo = new SiteInfo();
        siteInfo.setSiteName("Test Site");
        siteInfo.setSiteUrl("https://example.com");
        siteInfo.setContentsType(ContentsType.HTML);
        siteInfo.setDeleteFlg(DeleteFlg.OFF);
        siteInfo = siteInfoRepository.save(siteInfo);

        SiteCategory category = new SiteCategory();
        category.setSiteInfo(siteInfo);
        category.setCategoryName("News");
        category.setCategoryUrl("https://httpbin.org/get");
        category.setDeleteFlg(DeleteFlg.OFF);
        category = siteCategoryRepository.save(category);

        SiteInfoProcessPool process = new SiteInfoProcessPool();
        process.setSiteCategory(category);
        process.setProcessId("test-process");
        process.setProcessStatus(ProcessStatus.NONE);
        process.setProcessTime(LocalDateTime.now());
        process.setSiteInfoProcessId(1);
        siteInfoProcessRepository.save(process);
    }

    @Test
    void testSiteInfoProcessReader() {
//        ItemReader<SiteInfoProcessPool> reader = crawlerComponents.siteInfoProcessReader();
//        SiteInfoProcessPool process = null;
//        try {
//            process = reader.read();
//        } catch (Exception e) {
//            fail("Reader failed: " + e.getMessage());
//        }
//        assertNotNull(process);
//        assertEquals("https://httpbin.org/get", process.getSiteCategory().getCategoryUrl());
    }

    @Test
    void testSiteInfoProcessor() throws Exception {
        SiteInfoProcessPool input = siteInfoProcessRepository.findAll().get(0);
        ItemProcessor<SiteInfoProcessPool, SiteInfoProcessPool> processor = crawlerComponents.siteInfoProcessor();

        SiteInfoProcessPool result = processor.process(input);
        assertNotNull(result);
        assertTrue(EnumSet.of(ProcessStatus.SUCCESS, ProcessStatus.FAIL).contains(result.getProcessStatus()));
        assertNotNull(result.getProcessTime());
    }

    @Test
    void testSiteInfoProcessWriter() throws Exception {
        List<SiteInfoProcessPool> items = siteInfoProcessRepository.findAll();
        items.forEach(i -> {
            i.setProcessStatus(ProcessStatus.SUCCESS);
            i.setProcessTime(LocalDateTime.now());
        });

        ItemWriter<SiteInfoProcessPool> writer = crawlerComponents.siteInfoProcessWriter();

        // Spring Batch 5.x では Chunk でラップする必要がある
        writer.write(new Chunk<>(items));

        List<SiteInfoProcessPool> updated = siteInfoProcessRepository.findAll();
        assertEquals(ProcessStatus.SUCCESS, updated.get(0).getProcessStatus());
    }
}
