package com.ssafy.newstagram.domain.util.period;

// Period.DAILY
public enum Period {
    REALTIME,
    DAILY,
    WEEKLY;

    public PeriodCalculator getCalculator() {
        return switch (this) {
            case REALTIME -> new RealtimePeriodCalculator();
            case DAILY -> new DailyPeriodCalculator();
            case WEEKLY -> new WeeklyPeriodCalculator();
        };
    }
}
