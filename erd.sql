-- 사용자 테이블
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_image VARCHAR(255),
    introduction TEXT,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at DATETIME DEFAULT NULL
) COMMENT '사용자';

-- 포스트 테이블 (댓글 통합)
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES posts(id)
) COMMENT '포스트 및 댓글';

-- 좋아요 테이블
CREATE TABLE likes (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (post_id) REFERENCES posts(id)
) COMMENT '좋아요';

-- 팔로우 테이블
CREATE TABLE follows (
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    FOREIGN KEY (follower_id) REFERENCES users(id),
    FOREIGN KEY (followee_id) REFERENCES users(id)
) COMMENT '팔로우';

-- 주식 테이블
CREATE TABLE stocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    market_type ENUM('KOSPI', 'KOSDAQ') NOT NULL,
    sector VARCHAR(255),
    market_cap BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '주식';

-- 주식 가격 테이블
CREATE TABLE stock_prices (
    stock_id BIGINT NOT NULL,
    date DATE NOT NULL,
    open_price INT NOT NULL,
    high_price INT NOT NULL,
    low_price INT NOT NULL,
    close_price INT NOT NULL,
    volume BIGINT NOT NULL,
    change_amount INT,
    change_rate DECIMAL(5,2),
    trading_amount BIGINT,
    PRIMARY KEY (stock_id, date),
    FOREIGN KEY (stock_id) REFERENCES stocks(id)
) COMMENT '주식 가격';

-- 사용자 주식 관심 및 거래 테이블
CREATE TABLE user_stocks (
    user_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    watchlist BOOLEAN DEFAULT FALSE,
    quantity INT DEFAULT 0,
    average_price INT DEFAULT 0,
    last_trade_date DATETIME,
    PRIMARY KEY (user_id, stock_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (stock_id) REFERENCES stocks(id)
) COMMENT '사용자 주식 관심 및 보유';

-- 배치 작업 테이블
CREATE TABLE batch_jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_name VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '배치 작업';