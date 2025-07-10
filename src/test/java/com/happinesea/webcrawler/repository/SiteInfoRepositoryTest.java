package com.happinesea.webcrawler.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.happinesea.webcrawler.entity.SiteInfo;

@SpringBootTest
@ActiveProfiles("test")
class SiteInfoRepositoryTest {

    @Autowired
    private SiteInfoRepository repository;

    @Test
    void saveAndFind() {
        var entity = new SiteInfo();
        entity.setSiteName("Test Site");
        entity.setSiteUrl("https://example.com");
        entity.setDeleteFlg("0");
        entity.setContentsType("1");

        repository.save(entity);

        var result = repository.findAll();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getSiteName()).isEqualTo("Test Site");
    }
}
