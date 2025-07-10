package com.happinesea.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class SiteCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer siteCategoryId;

    private Integer siteInfoId;
    private String categoryName;
    private String categoryUrl;
    private String categoryListUrl;
    private LocalDateTime lastScanTime;
    private String targetCategory;
    private String listRecordSelectId;
    private String titleRecordSelectId;
    private String contentsUrlSelectId;
    private String bodySelectId;
    private String moreBodySelectId;
}
