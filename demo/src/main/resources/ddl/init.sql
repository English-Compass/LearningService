-- =====================================================
-- 학습 서비스 데이터베이스 초기화 스크립트
-- 생성일: 2024년
-- 목적: Question, QuestionAnswer, QuestionStatsView 테이블 및 관련 뷰 생성
-- 형태: CREATE IF NOT EXISTS (기존 스키마 보존)
-- =====================================================

-- =====================================================
-- 1. 문제 테이블 (Question)
-- =====================================================
CREATE TABLE IF NOT EXISTS question (
    question_id VARCHAR(255) NOT NULL PRIMARY KEY,
    question_text TEXT NOT NULL COMMENT '문제 내용',
    option_a VARCHAR(500) NOT NULL COMMENT '선택지 A',
    option_b VARCHAR(500) NOT NULL COMMENT '선택지 B', 
    option_c VARCHAR(500) NOT NULL COMMENT '선택지 C',
    correct_answer VARCHAR(1) NOT NULL COMMENT '정답 (A, B, C)',
    
    -- 카테고리 정보
    major_category VARCHAR(50) NOT NULL COMMENT '대분류',
    minor_category VARCHAR(50) NOT NULL COMMENT '소분류',
    question_type VARCHAR(50) NOT NULL COMMENT '문제 유형',
    
    -- 메타데이터
    explanation TEXT COMMENT '문제 해설',
    difficulty_level INT NOT NULL DEFAULT 1 COMMENT '난이도 (1: 초급, 2: 중급, 3: 상급)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- 인덱스
    INDEX idx_question_major_category (major_category),
    INDEX idx_question_minor_category (minor_category),
    INDEX idx_question_type (question_type),
    INDEX idx_question_difficulty (difficulty_level),
    INDEX idx_question_category_combo (major_category, minor_category, difficulty_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='문제 마스터 테이블';

-- =====================================================
-- 2. 학습 세션 테이블 (LearningSession)
-- =====================================================
CREATE TABLE IF NOT EXISTS learning_sessions (
    session_id VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID',
    
    -- 시간 정보
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '세션 생성 시간',
    started_at DATETIME(6) COMMENT '세션 시작 시간',
    completed_at DATETIME(6) COMMENT '세션 완료 시간',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- 세션 메타데이터
    status ENUM('STARTED', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'STARTED',
    session_type ENUM('PRACTICE', 'REVIEW', 'WRONG_ANSWER') NOT NULL DEFAULT 'PRACTICE',
    session_metadata TEXT COMMENT '세션 메타데이터 (JSON)',
    
    -- 통계 정보
    total_questions INT COMMENT '총 문제 수',
    answered_questions INT DEFAULT 0 COMMENT '답변한 문제 수',
    correct_answers INT DEFAULT 0 COMMENT '정답 수',
    wrong_answers INT DEFAULT 0 COMMENT '오답 수',
    progress_percentage DOUBLE DEFAULT 0.0 COMMENT '진행률 (%)',
    
    -- 인덱스
    INDEX idx_learning_sessions_user (user_id),
    INDEX idx_learning_sessions_status (status),
    INDEX idx_learning_sessions_type (session_type),
    INDEX idx_learning_sessions_user_status (user_id, status),
    INDEX idx_learning_sessions_created (created_at),
    INDEX idx_learning_sessions_completed (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학습 세션 테이블';

-- =====================================================
-- 3. 세션-문제 연결 테이블 (SessionQuestion)
-- =====================================================
CREATE TABLE IF NOT EXISTS session_question (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL COMMENT '세션 ID',
    question_id VARCHAR(255) NOT NULL COMMENT '문제 ID',
    question_order INT NOT NULL COMMENT '문제 순서 (1, 2, 3, ...)',
    
    -- 외래키 제약조건
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(question_id) ON DELETE CASCADE,
    
    -- 유니크 제약조건
    UNIQUE KEY uk_session_question (session_id, question_id),
    UNIQUE KEY uk_session_order (session_id, question_order),
    
    -- 인덱스
    INDEX idx_session_question_session (session_id),
    INDEX idx_session_question_question (question_id),
    INDEX idx_session_question_order (session_id, question_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='세션별 문제 할당 테이블';

-- =====================================================
-- 4. 문제 답변 테이블 (QuestionAnswer)
-- =====================================================
CREATE TABLE IF NOT EXISTS question_answer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL COMMENT '세션 ID',
    question_id VARCHAR(255) NOT NULL COMMENT '문제 ID',
    session_type VARCHAR(50) NOT NULL COMMENT '세션 타입',
    
    -- 답변 정보
    user_answer VARCHAR(1) NOT NULL COMMENT '사용자 답변 (A, B, C)',
    is_correct BIT(1) NOT NULL COMMENT '정답 여부',
    time_spent INT COMMENT '풀이 시간 (초)',
    answered_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '답변 시간',
    solve_count INT NOT NULL DEFAULT 1 COMMENT '해당 문제 풀이 횟수',
    
    -- 외래키 제약조건
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(question_id) ON DELETE CASCADE,
    
    -- 유니크 제약조건 (한 세션에서 같은 문제는 한 번만 답변)
    UNIQUE KEY uk_session_question_answer (session_id, question_id),
    
    -- 인덱스
    INDEX idx_question_answer_session (session_id),
    INDEX idx_question_answer_question (question_id),
    INDEX idx_question_answer_correct (is_correct),
    INDEX idx_question_answer_time (time_spent),
    INDEX idx_question_answer_answered_at (answered_at),
    INDEX idx_question_answer_user_session (session_id),
    INDEX idx_question_answer_combo (question_id, is_correct, time_spent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 문제 답변 테이블';

-- =====================================================
-- 5. 학습 세션 이벤트 테이블 (LearningSessionEvent)
-- =====================================================
CREATE TABLE IF NOT EXISTS learning_session_events (
    event_id VARCHAR(255) NOT NULL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL COMMENT '학습 세션 ID (FK)',
    user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID',
    
    -- 시간 정보
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '이벤트 생성 시간',
    published_at DATETIME(6) COMMENT '이벤트 발행 시간',
    
    -- 세션 기본 정보
    session_type VARCHAR(50) NOT NULL COMMENT 'PRACTICE, REVIEW, WRONG_ANSWER',
    session_started_at DATETIME(6) NOT NULL COMMENT '세션 시작 시간',
    session_completed_at DATETIME(6) NOT NULL COMMENT '세션 완료 시간',
    session_status VARCHAR(50) NOT NULL COMMENT 'COMPLETED, ABANDONED, PAUSED',
    session_metadata TEXT COMMENT '세션 메타데이터 (JSON)',
    
    -- 이벤트 메타데이터
    event_type VARCHAR(50) NOT NULL COMMENT '이벤트 타입 (SESSION_COMPLETED, SESSION_ABANDONED, SESSION_PAUSED)',
    event_status VARCHAR(50) NOT NULL COMMENT '이벤트 상태 (PENDING, PUBLISHED, FAILED)',
    event_version VARCHAR(20) COMMENT '이벤트 버전 (향후 호환성)',
    publish_error TEXT COMMENT '발행 실패 시 에러 메시지',
    
    -- 외래키 제약조건
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_learning_session_events_session (session_id),
    INDEX idx_learning_session_events_user (user_id),
    INDEX idx_learning_session_events_status (event_status),
    INDEX idx_learning_session_events_type (event_type),
    INDEX idx_learning_session_events_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학습 세션 이벤트 테이블';

-- =====================================================
-- 6. 문제 통계 뷰 (QuestionStatsView)
-- =====================================================
CREATE OR REPLACE VIEW question_stats_view AS
SELECT 
    q.question_id,
    q.question_type,
    q.major_category as category,
    q.difficulty_level,
    
    -- 기본 통계
    COUNT(qa.id) as total_solve_count,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as correct_solve_count,
    
    -- 정답률 (소수점 2자리)
    ROUND(
        (SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / 
         NULLIF(COUNT(qa.id), 0)), 2
    ) as correct_rate,
    
    -- 평균 풀이 시간 (초)
    ROUND(AVG(qa.time_spent), 2) as avg_solve_time,
    
    -- 사용자당 평균 시도 횟수
    ROUND(COUNT(qa.id) * 1.0 / NULLIF(COUNT(DISTINCT ls.user_id), 0), 2) as avg_solve_attempts_per_user,
    
    -- 고유 사용자 수
    COUNT(DISTINCT ls.user_id) as distinct_user_count
    
FROM question q
LEFT JOIN question_answer qa ON q.question_id = qa.question_id
LEFT JOIN learning_sessions ls ON qa.session_id = ls.session_id
GROUP BY q.question_id, q.question_type, q.major_category, q.difficulty_level;

-- =====================================================
-- 7. 사용자 학습 분석 뷰 (User Learning Analytics)
-- =====================================================
CREATE OR REPLACE VIEW user_learning_analytics AS
SELECT 
    ls.user_id,
    
    -- 기본 학습 통계
    COUNT(DISTINCT ls.session_id) as total_sessions,
    COUNT(qa.id) as total_questions_solved,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as total_correct_answers,
    
    -- 정답률 (%)
    ROUND(
        (SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / 
         NULLIF(COUNT(qa.id), 0)), 2
    ) as accuracy_rate,
    
    -- 오답률 (%)
    ROUND(
        (SUM(CASE WHEN qa.is_correct = 0 THEN 1 ELSE 0 END) * 100.0 / 
         NULLIF(COUNT(qa.id), 0)), 2
    ) as error_rate,
    
    -- 평균 풀이 시간 (초)
    ROUND(AVG(qa.time_spent), 2) as avg_solve_time,
    
    -- 문제 재시도율 (%)
    ROUND(
        (SUM(qa.solve_count - 1) * 100.0 / 
         NULLIF(COUNT(qa.id), 0)), 2
    ) as retry_rate,
    
    -- 학습 진도율 (완료된 세션 비율, %)
    ROUND(
        (COUNT(CASE WHEN ls.status = 'COMPLETED' THEN 1 END) * 100.0 / 
         NULLIF(COUNT(DISTINCT ls.session_id), 0)), 2
    ) as learning_progress_rate,
    
    -- 최근 학습일
    MAX(qa.answered_at) as last_learning_date,
    
    -- 총 학습 시간 (분)
    ROUND(SUM(qa.time_spent) / 60.0, 2) as total_learning_time_minutes
    
FROM learning_sessions ls
LEFT JOIN question_answer qa ON ls.session_id = qa.session_id
GROUP BY ls.user_id;

-- =====================================================
-- 8. 카테고리별 성과 뷰 (Category Performance View)
-- =====================================================
CREATE OR REPLACE VIEW category_performance_view AS
SELECT 
    ls.user_id,
    q.major_category,
    q.minor_category,
    
    -- 카테고리별 통계
    COUNT(qa.id) as questions_solved,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as correct_answers,
    
    -- 카테고리별 숙련도 (정답률, %)
    ROUND(
        (SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / 
         NULLIF(COUNT(qa.id), 0)), 2
    ) as category_proficiency,
    
    -- 카테고리별 평균 풀이 시간
    ROUND(AVG(qa.time_spent), 2) as avg_category_solve_time,
    
    -- 카테고리별 최고 성과일
    MAX(qa.answered_at) as last_category_practice_date
    
FROM learning_sessions ls
JOIN question_answer qa ON ls.session_id = qa.session_id
JOIN question q ON qa.question_id = q.question_id
GROUP BY ls.user_id, q.major_category, q.minor_category;

-- =====================================================
-- 9. 난이도별 성취도 뷰 (Difficulty Achievement View)
-- =====================================================
CREATE OR REPLACE VIEW difficulty_achievement_view AS
SELECT 
    ls.user_id,
    q.difficulty_level,
    
    -- 난이도별 통계
    COUNT(qa.id) as questions_solved,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as correct_answers,
    
    -- 난이도별 성취도 (정답률, %)
    ROUND(
        (SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / 
         NULLIF(COUNT(qa.id), 0)), 2
    ) as difficulty_achievement_rate,
    
    -- 난이도별 평균 풀이 시간
    ROUND(AVG(qa.time_spent), 2) as avg_difficulty_solve_time,
    
    -- 난이도별 도전 횟수
    ROUND(AVG(qa.solve_count), 2) as avg_attempts_per_question
    
FROM learning_sessions ls
JOIN question_answer qa ON ls.session_id = qa.session_id
JOIN question q ON qa.question_id = q.question_id
GROUP BY ls.user_id, q.difficulty_level;

-- =====================================================
-- DDL 스크립트 완료 (CREATE IF NOT EXISTS 방식)
-- 
-- 장점:
-- 1. 기존 테이블과 데이터 보존
-- 2. 개발 중 재실행 시 안전함
-- 3. 스키마 변경 시에만 수동으로 DROP 후 재생성
-- 4. VIEW는 CREATE OR REPLACE로 항상 최신 버전 적용
-- =====================================================