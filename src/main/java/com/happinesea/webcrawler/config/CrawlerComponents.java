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

        if (urls.isEmpty()) {
            throw new IllegalStateException("URLリストが空です。Crawlerを実行できません。");
        }

        Iterator<String> iterator = urls.iterator();
       // return () -> iterator.hasNext() ? iterator.next() : null;
        return () ->null;
    }

    @Bean
    public ItemProcessor<String, WebPage> webCrawlerProcessor(RestTemplate restTemplate) {
        return url -> {
        	// 取得内容URL列表，进行各个内容DB插入处理，如果出现异常，跳过进行下一步处理
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
    	// 最后写入处理
        return items -> items.forEach(page -> System.out.println("[Crawled] " + page));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}