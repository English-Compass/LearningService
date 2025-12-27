-- =====================================================
-- 학습 서비스 데이터베이스 초기화 스크립트 (수정 완료)
-- 목적: 모든 테이블, 뷰, 인덱스 생성 (기존 테이블이 있으면 생성하지 않음)
-- =====================================================

-- 기존 뷰와 테이블 삭제는 제거하고 CREATE IF NOT EXISTS 방식 사용
-- DROP VIEW IF EXISTS question_stats_view;
-- DROP VIEW IF EXISTS user_learning_analytics_view;
-- DROP VIEW IF EXISTS category_performance_view;
-- DROP VIEW IF EXISTS difficulty_achievement_view;
-- DROP TABLE IF EXISTS learning_pattern_analysis;
-- DROP TABLE IF EXISTS learning_session_events;
-- DROP TABLE IF EXISTS user_selected_minor_categories;
-- DROP TABLE IF EXISTS user_interests;
-- DROP TABLE IF EXISTS user_profiles;
-- DROP TABLE IF EXISTS question_answer;
-- DROP TABLE IF EXISTS session_question;
-- DROP TABLE IF EXISTS learning_sessions;
-- DROP TABLE IF EXISTS question;

-- =====================================================
-- 1. 문제 테이블 (Question) - 제거됨
-- ProblemService에서 관리하므로 LearningService에서는 불필요
-- question_answer 테이블에 문제 메타데이터(questionType, category, difficulty) 저장
-- =====================================================
-- CREATE TABLE IF NOT EXISTS question (...) - 제거됨

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
    
    -- 인덱스
    INDEX idx_learning_sessions_user (user_id),
    INDEX idx_learning_sessions_status (status),
    INDEX idx_learning_sessions_type (session_type),
    INDEX idx_learning_sessions_user_status (user_id, status),
    INDEX idx_learning_sessions_created (created_at),
    INDEX idx_learning_sessions_completed (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학습 세션 테이블';

-- =====================================================
-- 3. 세션-문제 연결 테이블 (SessionQuestion) - 제거됨
-- ProblemService에서 관리하므로 불필요
-- =====================================================
-- CREATE TABLE IF NOT EXISTS session_question (...) - 제거됨

-- =====================================================
-- 4. 문제 답변 테이블 (QuestionAnswer)
-- =====================================================
CREATE TABLE IF NOT EXISTS question_answer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL COMMENT '세션 ID',
    question_id VARCHAR(255) NOT NULL COMMENT '문제 ID (ProblemService 참조)',
    session_type VARCHAR(50) NOT NULL COMMENT '세션 타입',
    
    -- ProblemService에서 받은 문제 메타데이터 (분석용)
    question_type VARCHAR(50) COMMENT '문제 유형',
    major_category VARCHAR(50) COMMENT '대분류',
    minor_category VARCHAR(50) COMMENT '소분류',
    difficulty_level INT COMMENT '난이도 (1~3)',
    
    -- 답변 정보
    user_answer VARCHAR(1) NOT NULL COMMENT '사용자 답변 (A, B, C)',
    is_correct BIT(1) NOT NULL COMMENT '정답 여부',
    time_spent INT COMMENT '풀이 시간 (초)',
    answered_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '답변 시간',
    solve_count INT NOT NULL DEFAULT 1 COMMENT '해당 문제 풀이 횟수',
    
    -- 외래키 제약조건 (learning_sessions만 참조, question은 ProblemService에서 관리)
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    
    -- 유니크 제약조건 (한 세션에서 같은 문제는 한 번만 답변)
    UNIQUE KEY uk_session_question_answer (session_id, question_id, session_type),
    
    -- 인덱스
    INDEX idx_question_answer_session (session_id),
    INDEX idx_question_answer_question (question_id),
    INDEX idx_question_answer_correct (is_correct),
    INDEX idx_question_answer_time (time_spent),
    INDEX idx_question_answer_answered_at (answered_at),
    INDEX idx_question_answer_type (question_type),
    INDEX idx_question_answer_major_category (major_category),
    INDEX idx_question_answer_minor_category (minor_category),
    INDEX idx_question_answer_difficulty (difficulty_level),
    INDEX idx_question_answer_combo (question_id, is_correct, time_spent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 문제 답변 테이블 (문제 메타데이터 포함)';

-- =====================================================
-- 5. 사용자 프로필 테이블 (UserProfile)
-- =====================================================
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id VARCHAR(255) NOT NULL PRIMARY KEY,
    learning_purpose VARCHAR(50) COMMENT '학습 목적',
    learning_style VARCHAR(50) COMMENT '학습 스타일',
    difficulty_preference VARCHAR(50) COMMENT '난이도 선호도',
    interests_version INT COMMENT '관심사 버전',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    interests_last_updated DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 프로필 테이블';

-- =====================================================
-- 6. 사용자 관심사 테이블 (UserInterests)
-- =====================================================
CREATE TABLE IF NOT EXISTS user_interests (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    interest VARCHAR(255) NOT NULL,
    
    UNIQUE KEY uk_user_interest (user_id, interest),
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 관심사 테이블';

-- =====================================================
-- 7. 사용자 선택 소분류 데이터 삽입 (UserSelectedMinorCategories)
-- =====================================================
CREATE TABLE IF NOT EXISTS user_selected_minor_categories (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    minor_category VARCHAR(255) NOT NULL,

    UNIQUE KEY uk_user_minor_category (user_id, minor_category),
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 선택 소분류 테이블';

-- =====================================================
-- 8. 학습 세션 이벤트 데이터 삽입 (LearningSessionEvent)
-- =====================================================
CREATE TABLE IF NOT EXISTS learning_session_events (
    event_id VARCHAR(255) NOT NULL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    session_type VARCHAR(50) NOT NULL,
    event_metadata TEXT COMMENT '이벤트 메타데이터 (JSON)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    -- 마이크로서비스 환경에서는 user_id 외래키 제약 조건 제거
    -- ProblemService에서 받은 userId가 user_profiles에 없을 수 있음
    -- FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
    
    -- 인덱스 추가
    INDEX idx_learning_session_events_user (user_id),
    INDEX idx_learning_session_events_session (session_id),
    INDEX idx_learning_session_events_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학습 세션 이벤트 테이블';

-- =====================================================
-- 9. 학습 패턴 분석 데이터 삽입 (LearningPatternAnalysis)
-- =====================================================
CREATE TABLE IF NOT EXISTS learning_pattern_analysis (
    analysis_id VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255),
    analysis_type VARCHAR(50) NOT NULL,
    start_date DATETIME(6) NOT NULL,
    end_date DATETIME(6) NOT NULL,
    analyzed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    basic_statistics TEXT COMMENT '기본 통계 (JSON)',
    learning_pattern TEXT COMMENT '학습 패턴 (JSON)',
    learning_pattern_info TEXT COMMENT '학습 패턴 상세 정보 (JSON)',
    learning_progress TEXT COMMENT '학습 진행도 (JSON)',
    performance_analysis TEXT COMMENT '성과 분석 (JSON)',
    question_type_performances TEXT COMMENT '문제 유형별 성과 (JSON)',

    -- 마이크로서비스 환경에서는 외래키 제약 조건 제거
    -- ProblemService에서 받은 userId가 user_profiles에 없을 수 있음
    -- FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE
    
    -- 인덱스 추가
    INDEX idx_learning_pattern_analysis_user (user_id),
    INDEX idx_learning_pattern_analysis_session (session_id),
    INDEX idx_learning_pattern_analysis_type (analysis_type),
    INDEX idx_learning_pattern_analysis_analyzed (analyzed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학습 패턴 분석 결과 테이블';


-- =====================================================
-- 10. 문제 통계 뷰 (QuestionStatsView)
-- Question 테이블 없이 question_answer의 메타데이터 사용
-- =====================================================
CREATE OR REPLACE VIEW question_stats_view AS
SELECT 
    qa.question_id,
    qa.question_type,
    qa.major_category as category,
    qa.difficulty_level,
    
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
    
FROM question_answer qa
LEFT JOIN learning_sessions ls ON qa.session_id = ls.session_id
GROUP BY qa.question_id, qa.question_type, qa.major_category, qa.difficulty_level;

-- =====================================================
-- 11. 사용자 학습 분석 뷰 (User Learning Analytics)
-- =====================================================
CREATE OR REPLACE VIEW user_learning_analytics_view AS
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
-- 12. 카테고리별 성과 뷰 (Category Performance View)
-- Question 테이블 없이 question_answer의 메타데이터 사용
-- =====================================================
CREATE OR REPLACE VIEW category_performance_view AS
SELECT 
    ls.user_id,
    qa.major_category,
    qa.minor_category,
    
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
GROUP BY ls.user_id, qa.major_category, qa.minor_category;

-- =====================================================
-- 13. 난이도별 성취도 뷰 (Difficulty Achievement View)
-- Question 테이블 없이 question_answer의 메타데이터 사용
-- =====================================================
CREATE OR REPLACE VIEW difficulty_achievement_view AS
SELECT 
    ls.user_id,
    qa.difficulty_level,
    
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
GROUP BY ls.user_id, qa.difficulty_level;

-- =====================================================
-- DDL 스크립트 완료
-- =====================================================
