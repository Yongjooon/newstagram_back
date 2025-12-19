package com.ssafy.newstagram.domain.util.period;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RealtimePeriodCalculator implements PeriodCalculator {

    // 기준 시간대
//    private static final int[] BLOCKS = {0, 6, 9, 12, 15, 18, 21};
    private static final int[] BLOCKS = {0, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    private static final int BLOCK_SIZE = 19;

    static int findEndBlockIdx(int hour) {
        int idx = 0;
        for (int i = 0; i < BLOCK_SIZE; i++) {
            if (BLOCKS[i] > hour) break;
            idx = i;
        }
        return idx;
    }

    static LocalDateTime findDateTimeByIdx(LocalDate date, int idx) {
        while (idx < 0) {
            idx += BLOCK_SIZE;
            date = date.minusDays(1);
        }
        return date.atTime(BLOCKS[idx], 0);
    }

    @Override
    public LocalDateTime getStart() {
        LocalDateTime now = LocalDateTime.now();
        int idx = findEndBlockIdx(now.getHour());
        return findDateTimeByIdx(now.toLocalDate(), idx-1);
    }

    @Override
    public LocalDateTime getEnd() {
        LocalDateTime now = LocalDateTime.now();
        int idx = findEndBlockIdx(now.getHour());
        return findDateTimeByIdx(now.toLocalDate(), idx);
    }

    @Override
    public LocalDateTime getBeforeStart() {
        LocalDateTime now = LocalDateTime.now();
        int idx = findEndBlockIdx(now.getHour());
        return findDateTimeByIdx(now.toLocalDate(), idx-2);
    }

    @Override
    public LocalDateTime getBeforeEnd() {
        return getStart();
    }
}
