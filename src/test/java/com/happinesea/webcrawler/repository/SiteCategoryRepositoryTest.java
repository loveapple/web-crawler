package com.happinesea.webcrawler.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteInfo;

@SpringBootTest
@ActiveProfiles("test")
class SiteCategoryRepositoryTest {

    @Autowired
    private SiteInfoRepository siteInfoRepository;
    @Autowired
    private SiteCategoryRepository categoryRepository;
	@Test
	void testFindActiveCategoryBySiteInfoId() {

        var entity1 = new SiteInfo();
        entity1.setSiteName("Test Site");
        entity1.setSiteUrl("https://example.com");
        entity1.setDeleteFlg("0");
        entity1.setContentsType("1");
        var entity2 = new SiteInfo();
        entity2.setSiteName("Test Site2");
        entity2.setSiteUrl("https://example2.com");
        entity2.setDeleteFlg("1");
        entity2.setContentsType("1");

        siteInfoRepository.save(entity1);
        siteInfoRepository.save(entity2);
        
        var category1 = new SiteCategory();
        category1.setSiteInfo(entity1);
        category1.setCategoryName("test1");
        category1.setDeleteFlg("0");        
        var category2 = new SiteCategory();
        category2.setSiteInfo(entity1);
        category2.setCategoryName("test2");
        category2.setDeleteFlg("1");

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        
        // empty
        List<SiteCategory> result1 = categoryRepository.findActiveCategoryBySiteInfoId(entity1);
        // 1 item
        List<SiteCategory> result2 = categoryRepository.findActiveCategoryBySiteInfoId(entity2);
        
        assertEquals(1, result1.size());
        assertTrue(CollectionUtils.isEmpty(result2));
        
        assertEquals("test1", result1.get(0).getCategoryName());
	}

}
