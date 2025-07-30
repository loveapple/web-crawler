package com.happinesea.webcrawler.config;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.happinesea.webcrawler.Const.ProcessStatus;
import com.happinesea.webcrawler.entity.SiteInfoProcessPool;
import com.happinesea.webcrawler.repository.SiteInfoProcessRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class BatchConfig {
    @Autowired
    private SiteInfoProcessRepository siteInfoProcessRepository;

    @Bean
    public Job crawlJob(JobRepository jobRepository, Step crawlStep) {
        return new JobBuilder("crawlJob", jobRepository)
                .start(crawlStep)
                .build();
    }

    @Bean
    public Step crawlStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          CrawlerComponents crawlerComponents) {

        List<SiteInfoProcessPool> targetList =
                siteInfoProcessRepository.findByProcessStatusNot(ProcessStatus.PROCESSING);

        if (targetList.isEmpty()) {
            log.info("処理対象の SiteInfoProcess が存在しないため、ジョブを正常終了します。");

            return new StepBuilder("emptyStep", jobRepository)
                    .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED, transactionManager)
                    .build();
        }

        // データがある場合だけ TaskExecutor を初期化
        int threadCount = targetList.size();
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(threadCount);
        taskExecutor.setMaxPoolSize(threadCount);
        //TODO 
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix("crawler-");
        //TODO
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(30);
        taskExecutor.initialize();

        return new StepBuilder("crawlStep", jobRepository)
                .<SiteInfoProcessPool, SiteInfoProcessPool>chunk(5, transactionManager)
                .reader(new ListItemReader<>(targetList))
                .processor(crawlerComponents.siteInfoProcessor())
                .writer(crawlerComponents.siteInfoProcessWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

}

