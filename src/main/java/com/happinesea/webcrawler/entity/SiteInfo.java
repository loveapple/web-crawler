package com.happinesea.webcrawler.entity;

import com.happinesea.webcrawler.Const.ContentsType;
import com.happinesea.webcrawler.Const.DeleteFlg;
import com.happinesea.webcrawler.entity.converter.ContentsTypeConverter;
import com.happinesea.webcrawler.entity.converter.DeleteFlgConverter;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	@Convert(converter = DeleteFlgConverter.class)
    private DeleteFlg deleteFlg;
	@Convert(converter = ContentsTypeConverter.class)
    private ContentsType contentsType;
}
