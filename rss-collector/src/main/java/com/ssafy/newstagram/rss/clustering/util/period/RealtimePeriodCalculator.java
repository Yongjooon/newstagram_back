package com.ssafy.newstagram.rss.clustering.util.period;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RealtimePeriodCalculator implements PeriodCalculator {

    @Override
    public LocalDateTime getStart() {
        LocalDateTime now = LocalDateTime.now();

        // 현재 시간이 속한 6시간 구간 인덱스 (0~3)
        int block = now.getHour() / 6;

        // 조회해야 하는 이전 구간
        int prevBlock = block - 1;
        LocalDate date = now.toLocalDate();

        // block == 0이면 전날 마지막 구간(18~24)을 조회해야 함
        if (prevBlock < 0) {
            prevBlock = 3; // 18~24
            date = date.minusDays(1);
        }

        int startHour = prevBlock * 6;
        return date.atTime(startHour, 0);
    }

    @Override
    public LocalDateTime getEnd() {
        LocalDateTime now = LocalDateTime.now();

        int block = now.getHour() / 6;
        int prevBlock = block - 1;
        LocalDate date = now.toLocalDate();

        if (prevBlock < 0) {
            prevBlock = 3;
            date = date.minusDays(1);
        }

        int endHour = prevBlock * 6 + 6; // 6시간 뒤
        if (endHour == 24) endHour = 0;

        // endHour가 0이면 다음날 00시
        if (endHour == 0) {
            return date.plusDays(1).atStartOfDay();
        }

        return date.atTime(endHour, 0);
    }

    @Override
    public LocalDateTime getBeforeStart() {
        LocalDateTime now = LocalDateTime.now().minusHours(6);

        // 현재 시간이 속한 6시간 구간 인덱스 (0~3)
        int block = now.getHour() / 6;

        // 조회해야 하는 이전 구간
        int prevBlock = block - 1;
        LocalDate date = now.toLocalDate();

        // block == 0이면 전날 마지막 구간(18~24)을 조회해야 함
        if (prevBlock < 0) {
            prevBlock = 3; // 18~24
            date = date.minusDays(1);
        }

        int startHour = prevBlock * 6;
        return date.atTime(startHour, 0);
    }

    @Override
    public LocalDateTime getBeforeEnd() {
        LocalDateTime now = LocalDateTime.now().minusHours(6);

        int block = now.getHour() / 6;
        int prevBlock = block - 1;
        LocalDate date = now.toLocalDate();

        if (prevBlock < 0) {
            prevBlock = 3;
            date = date.minusDays(1);
        }

        int endHour = prevBlock * 6 + 6; // 6시간 뒤
        if (endHour == 24) endHour = 0;

        // endHour가 0이면 다음날 00시
        if (endHour == 0) {
            return date.plusDays(1).atStartOfDay();
        }

        return date.atTime(endHour, 0);
    }


}
