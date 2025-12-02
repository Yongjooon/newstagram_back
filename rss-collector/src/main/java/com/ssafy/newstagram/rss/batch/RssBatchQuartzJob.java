package com.ssafy.newstagram.rss.batch;

import com.ssafy.newstagram.rss.service.RssBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class RssBatchQuartzJob implements Job {
    private final RssBatchService rssBatchService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        try{
            log.info("[Quartz] RSS 배치 시작");
            Map<String, Object> result = rssBatchService.runRssMasterJob();
            log.info("RSS 배치 완료 -{}", result);
        }catch(Exception e){
            log.info("[Quartz] RSS 배치 실행 중 오류 발생",e);
            throw new JobExecutionException(e);
        }
    }
}
