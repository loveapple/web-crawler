package com.happinesea.webcrawler.model;

import lombok.Data;

@Data
public class WebPage {
    private final String url;
    private final String content;

    public WebPage(String url, String content) {
        this.url = url;
        this.content = content;
    }

    @Override
    public String toString() {
        return "WebPage{" + "url='" + url + '\'' + ", content='" + content + '\'' + '}';
    }
}