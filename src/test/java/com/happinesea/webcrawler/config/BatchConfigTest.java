package com.happinesea.webcrawler.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class BatchConfigTest {

    @Mock
    private SiteInfoProcessRepository siteInfoProcessRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private CrawlerComponents crawlerComponents;

    @Mock
    private ItemProcessor<SiteInfoProcessPool, SiteInfoProcessPool> processor;

    @Mock
    private ItemWriter<SiteInfoProcessPool> writer;

    private BatchConfig batchConfig;

    @BeforeEach
    void setUp() {
        batchConfig = new BatchConfig();
        ReflectionTestUtils.setField(batchConfig, "siteInfoProcessRepository", siteInfoProcessRepository);
    }

    @Test
    void testCrawlStep_returnsEmptyStepIfNoData() {
        when(siteInfoProcessRepository.findByProcessStatusNot(ProcessStatus.PROCESSING))
                .thenReturn(Collections.emptyList());

        Step step = batchConfig.crawlStep(jobRepository, transactionManager, crawlerComponents);

        assertNotNull(step);
        assertEquals("emptyStep", step.getName());
    }

    @Test
    void testCrawlStep_returnsNormalStepIfDataExists() {
        SiteInfoProcessPool p1 = new SiteInfoProcessPool();
        p1.setSiteInfoProcessId(1);
        SiteInfoProcessPool p2 = new SiteInfoProcessPool();
        p2.setSiteInfoProcessId(2);

        List<SiteInfoProcessPool> list = List.of(p1, p2);

        when(siteInfoProcessRepository.findByProcessStatusNot(ProcessStatus.PROCESSING))
                .thenReturn(list);
        when(crawlerComponents.siteInfoProcessor()).thenReturn(processor);
        when(crawlerComponents.siteInfoProcessWriter()).thenReturn(writer);

        Step step = batchConfig.crawlStep(jobRepository, transactionManager, crawlerComponents);

        assertNotNull(step);
        assertEquals("crawlStep", step.getName());
    }

    @Test
    void testCrawlJob_buildsJob() {
        Step step = new StepBuilder("mockStep", jobRepository)
                .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED, transactionManager)
                .build();

        Job job = batchConfig.crawlJob(jobRepository, step);

        assertNotNull(job);
        assertEquals("crawlJob", job.getName());
    }
}


