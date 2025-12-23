package com.ssafy.newstagram.api.admin.service;

import com.ssafy.newstagram.api.admin.dto.RSSAdminJobLogResponse;

import java.time.LocalDate;
import java.util.List;

public interface RSSAdminService {
    List<RSSAdminJobLogResponse> getRssJobLogs(LocalDate periodDate);
}

