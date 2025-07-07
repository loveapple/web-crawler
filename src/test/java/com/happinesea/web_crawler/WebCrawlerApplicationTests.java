package com.happinesea.web_crawler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class WebCrawlerApplicationTests {

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
