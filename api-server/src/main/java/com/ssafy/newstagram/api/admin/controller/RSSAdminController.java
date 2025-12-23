package com.ssafy.newstagram.api.admin.controller;

import com.ssafy.newstagram.api.admin.dto.RSSAdminJobLogResponse;
import com.ssafy.newstagram.api.admin.service.RSSAdminService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class RSSAdminController {

    private final RSSAdminService rssAdminService;

    public RSSAdminController(RSSAdminService rssAdminService) {
        this.rssAdminService = rssAdminService;
    }

    @GetMapping("/rss")
    public ResponseEntity<List<RSSAdminJobLogResponse>> getRssLogs(
            @RequestParam("periodDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate periodDate
    ) {
        List<RSSAdminJobLogResponse> result = rssAdminService.getRssJobLogs(periodDate);
        return ResponseEntity.ok(result);
    }
}

