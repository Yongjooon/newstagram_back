package com.ssafy.newstagram.domain.util.period;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class WeeklyPeriodCalculator implements PeriodCalculator {

    @Override
    public LocalDateTime getStart() {
        LocalDate lastWeekMonday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1);

        return lastWeekMonday.atStartOfDay();
    }

    @Override
    public LocalDateTime getEnd() {
        LocalDate thisWeekMonday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return thisWeekMonday.atStartOfDay();
    }

    @Override
    public LocalDateTime getBeforeStart() {
        return getStart().minusWeeks(1);
    }

    @Override
    public LocalDateTime getBeforeEnd() {
        return getEnd().minusWeeks(1);
    }
}
