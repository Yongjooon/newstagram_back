package com.ssafy.newstagram.rss.config;

import com.ssafy.newstagram.rss.batch.RssBatchQuartzJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    //어떤 job을 실행할 것인가
    @Bean
    public JobDetail rssJobDetail(){
        return JobBuilder.newJob(RssBatchQuartzJob.class)
                .withIdentity("rssBatchJob")
                .storeDurably()
                .build();
    }

    //언제 실행할 것인가
    @Bean
    public Trigger rssJobTrigger(JobDetail rssJobDetail){
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule("0 0 0,12 * * ?")
                .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"));

        return TriggerBuilder.newTrigger()
                .forJob(rssJobDetail)
                .withIdentity("rssBatchTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

}
