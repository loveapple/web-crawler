package com.happinesea.webcrawler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.dto.PostContentsResult;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.entity.SiteInfo;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.ContentsPostRepository;
import com.happinesea.webcrawler.repository.SiteCategoryRepository;
import com.happinesea.webcrawler.repository.SiteContentsRepository;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;
import com.happinesea.webcrawler.repository.SiteInfoRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @Mock
    private ContentsPostRepository contentsPostRepository;

    List<SiteContents> contentsList;

    private SiteCategory category1;

    private SiteCategory category2;

    private SiteInfoProcessPool pool;

    @BeforeEach
    void setUp() throws Exception {
        contentsList = new ArrayList<SiteContents>();

        SiteInfo site = new SiteInfo();
        site.setSiteName("Test Site");
        site.setSiteUrl("http://test.com");
        site = siteInfoRepository.save(site);

        category1 = new SiteCategory();
        category1.setCategoryName("name1");
        category1.setSiteInfo(site);
        category1 = siteCategoryRepository.save(category1);

        category2 = new SiteCategory();
        category2.setCategoryName("name2");
        category2.setSiteInfo(site);
        category2 = siteCategoryRepository.save(category2);

        SiteContents s1 = new SiteContents();
        s1.setProcessStatus(ProcessStatus.NONE);
        s1.setUrl("http://url1");
        s1.setTitle("Title 1");
        s1.setContents("Content 1");
        s1.setSiteCategory(category1);

        SiteContents s2 = new SiteContents();
        s2.setProcessStatus(ProcessStatus.PROCESSING);
        s2.setUrl("http://url2");
        s2.setTitle("Title 2");
        s2.setContents("Content 2");
        s2.setSiteCategory(category1);

        SiteContents s3 = new SiteContents();
        s3.setProcessStatus(ProcessStatus.SUCCESS);
        s3.setUrl("http://url3");
        s3.setTitle("Title 3");
        s3.setContents("Content 3");
        s3.setSiteCategory(category1);

        SiteContents s4 = new SiteContents();
        s4.setProcessStatus(ProcessStatus.PROCESSING);
        s4.setUrl("http://url4");
        s4.setTitle("Title 4");
        s4.setContents("Content 4");
        s4.setSiteCategory(category1);

        s1 = siteContentsRepository.save(s1);
        s2 = siteContentsRepository.save(s2);
        s3 = siteContentsRepository.save(s3);
        s4 = siteContentsRepository.save(s4);

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

        // 手动注入 Mock
        ReflectionTestUtils.setField(siteContentsService, "contentsPostRepository", contentsPostRepository);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        siteInfoProcessRepository.deleteAll();
        siteContentsRepository.deleteAll();
        siteCategoryRepository.deleteAll();
        siteInfoRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testBulkInsertIfNotExists() {
        siteContentsService.bulkInsertIfNotExists(contentsList);
        List<SiteContents> result = siteContentsRepository.findAll();
        assertEquals(4, result.size());
    }

    @Test
    @Order(2)
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
    @Order(3)
    void testChangSiteInfoProcess2Processing() {
        SiteInfoProcessPool result = siteContentsService.changSiteInfoProcess2Processing(pool);
        assertEquals(ProcessStatus.PROCESSING, result.getProcessStatus());
    }

    @Test
    @Order(4)
    void testChangSiteInfoProcess2Sucess() {
        SiteInfoProcessPool result = siteContentsService.changSiteInfoProcess2Sucess(pool);
        assertEquals(ProcessStatus.SUCCESS, result.getProcessStatus());
    }

    @Test
    @Order(5)
    void testChangSiteInfoProcess2Fail() {
        SiteInfoProcessPool result = siteContentsService.changSiteInfoProcess2Fail(pool);
        assertEquals(ProcessStatus.FAIL, result.getProcessStatus());
    }

    @Test
    @Order(6)
    void testSaveAllProcessPools() {
        // 创建一个 PROCESSING 状态的 pool
        SiteInfoProcessPool processingPool = new SiteInfoProcessPool();
        processingPool.setSiteInfoProcessId(2);
        processingPool.setProcessStatus(ProcessStatus.PROCESSING);
        processingPool.setProcessTime(LocalDateTime.now());
        processingPool.setSiteCategory(category1);
        processingPool = siteInfoProcessRepository.save(processingPool);

        List<SiteInfoProcessPool> p = new ArrayList<SiteInfoProcessPool>();
        p.add(pool); // 这个 pool 的状态是 NONE，不会被处理
        p.add(processingPool); // 这个 pool 的状态是 PROCESSING，会被处理

        PostContentsResult postResult = new PostContentsResult();
        List<SiteContents> resultList = new ArrayList<>();
        resultList.add(contentsList.get(0));
        List<SiteContents> failList = new ArrayList<>();
        failList.add(contentsList.get(1));
        postResult.setResultList(resultList);
        postResult.setFailedResultList(failList);
        
        // 设置 Mock stubbing
        when(contentsPostRepository.postContents(anyList())).thenReturn(postResult);

        List<SiteInfoProcessPool> result = siteContentsService.saveAllProcessPools(p);

        assertEquals(1, result.size());
        assertEquals(ProcessStatus.SUCCESS, result.get(0).getProcessStatus());
    }

    @Test
    @Order(7)
    void testSaveAllProcessPools_updatesProcessStatusCorrectly() {
        // 创建一个 PROCESSING 状态的 pool
        SiteInfoProcessPool processingPool = new SiteInfoProcessPool();
        processingPool.setSiteInfoProcessId(3);
        processingPool.setProcessStatus(ProcessStatus.PROCESSING);
        processingPool.setProcessTime(LocalDateTime.now());
        processingPool.setSiteCategory(category1);
        processingPool = siteInfoProcessRepository.save(processingPool);

        PostContentsResult postResult = new PostContentsResult();
        List<SiteContents> resultList = new ArrayList<>();
        resultList.add(contentsList.get(0));
        List<SiteContents> failList = new ArrayList<>();
        failList.add(contentsList.get(1));
        postResult.setResultList(resultList);
        postResult.setFailedResultList(failList);

        // 设置 Mock stubbing
        when(contentsPostRepository.postContents(anyList())).thenReturn(postResult);

        // Act
        List<SiteInfoProcessPool> result = siteContentsService.saveAllProcessPools(List.of(processingPool));

        // Assert
        assertEquals(1, result.size());
        assertEquals(ProcessStatus.SUCCESS, result.get(0).getProcessStatus());
    }

    @Test
    @Order(8)
    void testSaveAllProcessPools_noContentsToPost() {
        // 创建一个 PROCESSING 状态的 pool，但使用 category2（没有内容）
        SiteInfoProcessPool processingPool = new SiteInfoProcessPool();
        processingPool.setSiteInfoProcessId(4);
        processingPool.setProcessStatus(ProcessStatus.PROCESSING);
        processingPool.setProcessTime(LocalDateTime.now());
        processingPool.setSiteCategory(category2);
        processingPool = siteInfoProcessRepository.save(processingPool);

        // 不需要设置 Mock stubbing，因为 contentsPostRepository.postContents 不会被调用

        // Act
        List<SiteInfoProcessPool> result = siteContentsService.saveAllProcessPools(List.of(processingPool));

        // Assert
        assertEquals(1, result.size());
        assertEquals(ProcessStatus.SUCCESS, result.get(0).getProcessStatus());
    }
}