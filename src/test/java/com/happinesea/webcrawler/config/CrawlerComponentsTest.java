package com.happinesea.webcrawler.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import com.happinesea.webcrawler.ContentsParser;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.entity.SiteInfo;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteCategoryRepository;
import com.happinesea.webcrawler.repository.SiteContentsRepository;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;
import com.happinesea.webcrawler.repository.SiteInfoRepository;
import com.happinesea.webcrawler.service.SiteContentsService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class CrawlerComponentsTest {

	@Mock
	private SiteContentsService siteContentsServiceMock;

	@Mock
	private SiteInfoProcessRepository siteInfoProcessRepository;

	@Mock
	private ContentsParser contentsParser;

	@Autowired
	private CrawlerComponents crawlerComponents;
	@Autowired
	private SiteInfoRepository siteInfoRepository;
	@Autowired
	private SiteCategoryRepository siteCategoryRepository;
	@Autowired
	private SiteContentsRepository siteContentsRepository;
	@Autowired
	private SiteContentsService siteContentsService;

	private SiteInfoProcessPool process;
	private SiteCategory category;
	private SiteContents contents;
	private List<SiteContents> contentsList;

	@BeforeEach
	void setup() {
		SiteInfo siteInfo = new SiteInfo();
		siteInfo.setSiteUrl("https://example.com");
		// IDはセットしない → DBが自動採番
		siteInfo = siteInfoRepository.save(siteInfo);

		category = new SiteCategory();
		category.setCategoryName("ニュース");
		category.setCategoryUrl("https://example.com/news");
		category.setSiteInfo(siteInfo);
		category = siteCategoryRepository.save(category);

		contents = new SiteContents();
		contents.setTitle("記事タイトル");
		contents.setUrl("https://example.com/article");
		contents.setSiteCategory(category);
		contents = siteContentsRepository.save(contents);

		process = new SiteInfoProcessPool();
		process.setSiteCategory(category);
		process.setSiteInfoProcessId(1); // これは必要なら
		// process = siteInfoProcessPoolRepository.save(process); // 必要に応じて保存

		contentsList = new ArrayList<>();
		contentsList.add(contents);

		crawlerComponents.setContentsParser(contentsParser);
		crawlerComponents.setSiteContentsService(siteContentsServiceMock);
	}

	@Test
	void testSiteInfoProcessor_shouldCallLoadMethodsAndUpdateStatus() throws Exception {

		when(contentsParser.loadCategoryContentsList(category)).thenReturn(List.of(contents));
		when(contentsParser.loadContents(contents)).thenReturn(contents);

		// Act
		SiteInfoProcessPool result = crawlerComponents.siteInfoProcessor().process(process);

		// Assert
		assertEquals(process, result);
		verify(contentsParser).loadCategoryContentsList(category);
		verify(contentsParser).loadContents(contents);
		verify(siteContentsServiceMock).changSiteInfoProcess2Processing(any(SiteInfoProcessPool.class));
	}

	@Test
	void testSiteInfoProcessor_shouldHandleExceptionAndSetFailStatus() throws Exception {
		when(contentsParser.loadCategoryContentsList(category)).thenThrow(new RuntimeException("読み取り失敗"));
		when(siteContentsServiceMock.changSiteInfoProcess2Fail(any())).thenReturn(process);

		// Act
		SiteInfoProcessPool result = crawlerComponents.siteInfoProcessor().process(process);

		// Assert
		assertEquals(process, result);
		verify(siteContentsServiceMock).changSiteInfoProcess2Processing(any(SiteInfoProcessPool.class));
		verify(siteContentsServiceMock).changSiteInfoProcess2Fail(any(SiteInfoProcessPool.class));
	}

	@Test
	void testSiteInfoProcessWriter_shouldInsertContentsAndUpdateStatus() throws Exception {

		process.setSiteCategory(category);
		ItemWriter<SiteInfoProcessPool> writer = crawlerComponents.siteInfoProcessWriter();

		// Act
		writer.write(new Chunk<>(List.of(process)));

		// Assert
	    verify(siteContentsServiceMock).saveAllProcessPools(List.of(process));
	}

	// TODO siteInfoProcessWriter失敗のテストは必要か？不明のため、一旦放置
//	@Test
//	void testSiteInfoProcessWriter_shouldHandleExceptionAndUpdateStatusToFail() throws Exception {
//		ItemWriter<SiteInfoProcessPool> writer = crawlerComponents.siteInfoProcessWriter();
//
//		SiteCategory dummyContents = new SiteCategory();
//		when(process.getSiteCategory()).thenReturn(dummyContents);
//
//		// bulkInsertIfNotExists を呼び出すと例外スロー
//		doThrow(new IllegalStateException("DB Error")).when(siteContentsServiceMock).bulkInsertIfNotExists(any());
//
//		// 実行
//		writer.write(new Chunk<>(List.of(process)));
//
//		// 検証
//		verify(siteContentsServiceMock).bulkInsertIfNotExists(any());
//		verify(siteContentsServiceMock).changSiteInfoProcess2Fail(any(SiteInfoProcessPool.class));
//	}

//	@Test
//	void testParallelExecution_withMultipleThreads() throws Exception {
//		PlatformTransactionManager transactionManager = new ResourcelessTransactionManager();
//
//		// H2 組み込みDBを用意
//		DataSource dataSource = new EmbeddedDatabaseBuilder()
//				.setType(EmbeddedDatabaseType.H2)
//				.addScript(BatchDatabaseInitializer.DEFAULT_SCHEMA) // Spring Batchのテーブルスキーマ
//				.build();
//
//		// JobRepository を初期化
//		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//		factory.setDataSource(dataSource); // これを忘れるとエラーになる
//		factory.setTransactionManager(transactionManager);
//		factory.afterPropertiesSet();
//		JobRepository jobRepository = factory.getObject();
//
//		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
//
//		// テストデータ
//		List<SiteInfoProcessPool> items = new ArrayList<>();
//		for (int i = 0; i < 10; i++) {
//			SiteInfoProcessPool item = new SiteInfoProcessPool();
//			item.setProcessId(String.valueOf(i));
//			items.add(item);
//		}
//
//		// Processor
//		ItemProcessor<SiteInfoProcessPool, SiteInfoProcessPool> processor = item -> {
//			System.out.println(Thread.currentThread().getName() + " processing ID = " + item.getProcessId());
//			return item;
//		};
//
//		// Writer
//		ItemWriter<SiteInfoProcessPool> writer = list -> {
//			for (SiteInfoProcessPool item : list) {
//				System.out.println(Thread.currentThread().getName() + " writing ID = " + item.getProcessId());
//			}
//		};
//
//		// Executor
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(3);
//		executor.setMaxPoolSize(3);
//		executor.setQueueCapacity(0);
//		executor.setThreadNamePrefix("test-thread-");
//		executor.initialize();
//
//		// Reader
//		ListItemReader<SiteInfoProcessPool> listReader = new ListItemReader<>(items);
//		SynchronizedItemStreamReader<SiteInfoProcessPool> reader = new SynchronizedItemStreamReader<>();
//		reader.setDelegate((ItemStreamReader<SiteInfoProcessPool>) listReader); // これでコンパイルOK
//
//
//		Step step = new StepBuilder("testStep", jobRepository)
//				.<SiteInfoProcessPool, SiteInfoProcessPool>chunk(2, transactionManager).reader(reader)
//				.processor(processor).writer(writer).taskExecutor(executor).build(); // throttleLimit は不要
//
//		// 実行
//		step.execute(stepExecution);
//
//		System.out.println("Completed all steps.");
//	}

}
