package com.happinesea.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SiteContents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long siteContentsId;

    private String url;
    @Lob
    private String title;
    @Lob
    private String contents;
    private Integer siteCategoyId;
    private String processStatus;
}
