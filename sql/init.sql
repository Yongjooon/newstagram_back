-- pgvector 확장 기능 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 스키마 생성 및 권한 부여 (안전장치)
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL ON SCHEMA public TO newstagram;
SET search_path TO public;

-- 1. 사용자 테이블: 사용자 계정 정보 및 선호도 임베딩 저장
CREATE TABLE users (
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
CREATE TABLE news_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- 3. 뉴스 소스: 언론사 또는 미디어 매체
CREATE TABLE news_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    homepage_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. RSS 피드: 소스별 특정 RSS 엔드포인트
CREATE TABLE rss_feeds (
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
CREATE TABLE articles (
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
CREATE TABLE period_recommendations (
    id BIGSERIAL PRIMARY KEY,
    period_type VARCHAR(20) NOT NULL, -- 'REALTIME', 'DAILY', 'WEEKLY', 'MONTHLY'
    ranking INTEGER NOT NULL, -- 노출 순위
    score FLOAT, -- 정렬 기준 점수
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 집계 시점
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE
);

-- 7. 사용자 선호 키워드: 사용자 기록이나 선택에서 도출된 명시적 키워드
CREATE TABLE user_preference_keywords (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    keyword VARCHAR(100) NOT NULL,
    score FLOAT DEFAULT 1.0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. 사용자 카테고리: 사용자가 명시적으로 선택한 카테고리 (콜드 스타트용)
CREATE TABLE user_categories (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES news_categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, category_id)
);

-- 9. 사용자 상호작용 로그: 사용자 행동 기록 (클릭)
CREATE TABLE user_interaction_logs (
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
CREATE TABLE recommendation_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recommended_article_ids BIGINT NOT NULL, -- 주의: 현재 스키마상 BIGINT로 되어있으나, 여러 ID 저장을 위해선 TEXT나 별도 테이블 권장 (현재는 스키마 따름)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. 시스템 작업 로그: 백그라운드 작업 로그 (RSS 수집 등)
CREATE TABLE system_job_logs (
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

-- 초기 데이터 삽입

-- 1. 카테고리
INSERT INTO news_categories (name, description) VALUES
('Politics', '정치 뉴스 및 정부 소식'),
('Economy', '금융 시장, 비즈니스 및 경제'),
('Society', '사회 문제, 휴먼 스토리 및 커뮤니티'),
('Technology', 'IT, 과학 및 기술 트렌드'),
('World', '국제 뉴스 및 글로벌 이벤트'),
('Culture', '예술, 엔터테인먼트 및 라이프스타일'),
('Sports', '스포츠 뉴스 및 경기 결과');

-- 2. 뉴스 소스
INSERT INTO news_sources (name, homepage_url) VALUES
('TechCrunch', 'https://techcrunch.com'),
('BBC News', 'https://www.bbc.com/news'),
('CNN', 'https://edition.cnn.com'),
('Reuters', 'https://www.reuters.com');

-- 3. RSS 피드
INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
((SELECT id FROM news_sources WHERE name = 'TechCrunch'), (SELECT id FROM news_categories WHERE name = 'Technology'), 'TechCrunch Main Feed', 'https://techcrunch.com/feed/', TRUE),
((SELECT id FROM news_sources WHERE name = 'BBC News'), (SELECT id FROM news_categories WHERE name = 'World'), 'BBC World News', 'http://feeds.bbci.co.uk/news/world/rss.xml', TRUE),
((SELECT id FROM news_sources WHERE name = 'CNN'), (SELECT id FROM news_categories WHERE name = 'Politics'), 'CNN Politics', 'http://rss.cnn.com/rss/cnn_allpolitics.rss', TRUE);

-- 4. 사용자
INSERT INTO users (email, password_hash, nickname, role) VALUES
('admin@newstagram.com', 'hashed_secret_password', 'AdminUser', 'ADMIN'),
('user1@example.com', 'hashed_password_1', 'NewsLover', 'USER'),
('user2@example.com', 'hashed_password_2', 'TechGeek', 'USER');

-- 5. 사용자 카테고리 (콜드 스타트)
INSERT INTO user_categories (user_id, category_id) VALUES
((SELECT id FROM users WHERE email = 'user2@example.com'), (SELECT id FROM news_categories WHERE name = 'Technology')),
((SELECT id FROM users WHERE email = 'user2@example.com'), (SELECT id FROM news_categories WHERE name = 'Economy'));

-- 6. 기사 (더미 데이터)
INSERT INTO articles (feed_id, category_id, sources_id, title, content, description, url, published_at) VALUES
(
    (SELECT id FROM rss_feeds WHERE name = 'TechCrunch Main Feed'), 
    (SELECT id FROM news_categories WHERE name = 'Technology'), 
    (SELECT id FROM news_sources WHERE name = 'TechCrunch'),
    'AI Revolution in 2025', 'Content about AI...', 'Description about AI...', 'https://techcrunch.com/ai-2025', NOW()
),
(
    (SELECT id FROM rss_feeds WHERE name = 'BBC World News'), 
    (SELECT id FROM news_categories WHERE name = 'World'), 
    (SELECT id FROM news_sources WHERE name = 'BBC News'),
    'Global Climate Summit', 'Content about climate...', 'Description about climate...', 'https://bbc.com/climate', NOW()
),
(
    (SELECT id FROM rss_feeds WHERE name = 'CNN Politics'), 
    (SELECT id FROM news_categories WHERE name = 'Politics'), 
    (SELECT id FROM news_sources WHERE name = 'CNN'),
    'Election Updates', 'Content about elections...', 'Description about elections...', 'https://cnn.com/election', NOW()
);

-- 7. 기간별 추천 기사 (예시)
INSERT INTO period_recommendations (period_type, article_id, ranking, score) VALUES
('REALTIME', (SELECT id FROM articles WHERE title = 'AI Revolution in 2025'), 1, 95.5),
('DAILY', (SELECT id FROM articles WHERE title = 'Global Climate Summit'), 1, 88.0),
('WEEKLY', (SELECT id FROM articles WHERE title = 'Election Updates'), 1, 92.0);
