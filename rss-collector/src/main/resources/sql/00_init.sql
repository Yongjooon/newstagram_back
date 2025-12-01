-- pgvector 확장 기능 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 스키마 생성 및 권한 부여 (안전장치)
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL ON SCHEMA public TO newstagram;
SET search_path TO public;

-- 1. 사용자 테이블: 사용자 계정 정보 및 선호도 임베딩 저장
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    login_type VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    provider_id TEXT,
    role VARCHAR(50) DEFAULT 'USER',
    refresh_token TEXT,
    preference_embedding vector(1536), -- 사용자의 선호도 벡터 (이동 평균)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 뉴스 카테고리: 뉴스 분류를 위한 표준 카테고리
CREATE TABLE IF NOT EXISTS news_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- 3. 뉴스 소스: 언론사 또는 미디어 매체
CREATE TABLE IF NOT EXISTS news_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    homepage_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. RSS 피드: 소스별 특정 RSS 엔드포인트
CREATE TABLE IF NOT EXISTS rss_feeds (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    rss_url TEXT NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_fetched_at TIMESTAMP,
    category_id BIGINT NOT NULL REFERENCES news_categories(id) ON DELETE CASCADE,
    source_id BIGINT NOT NULL REFERENCES news_sources(id) ON DELETE CASCADE
);

-- 5. 기사: 피드에서 수집된 개별 뉴스 항목
CREATE TABLE IF NOT EXISTS articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    description TEXT,
    url TEXT NOT NULL UNIQUE,
    thumbnail_url TEXT,
    author VARCHAR(255),
    published_at TIMESTAMP NOT NULL,
    embedding vector(1536), -- 유사도 검색을 위한 콘텐츠 임베딩
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    feed_id BIGINT NOT NULL REFERENCES rss_feeds(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES news_categories(id) ON DELETE CASCADE,
    sources_id BIGINT NOT NULL REFERENCES news_sources(id) ON DELETE CASCADE
);

-- 6. 기간별 추천 기사: 실시간, 일일, 주간, 월간 단위로 집계된 기사 목록
CREATE TABLE IF NOT EXISTS period_recommendations (
    id BIGSERIAL PRIMARY KEY,
    period_type VARCHAR(20) NOT NULL, -- 'REALTIME', 'DAILY', 'WEEKLY', 'MONTHLY'
    ranking INTEGER NOT NULL, -- 노출 순위
    score FLOAT, -- 정렬 기준 점수
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 집계 시점
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE
);

-- 7. 사용자 선호 키워드: 사용자 기록이나 선택에서 도출된 명시적 키워드
CREATE TABLE IF NOT EXISTS user_preference_keywords (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    keyword VARCHAR(100) NOT NULL,
    score FLOAT DEFAULT 1.0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. 사용자 카테고리: 사용자가 명시적으로 선택한 카테고리 (콜드 스타트용)
CREATE TABLE IF NOT EXISTS user_categories (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES news_categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, category_id)
);

-- 9. 사용자 상호작용 로그: 사용자 행동 기록 (클릭)
CREATE TABLE IF NOT EXISTS user_interaction_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    interaction_type VARCHAR(20) NOT NULL DEFAULT 'CLICK',
    session_id VARCHAR(100),
    user_agent TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 추천 로그: CTR 분석을 위해 사용자에게 추천된 내용 추적
CREATE TABLE IF NOT EXISTS recommendation_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recommended_article_ids BIGINT NOT NULL, -- 주의: 현재 스키마상 BIGINT로 되어있으나, 여러 ID 저장을 위해선 TEXT나 별도 테이블 권장 (현재는 스키마 따름)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. 시스템 작업 로그: 백그라운드 작업 로그 (RSS 수집 등)
CREATE TABLE IF NOT EXISTS system_job_logs (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(50) NOT NULL, -- 예: 'RSS_COLLECT'
    run_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,   -- 'SUCCESS', 'FAILURE', 'RUNNING'
    message TEXT,
    items_processed INTEGER DEFAULT 0,
    retry_count INTEGER DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    feed_id BIGINT NOT NULL REFERENCES rss_feeds(id) ON DELETE CASCADE
);
