package com.ssafy.newstagram.rss.controller;

import com.ssafy.newstagram.rss.service.RssBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchController {
    private final RssBatchService rssBatchService;

    @PostMapping("/rss")
    public Map<String, Object>startBatch() throws Exception{
        return rssBatchService.runRssMasterJob();
    }

}
