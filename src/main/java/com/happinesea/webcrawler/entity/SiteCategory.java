package com.happinesea.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "site_category")
public class SiteCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "site_category_id")
	private Integer siteCategoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_info_id", nullable = false)
	private SiteInfo siteInfo;

	@Column(name = "category_name")
	private String categoryName;

	@Column(name = "delete_flg")
	private String deleteFlg;

	@Column(name = "category_url")
	private String categoryUrl;

	@Column(name = "category_list_url")
	private String categoryListUrl;

	@Column(name = "last_scan_time")
	private LocalDateTime lastScanTime;

	@Column(name = "target_category")
	private String targetCategory;

	@Column(name = "list_record_select_id")
	private String listRecordSelectId;

	@Column(name = "title_record_select_id")
	private String titleRecordSelectId;

	@Column(name = "contents_url_selectId")
	private String contentsUrlSelectId;

	@Column(name = "body_select_id")
	private String bodySelectId;

	@Column(name = "more_body_select_id")
	private String moreBodySelectId;

}
