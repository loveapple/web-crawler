package com.happinesea.webcrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WebCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebCrawlerApplication.class, args);
	}

	// テスト用に runApplication メソッドを切り出し
	public static ConfigurableApplicationContext runApplication(String[] args) {
		return SpringApplication.run(WebCrawlerApplication.class, args);
	}
}
