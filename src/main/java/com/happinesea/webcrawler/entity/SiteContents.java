package com.happinesea.webcrawler.entity;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.converter.ProcessStatusConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "site_contents")
public class SiteContents {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "site_contents_id")
	private Long siteContentsId;

	@Column(name = "url")
	private String url;
	@Lob
	@Column(name = "title")
	private String title;
	@Lob
	@Column(name = "contents")
	private String contents;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_categoy_id", nullable = false)
	private SiteCategory siteCategory;

	@Column(name = "process_status")
	@Convert(converter = ProcessStatusConverter.class)
	private ProcessStatus processStatus;
}
