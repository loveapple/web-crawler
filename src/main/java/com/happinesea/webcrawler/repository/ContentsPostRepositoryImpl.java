package com.happinesea.webcrawler.repository;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.happinesea.webcrawler.dto.PostContentsResult;
import com.happinesea.webcrawler.entity.SiteContents;
import com.happinesea.webcrawler.exception.PostFailedException;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ContentsPostRepositoryImpl implements ContentsPostRepository {

    @Getter
    @Autowired
    private final HostInfo hostInfo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ContentsPostRepositoryImpl(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public SiteContents postContents(SiteContents contents) throws PostFailedException {
        try {
            // 准备认证头
            String credentials = hostInfo.getLoginId() + ":" + hostInfo.getLoginPw();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Basic " + encodedCredentials);
            
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("title", contents.getTitle());
            
            // 添加原文链接到内容末尾
            String contentWithSource = contents.getContents() + 
                "<p><em>原文地址: <a href=\"" + contents.getUrl() + "\">" + contents.getUrl() + "</a></em></p>";
            requestBody.put("content", contentWithSource);
            
            requestBody.put("status", "publish"); // 直接发布
            
            // 设置分类（如果有）
            if (contents.getSiteCategory() != null) {
                requestBody.putArray("categories").add(contents.getSiteCategory().getSiteCategoryId());
            }
            
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            
            // 构建API URL
            String apiUrl = buildWordPressApiUrl() + "/wp-json/wp/v2/posts";
            
            // 发送请求
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, request, JsonNode.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = response.getBody();
                log.info("Successfully posted content to WordPress. Post ID: {}", 
                         responseBody.get("id").asText());
                
                return contents;
            } else {
                throw new PostFailedException("Failed to post to WordPress. Status: " + 
                    response.getStatusCode(), null);
            }
        } catch (Exception e) {
            throw new PostFailedException("Error posting to WordPress: " + e.getMessage(), e);
        }
    }

    @Override
    public PostContentsResult postContents(List<SiteContents> contentsList) {
        if(CollectionUtils.isEmpty(contentsList)) {
            throw new IllegalArgumentException("Illegal contentList for post contents.");
        }
        
        PostContentsResult result = new PostContentsResult();
        int successCount = 0;
        int failureCount = 0;
        
        for (SiteContents siteContents : contentsList) {
            try {
                // 检查发布限制
                if (hostInfo.getPostContentsLimitCount() > 0 && 
                    successCount >= hostInfo.getPostContentsLimitCount()) {
                    log.info("Reached post limit ({}), skipping remaining contents.", 
                             hostInfo.getPostContentsLimitCount());
                    break;
                }
                
                SiteContents tmpContents = postContents(siteContents);
                result.addResult(tmpContents);
                successCount++;
                log.debug("Success post contents: {}", tmpContents.getUrl());
            } catch (Exception e) {
                result.addFailedResultList(siteContents);
                result.addFailedList(siteContents.getSiteContentsId(), e);
                failureCount++;
                log.error("Failed post contents: {}", siteContents.getUrl(), e);
            }
            
            // 添加短暂延迟，避免对WordPress服务器造成过大压力
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Posting thread interrupted", e);
            }
        }
        
        log.info("Posting completed. Success: {}, Failed: {}", successCount, failureCount);
        return result;
    }
    
    /**
     * 构建WordPress API基础URL
     */
    private String buildWordPressApiUrl() {
        String url = hostInfo.getUrl();
        if (hostInfo.getPort() != null && !hostInfo.getPort().isEmpty()) {
            url += ":" + hostInfo.getPort();
        }
        return url;
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "web-crawler.host-info")
    public static class HostInfo {
        private String url;
        private String port;
        private String loginId;
        private String loginPw;
        private int postContentsLimitCount;
    }
}