package com.ssafy.newstagram.api.admin.dto;

import java.time.LocalDateTime;

public record RSSAdminJobLogResponse(
        LocalDateTime endedAt,
        String jobName,
        String message,
        LocalDateTime startedAt,
        String status
) {}
