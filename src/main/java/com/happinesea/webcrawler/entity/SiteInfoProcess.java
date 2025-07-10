package com.happinesea.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class SiteInfoProcess {
    @Id
    private Integer siteInfoProcessId;

    private Integer siteCategoryId;
    private String processStatus;
    private String processId;
    private LocalDateTime processTime;
}
