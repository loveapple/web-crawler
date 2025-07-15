package com.happinesea.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.converter.ProcessStatusConverter;

@Entity
@Getter
@Setter
@Table(name = "site_info_process_pool")
public class SiteInfoProcessPool {
    @Id
	@Column(name = "site_info_process_id")
    private Integer siteInfoProcessId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_category_id", nullable = false)
	private SiteCategory siteCategory;

	@Column(name = "process_status")
	@Convert(converter = ProcessStatusConverter.class)
	private ProcessStatus processStatus;
	
	@Column(name = "process_id")
    private String processId;
	
	@Column(name = "process_time")
    private LocalDateTime processTime;
}
