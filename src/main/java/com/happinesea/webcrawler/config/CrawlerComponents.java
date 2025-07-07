package com.happinesea.webcrawler.config;


import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.happinesea.webcrawler.model.WebPage;

@Configuration
public class CrawlerComponents {

    @Bean
    public ItemReader<String> urlReader() {
        List<String> urls = List.of(
                "https://httpbin.org/get",
                "https://httpbin.org/headers"
        );
        Iterator<String> iterator = urls.iterator();
        return () -> iterator.hasNext() ? iterator.next() : null;
    }

    @Bean
    public ItemProcessor<String, WebPage> webCrawlerProcessor(RestTemplate restTemplate) {
        return url -> {
            try {
                String content = restTemplate.getForObject(url, String.class);
                return new WebPage(url, content);
            } catch (Exception e) {
                return new WebPage(url, "[Error] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        };
    }

    @Bean
    public ItemWriter<WebPage> webPageWriter() {
        return items -> items.forEach(page -> System.out.println("[Crawled] " + page));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}