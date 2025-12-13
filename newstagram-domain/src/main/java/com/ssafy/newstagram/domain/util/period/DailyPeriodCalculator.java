package com.ssafy.newstagram.domain.util.period;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyPeriodCalculator implements PeriodCalculator {

    @Override
    public LocalDateTime getStart() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return yesterday.atStartOfDay();  // 00:00:00
    }

    @Override
    public LocalDateTime getEnd() {
        LocalDate today = LocalDate.now();
        return today.atStartOfDay();
    }

    @Override
    public LocalDateTime getBeforeStart() {
        return getStart().minusDays(1);
    }

    @Override
    public LocalDateTime getBeforeEnd() {
        return getEnd().minusDays(1);
    }
}
