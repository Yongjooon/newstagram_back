TRUNCATE TABLE news_categories RESTART IDENTITY CASCADE;

INSERT INTO news_categories (id, name, description) VALUES
    (1, 'TOP', '속보, 최신 기사, 헤드라인, 전체 기사 스트림'),
    (2, 'POLITICS', '정치 관련 기사'),
    (3, 'ECONOMY', '경제 관련 기사'),
    (4, 'BUSINESS', '기업, 산업, 증권, 부동산, 마켓 관련 기사'),
    (5, 'SOCIETY', '사회 일반 기사'),
    (6, 'LOCAL', '지역, 전국 이슈'),
    (7, 'WORLD', '국제, 세계 뉴스'),
    (8, 'NORTH_KOREA', '북한 관련 기사'),
    (9, 'CULTURE_LIFE', '문화, 생활, 라이프 기사'),
    (10, 'ENTERTAINMENT', '연예, 예능, 게임 등'),
    (11, 'SPORTS', '스포츠 기사'),
    (12, 'WEATHER', '날씨 기사'),
    (13, 'SCIENCE_ENV', '과학, 기술, 환경 기사'),
    (14, 'HEALTH', '건강, 의료 기사'),
    (15, 'OPINION', '사설, 칼럼, 오피니언'),
    (16, 'PEOPLE', '사람들, 인물 기사'),
    (17, 'OTHER', '기타 분류 (영문, 뉴스레터 등)');

TRUNCATE TABLE news_sources RESTART IDENTITY CASCADE;

INSERT INTO news_sources (name, homepage_url) VALUES
    ('Yonhaptv', 'https://www.yonhapnewstv.co.kr/add/rss'),
    ('Yonhap', 'https://www.yna.co.kr/rss/index'),
    ('JTBC', 'https://news.jtbc.co.kr/rss'),
    ('Chosun', 'https://rssplus.chosun.com/'),
    ('SBS', 'https://news.sbs.co.kr/news/rss.do'),
    ('Mail', 'https://www.mk.co.kr/rss'),
    ('Khan', 'https://www.khan.co.kr/help/help_rss.html');

INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'TOP'), 'Yonhaptv Top Feed', 'https://www.yonhapnewstv.co.kr/browse/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'POLITICS'), 'Yonhaptv Politics Feed', 'https://www.yonhapnewstv.co.kr/category/news/politics/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'ECONOMY'), 'Yonhaptv Economy Feed', 'https://www.yonhapnewstv.co.kr/category/news/economy/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'SOCIETY'), 'Yonhaptv Society Feed', 'https://www.yonhapnewstv.co.kr/category/news/society/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'LOCAL'), 'Yonhaptv Local Feed', 'https://www.yonhapnewstv.co.kr/category/news/local/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'WORLD'), 'Yonhaptv World Feed', 'https://www.yonhapnewstv.co.kr/category/news/international/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'), 'Yonhaptv Culture_life Feed', 'https://www.yonhapnewstv.co.kr/category/news/culture/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'SPORTS'), 'Yonhaptv Sports Feed', 'https://www.yonhapnewstv.co.kr/category/news/sports/feed', TRUE),
    ((SELECT id FROM news_sources WHERE name = 'Yonhaptv'), (SELECT id FROM news_categories WHERE name = 'WEATHER'), 'Yonhaptv Weather Feed', 'https://www.yonhapnewstv.co.kr/category/news/weather/feed', TRUE);

-- 연합뉴스 (Yonhap)
INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'Yonhap Latest Feed',
                                                                                 'https://www.yna.co.kr/rss/news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'POLITICS'),
                                                                                 'Yonhap Politics Feed',
                                                                                 'https://www.yna.co.kr/rss/politics.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ECONOMY'),
                                                                                 'Yonhap Economy Feed',
                                                                                 'https://www.yna.co.kr/rss/economy.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'NORTH_KOREA'),
                                                                                 'Yonhap North Korea Feed',
                                                                                 'https://www.yna.co.kr/rss/northkorea.xml',
                                                                                 TRUE
                                                                             ),
                                                                             -- 마켓+ → BUSINESS
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'BUSINESS'),
                                                                                 'Yonhap MarketPlus Feed',
                                                                                 'https://www.yna.co.kr/rss/market.xml',
                                                                                 TRUE
                                                                             ),
                                                                             -- 산업 → BUSINESS
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'BUSINESS'),
                                                                                 'Yonhap Industry Feed',
                                                                                 'https://www.yna.co.kr/rss/industry.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SOCIETY'),
                                                                                 'Yonhap Society Feed',
                                                                                 'https://www.yna.co.kr/rss/society.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'LOCAL'),
                                                                                 'Yonhap Local Feed',
                                                                                 'https://www.yna.co.kr/rss/local.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'WORLD'),
                                                                                 'Yonhap World Feed',
                                                                                 'https://www.yna.co.kr/rss/international.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'),
                                                                                 'Yonhap Culture Feed',
                                                                                 'https://www.yna.co.kr/rss/culture.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'HEALTH'),
                                                                                 'Yonhap Health Feed',
                                                                                 'https://www.yna.co.kr/rss/health.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ENTERTAINMENT'),
                                                                                 'Yonhap Entertainment Feed',
                                                                                 'https://www.yna.co.kr/rss/entertainment.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SPORTS'),
                                                                                 'Yonhap Sports Feed',
                                                                                 'https://www.yna.co.kr/rss/sports.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'OPINION'),
                                                                                 'Yonhap Opinion Feed',
                                                                                 'https://www.yna.co.kr/rss/opinion.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Yonhap'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'PEOPLE'),
                                                                                 'Yonhap People Feed',
                                                                                 'https://www.yna.co.kr/rss/people.xml',
                                                                                 TRUE
                                                                             );



-- JTBC
INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'JTBC Breaking News Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/newsflesh',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'JTBC Issue Top10 Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/issue',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'POLITICS'),
                                                                                 'JTBC Politics Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/politics',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ECONOMY'),
                                                                                 'JTBC Economy Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/economy',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SOCIETY'),
                                                                                 'JTBC Society Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/society',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'WORLD'),
                                                                                 'JTBC World Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/international',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'),
                                                                                 'JTBC Culture Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/culture',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ENTERTAINMENT'),
                                                                                 'JTBC Entertainment Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/entertainment',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SPORTS'),
                                                                                 'JTBC Sports Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/sports',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'JTBC'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'WEATHER'),
                                                                                 'JTBC Weather Feed',
                                                                                 'https://news-ex.jtbc.co.kr/v1/get/rss/section/weather',
                                                                                 TRUE
                                                                             );



-- 조선일보 (ChosunIlbo)
INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'Chosun All Articles Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'POLITICS'),
                                                                                 'Chosun Politics Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/politics/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ECONOMY'),
                                                                                 'Chosun Economy Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/economy/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SOCIETY'),
                                                                                 'Chosun Society Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/national/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'WORLD'),
                                                                                 'Chosun World Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/international/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'),
                                                                                 'Chosun Culture&Life Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/culture-life/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'OPINION'),
                                                                                 'Chosun Opinion Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/opinion/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SPORTS'),
                                                                                 'Chosun Sports Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/sports/?outputType=xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Chosun'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ENTERTAINMENT'),
                                                                                 'Chosun Entertainment Feed',
                                                                                 'https://www.chosun.com/arc/outboundfeeds/rss/category/entertainments/?outputType=xml',
                                                                                 TRUE
                                                                             );



-- SBS
INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'SBS Hot Issues Feed',
                                                                                 'https://news.sbs.co.kr/news/headlineRssFeed.do?plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'SBS Popular Now Feed',
                                                                                 'https://news.sbs.co.kr/news/TopicRssFeed.do?plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'SBS Latest Feed',
                                                                                 'https://news.sbs.co.kr/news/newsflashRssFeed.do?plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'POLITICS'),
                                                                                 'SBS Politics Feed',
                                                                                 'https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=01&plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ECONOMY'),
                                                                                 'SBS Economy Feed',
                                                                                 'https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=02&plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SOCIETY'),
                                                                                 'SBS Society Feed',
                                                                                 'https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=03&plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'WORLD'),
                                                                                 'SBS World Feed',
                                                                                 'https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=07&plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'),
                                                                                 'SBS Life&Culture Feed',
                                                                                 'https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=08&plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ENTERTAINMENT'),
                                                                                 'SBS Entertainment Feed',
                                                                                 'https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=14&plink=RSSREADER',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'SBS'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SPORTS'),
                                                                                 'SBS Sports Feed',
                                                                                 'https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=09&plink=RSSREADER',
                                                                                 TRUE
                                                                             );



-- 매일경제 (MaeilKyungje)  -에러
INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'MK Headline Feed',
                                                                                 'https://www.mk.co.kr/rss/30000001/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'MK All News Feed',
                                                                                 'https://www.mk.co.kr/rss/40300001/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ECONOMY'),
                                                                                 'MK Economy Feed',
                                                                                 'https://www.mk.co.kr/rss/30100041/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'POLITICS'),
                                                                                 'MK Politics Feed',
                                                                                 'https://www.mk.co.kr/rss/30200030/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SOCIETY'),
                                                                                 'MK Society Feed',
                                                                                 'https://www.mk.co.kr/rss/50400012/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'WORLD'),
                                                                                 'MK World Feed',
                                                                                 'https://www.mk.co.kr/rss/30300018/',
                                                                                 TRUE
                                                                             ),
                                                                             -- 기업&경영 → BUSINESS
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'BUSINESS'),
                                                                                 'MK Business&Management Feed',
                                                                                 'https://www.mk.co.kr/rss/50100032/',
                                                                                 TRUE
                                                                             ),
                                                                             -- 증권 → BUSINESS
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'BUSINESS'),
                                                                                 'MK Stock Market Feed',
                                                                                 'https://www.mk.co.kr/rss/50200011/',
                                                                                 TRUE
                                                                             ),
                                                                             -- 부동산 → BUSINESS
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'BUSINESS'),
                                                                                 'MK Real Estate Feed',
                                                                                 'https://www.mk.co.kr/rss/50300009/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'),
                                                                                 'MK Culture&Entertainment Feed',
                                                                                 'https://www.mk.co.kr/rss/30000023/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SPORTS'),
                                                                                 'MK Sports Feed',
                                                                                 'https://www.mk.co.kr/rss/71000001/',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Mail'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ENTERTAINMENT'),
                                                                                 'MK Game Feed',
                                                                                 'https://www.mk.co.kr/rss/50700001/',
                                                                                 TRUE
                                                                             );



-- 경향신문 (Kyunghyang)
INSERT INTO rss_feeds (source_id, category_id, name, rss_url, is_active) VALUES
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'TOP'),
                                                                                 'Kyunghyang All News Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/total_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'OPINION'),
                                                                                 'Kyunghyang Cartoon Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/cartoon_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'OPINION'),
                                                                                 'Kyunghyang Opinion Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/opinion_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'POLITICS'),
                                                                                 'Kyunghyang Politics Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/politic_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'ECONOMY'),
                                                                                 'Kyunghyang Economy Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/economy_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SOCIETY'),
                                                                                 'Kyunghyang Society Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/society_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'LOCAL'),
                                                                                 'Kyunghyang Local Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/local_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'WORLD'),
                                                                                 'Kyunghyang World Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/kh_world.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'),
                                                                                 'Kyunghyang Culture Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/culture_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SPORTS'),
                                                                                 'Kyunghyang Sports Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/kh_sports.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'SCIENCE_ENV'),
                                                                                 'Kyunghyang Science&Environment Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/science_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'CULTURE_LIFE'),
                                                                                 'Kyunghyang Life Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/life_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'PEOPLE'),
                                                                                 'Kyunghyang People Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/people_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'OTHER'),
                                                                                 'Kyunghyang English Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/english_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'OTHER'),
                                                                                 'Kyunghyang Newsletter Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/newsletter_news.xml',
                                                                                 TRUE
                                                                             ),
                                                                             (
                                                                                 (SELECT id FROM news_sources WHERE name = 'Khan'),
                                                                                 (SELECT id FROM news_categories WHERE name = 'OTHER'),
                                                                                 'Kyunghyang Interactive Feed',
                                                                                 'https://www.khan.co.kr/rss/rssdata/interactive_news.xml',
                                                                                 TRUE
                                                                             );

