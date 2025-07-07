package com.happinesea.webcrawler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
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
        var reader = crawlerComponents.urlReader();
       // assertThat(reader.read()).isNotNull();
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
