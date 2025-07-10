package com.happinesea.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SiteInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer siteInfoId;

    private String siteName;
    private String siteUrl;
    private String logoUrl;
    private String deleteFlg;
    private String contentsType;
}
