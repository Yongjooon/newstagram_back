package com.ssafy.newstagram.rss.batch;

import lombok.RequiredArgsConstructor;
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

@Configuration
@RequiredArgsConstructor
public class RssBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final NewsSourceItemReader newsSourceItemReader;
    private final NewsSourceItemWriter newsSourceItemWriter;

    //병렬 실행을 위한 TaskExecutor
    @Bean
    public TaskExecutor rssTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(7); //한번에 실행할 신문사 개수
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("RssBatch-");
        executor.initialize();
        return executor;
    }

    //신문사 단위로 chunk(1) 처리
    @Bean
    public Step rssPerSourceStep(TaskExecutor rssTaskExecutor) {
        return new StepBuilder("rssPerSourceStep", jobRepository)
                .<Long, Long>chunk(1, transactionManager)
                .reader(newsSourceItemReader)
                .writer(newsSourceItemWriter)
                .taskExecutor(rssTaskExecutor) //멀티스레드 스탭
                .build();
    }

    // 전체 신문사에 대해 위 스탭 수행
    @Bean
    public Job rssMasterJob(Step rssPerSourceStep) {
        return new JobBuilder("rssMasterJob", jobRepository)
                .start(rssPerSourceStep)
                .build();
    }
}
