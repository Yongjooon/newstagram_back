-- Newstagram Database Initialization Script
-- 이 파일은 Docker Compose 실행 시 자동으로 실행됩니다.

USE newstagram;

-- 연결 테스트용 테이블
CREATE TABLE IF NOT EXISTS connection_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테스트 데이터
INSERT INTO connection_test (message) VALUES ('Database connection successful!');

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 뉴스 카테고리 테이블
CREATE TABLE IF NOT EXISTS news_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 언론사 테이블
CREATE TABLE IF NOT EXISTS news_sources (
    source_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    rss_url VARCHAR(500) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기사 테이블
CREATE TABLE IF NOT EXISTS articles (
    article_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT,
    url VARCHAR(1000) NOT NULL UNIQUE,
    published_at TIMESTAMP NOT NULL,
    source_id BIGINT NOT NULL,
    category_id BIGINT,
    thumbnail_url VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES news_sources(source_id),
    FOREIGN KEY (category_id) REFERENCES news_categories(category_id),
    INDEX idx_published_at (published_at),
    INDEX idx_source_id (source_id),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 클릭 로그 테이블
CREATE TABLE IF NOT EXISTS user_click_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (article_id) REFERENCES articles(article_id),
    INDEX idx_user_id (user_id),
    INDEX idx_article_id (article_id),
    INDEX idx_clicked_at (clicked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 기본 카테고리 데이터
INSERT INTO news_categories (name, description) VALUES
('정치', '정치 관련 뉴스'),
('경제', '경제 관련 뉴스'),
('사회', '사회 관련 뉴스'),
('생활/문화', '생활 및 문화 관련 뉴스'),
('IT/과학', 'IT 및 과학 관련 뉴스'),
('스포츠', '스포츠 관련 뉴스')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 기본 언론사 데이터 (예시)
INSERT INTO news_sources (name, rss_url, is_active) VALUES
('JTBC', 'https://fs.jtbc.co.kr/RSS/newsflash.xml', TRUE),
('KBS', 'https://www.kbs.co.kr/rss/news.xml', TRUE)
ON DUPLICATE KEY UPDATE rss_url = VALUES(rss_url);
