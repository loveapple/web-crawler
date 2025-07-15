package com.happinesea.webcrawler.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteCategory;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class CrawlerComponentsTest {

    @Mock
    private SiteInfoProcessRepository siteInfoProcessRepository;

    @InjectMocks
    private CrawlerComponents crawlerComponents;

    @Test
    void testSiteInfoProcessor_success() throws Exception {
        SiteInfoProcessPool process = new SiteInfoProcessPool();
        SiteCategory category = new SiteCategory();
        category.setCategoryUrl("http://example.com");
        process.setSiteCategory(category);

        // save時にそのまま返す
        when(siteInfoProcessRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ItemProcessor<SiteInfoProcessPool, SiteInfoProcessPool> processor = crawlerComponents.siteInfoProcessor();

        SiteInfoProcessPool result = processor.process(process);

        assertEquals(ProcessStatus.SUCCESS, result.getProcessStatus());
        assertNotNull(result.getProcessTime());
    }

    @Test
    void testSiteInfoProcessor_failure() throws Exception {
        SiteInfoProcessPool process = new SiteInfoProcessPool();
        SiteCategory category = mock(SiteCategory.class);

        when(category.getCategoryUrl()).thenThrow(new RuntimeException("URL取得失敗"));
        process.setSiteCategory(category);

        when(siteInfoProcessRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ItemProcessor<SiteInfoProcessPool, SiteInfoProcessPool> processor = crawlerComponents.siteInfoProcessor();

        SiteInfoProcessPool result = processor.process(process);

        assertEquals(ProcessStatus.FAIL, result.getProcessStatus());
        assertNotNull(result.getProcessTime());
    }

    @Test
    void testSiteInfoWriter() throws Exception {
        // Arrange
        SiteInfoProcessPool p1 = new SiteInfoProcessPool();
        SiteInfoProcessPool p2 = new SiteInfoProcessPool();
        List<SiteInfoProcessPool> expected = List.of(p1, p2);

        Chunk<SiteInfoProcessPool> chunk = new Chunk<>(expected);

        ItemWriter<SiteInfoProcessPool> writer = crawlerComponents.siteInfoProcessWriter();

        // Act
        writer.write(chunk);

        // Assert
        ArgumentCaptor<List<SiteInfoProcessPool>> captor = ArgumentCaptor.forClass(List.class);
        verify(siteInfoProcessRepository, times(1)).saveAll(captor.capture());

        List<SiteInfoProcessPool> actualSaved = captor.getValue();
        assertThat(actualSaved).containsExactlyElementsOf(expected);
    }
}

