package com.ssafy.newstagram.api.admin.service;

import com.ssafy.newstagram.api.admin.dto.RSSAdminJobLogResponse;
import com.ssafy.newstagram.api.admin.repository.RSSAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RSSAdminServiceImpl implements RSSAdminService {

    private final RSSAdminRepository rssAdminRepository;

    public RSSAdminServiceImpl(RSSAdminRepository rssAdminRepository) {
        this.rssAdminRepository = rssAdminRepository;
    }

    @Override
    public List<RSSAdminJobLogResponse> getRssJobLogs(LocalDate periodDate) {
        return rssAdminRepository.findJobLogsByPeriodDate(periodDate)
                .stream()
                .map(v -> new RSSAdminJobLogResponse(
                        v.getEndedAt(),
                        v.getJobName(),
                        v.getMessage(),
                        v.getStartedAt(),
                        v.getStatus()
                ))
                .toList();
    }
}

