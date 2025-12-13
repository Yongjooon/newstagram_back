package com.ssafy.newstagram.domain.constant;

public final class KafkaTopic {

    // 인스턴스화 방지
    private KafkaTopic() {}

    public static final class Log {
        // 사용자 기사 클릭 관련
        public static final String INTERACTION = "log.article.click";
        public static final String INTERACTION_DLT = "log.article.click.DLT";
    }
}
