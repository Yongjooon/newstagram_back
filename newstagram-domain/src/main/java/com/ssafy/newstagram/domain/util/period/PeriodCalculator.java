package com.ssafy.newstagram.domain.util.period;

import java.time.LocalDateTime;

public interface PeriodCalculator {
    LocalDateTime getStart();
    LocalDateTime getEnd();
    LocalDateTime getBeforeStart();
    LocalDateTime getBeforeEnd();
}
