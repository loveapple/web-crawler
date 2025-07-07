package com.happinesea.webcrawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.happinesea.webcrawler.config.CrawlerComponents;
import com.happinesea.webcrawler.model.WebPage;

@SpringBootTest
@SpringBatchTest
class WebCrawlerApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private CrawlerComponents crawlerComponents;

	@Autowired
	private RestTemplate restTemplate;

	@Test
	void crawlJob_shouldCompleteSuccessfully() throws Exception {
		JobExecution execution = jobLauncherTestUtils.launchJob();
		assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
	}

	@Test
	void urlReader_shouldReturnUrls() throws Exception {
		List<String> urls = List.of("https://test1.com", "https://test2.com");
		Iterator<String> iterator = urls.iterator();
		var reader = (org.springframework.batch.item.ItemReader<String>) () -> iterator.hasNext() ? iterator.next()
				: null;

		assertThat(reader.read()).isEqualTo("https://test1.com");
		assertThat(reader.read()).isEqualTo("https://test2.com");
		assertThat(reader.read()).isNull();
	}

	@Test
	void webCrawlerProcessor_shouldFetchContent() throws Exception {
		var processor = crawlerComponents.webCrawlerProcessor(restTemplate);
		var result = processor.process("https://httpbin.org/get");
		assertThat(result.getUrl()).contains("httpbin.org");
		assertThat(result.getContent()).isNotEmpty();
	}

	@Test
	void webCrawlerProcessor_shouldHandleException() throws Exception {
		RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
		when(mockRestTemplate.getForObject(anyString(), Mockito.eq(String.class)))
				.thenThrow(new RestClientException("Simulated failure"));

		var processor = crawlerComponents.webCrawlerProcessor(mockRestTemplate);
		var result = processor.process("https://invalid-url");

		assertThat(result.getUrl()).isEqualTo("https://invalid-url");
		assertThat(result.getContent()).contains("[Error]");
	}

	@Test
	void webPageWriter_shouldPrintContent() throws Exception {
		var writer = crawlerComponents.webPageWriter();
		writer.write(Chunk.of(new WebPage("https://example.com", "Example content")));
	}

	@Test
	void contextLoads() {

	}

	@Test
	void testSpringApplicationRun() {
		try (ConfigurableApplicationContext context = WebCrawlerApplication.runApplication(new String[] {})) {
			// アプリケーションコンテキストが起動しているか
			assertThat(context).isNotNull();
			assertThat(context.isActive()).isTrue();

			// Bean が取得できるか確認
			assertThat(context.getBean(WebCrawlerApplication.class)).isNotNull();
		}
	}
}
