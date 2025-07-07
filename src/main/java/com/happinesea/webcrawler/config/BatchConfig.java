package com.happinesea.webcrawler.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.happinesea.webcrawler.model.WebPage;

@Configuration
public class BatchConfig {

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setThreadNamePrefix("crawler-");
		executor.initialize();
		return executor;
	}

	@Bean
	public Job crawlJob(JobRepository jobRepository, Step crawlStep) {
		return new JobBuilder("crawlJob", jobRepository).start(crawlStep).build();
	}

	@Bean
	public Step crawlStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
			CrawlerComponents crawlerComponents, TaskExecutor taskExecutor) {

		return new StepBuilder("crawlStep", jobRepository).<String, WebPage>chunk(2, transactionManager)
				.reader(crawlerComponents.urlReader())
				.processor(crawlerComponents.webCrawlerProcessor(crawlerComponents.restTemplate()))
				.writer(crawlerComponents.webPageWriter()).taskExecutor(taskExecutor).build();
	}
}
