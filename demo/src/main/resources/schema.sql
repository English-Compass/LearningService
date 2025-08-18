-- =====================================================
-- Learning Service Database Schema
-- 엔티티 기반 DDL 생성
-- =====================================================

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS learning_service
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE learning_service;

-- =====================================================
-- 1. 사용자 프로필 테이블 (UserService에서 관리)
-- =====================================================
CREATE TABLE user_profiles (
    user_id VARCHAR(255) NOT NULL PRIMARY KEY,
    learning_purpose ENUM('STUDY', 'BUSINESS', 'TRAVEL', 'DAILY_LIFE') NOT NULL,
    business_domain VARCHAR(255),
    language_goal VARCHAR(255),
    difficulty_preference ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'),
    learning_style ENUM('VISUAL', 'AUDITORY', 'KINESTHETIC', 'READING', 'MIXED'),
    interests_last_updated DATETIME(6),
    interests_version INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    
    INDEX idx_user_profiles_learning_purpose (learning_purpose),
    INDEX idx_user_profiles_difficulty (difficulty_preference),
    INDEX idx_user_profiles_style (learning_style)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. 사용자 관심사 테이블 (1:N 관계)
-- =====================================================
CREATE TABLE user_interests (
    user_id VARCHAR(255) NOT NULL,
    interest VARCHAR(255) NOT NULL,
    
    PRIMARY KEY (user_id, interest),
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE,
    
    INDEX idx_user_interests_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. 사용자 선택 소분류 테이블 (1:N 관계)
-- =====================================================
CREATE TABLE user_selected_minor_categories (
    user_id VARCHAR(255) NOT NULL,
    minor_category ENUM(
        'CLASS_LISTENING', 'DEPARTMENT_CONVERSATION', 'ASSIGNMENT_EXAM',
        'MEETING_CONFERENCE', 'CUSTOMER_SERVICE', 'EMAIL_REPORT',
        'BACKPACKING', 'FAMILY_TRIP', 'FRIEND_TRIP',
        'SHOPPING_DINING', 'HOSPITAL_VISIT', 'PUBLIC_TRANSPORT'
    ) NOT NULL,
    
    PRIMARY KEY (user_id, minor_category),
    FOREIGN KEY (user_id) REFERENCES user_profiles(user_id) ON DELETE CASCADE,
    
    INDEX idx_user_minor_categories_user_id (user_id),
    INDEX idx_user_minor_categories_category (minor_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. 문제 테이블 (핵심 문제 정보)
-- =====================================================
CREATE TABLE questions (
    question_id VARCHAR(255) NOT NULL PRIMARY KEY,
    question_text TEXT NOT NULL,
    
    -- 보기(선택지) 필드들
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    correct_answer VARCHAR(255) NOT NULL,
    
    -- 문제 분류 필드들
    major_category VARCHAR(255) NOT NULL,
    minor_category VARCHAR(255) NOT NULL,
    question_type VARCHAR(255) NOT NULL,
    
    -- 추가 정보
    explanation TEXT,
    difficulty INT NOT NULL DEFAULT 1, -- 1: 쉬움, 2: 보통, 3: 어려움
    points_per_question INT NOT NULL DEFAULT 10,
    tags VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    INDEX idx_questions_major_category (major_category),
    INDEX idx_questions_minor_category (minor_category),
    INDEX idx_questions_question_type (question_type),
    INDEX idx_questions_difficulty (difficulty),
    INDEX idx_questions_category_difficulty (major_category, minor_category, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. 학습 세션 테이블 (핵심 테이블)
-- =====================================================
CREATE TABLE learning_sessions (
    session_id VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6),
    
    -- 시간 관련 필드 (학습 패턴 분석용)
    session_duration_seconds INT, -- 세션 총 소요 시간(초)
    session_duration_minutes DECIMAL(5,2), -- 세션 총 소요 시간(분)
    avg_time_per_question DECIMAL(5,2), -- 문제별 평균 풀이 시간(초)
    longest_question_time INT, -- 가장 오래 걸린 문제 시간(초)
    shortest_question_time INT, -- 가장 빨리 푼 문제 시간(초)
    
    -- 기존 필드들
    total_questions INT NOT NULL DEFAULT 10,
    answered_questions INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    wrong_answers INT NOT NULL DEFAULT 0,
    score INT,
    status ENUM('STARTED', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'ABANDONED') NOT NULL,
    session_metadata TEXT,
    session_type ENUM('PRACTICE', 'REVIEW', 'WRONG_ANSWER') NOT NULL,
    last_updated_at DATETIME(6) NOT NULL,
    
    INDEX idx_learning_sessions_user_id (user_id),
    INDEX idx_learning_sessions_status (status),
    INDEX idx_learning_sessions_type (session_type),
    INDEX idx_learning_sessions_started_at (started_at),
    INDEX idx_learning_sessions_completed_at (completed_at),
    INDEX idx_learning_sessions_user_status (user_id, status),
    INDEX idx_learning_sessions_duration (session_duration_minutes),
    INDEX idx_learning_sessions_user_duration (user_id, session_duration_minutes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. 세션-문제 연결 테이블 (세션별 문제 할당 및 순서 관리)
-- =====================================================
CREATE TABLE session_questions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    question_id VARCHAR(255) NOT NULL,
    question_order INT NOT NULL, -- 세션 내 문제 순서 (1, 2, 3, ...)
    is_answered BIT(1) NOT NULL DEFAULT 0, -- 답변 완료 여부
    assigned_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), -- 문제 할당 시간
    
    -- 외래키 제약조건
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    
    -- 유니크 제약조건 (한 세션에서 같은 문제는 한 번만)
    UNIQUE KEY uk_session_question (session_id, question_id),
    
    INDEX idx_session_questions_session_id (session_id),
    INDEX idx_session_questions_question_id (question_id),
    INDEX idx_session_questions_order (session_id, question_order),
    INDEX idx_session_questions_answered (session_id, is_answered)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. 문제 답변 테이블 (사용자 답변 정보)
-- =====================================================
CREATE TABLE question_answers (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    question_id VARCHAR(255) NOT NULL,
    
    -- 사용자 답변 정보
    user_answer VARCHAR(255) NOT NULL,
    is_correct BIT(1) NOT NULL,
    time_spent INT, -- 문제 풀이 시간(초)
    answered_at DATETIME(6) NOT NULL,
    
    -- 추가 정보
    user_notes TEXT,
    earned_points INT NOT NULL DEFAULT 0,
    
    -- 외래키 제약조건
    FOREIGN KEY (session_id) REFERENCES learning_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    
    -- 유니크 제약조건 (한 세션에서 같은 문제는 한 번만 답변)
    UNIQUE KEY uk_session_question_answer (session_id, question_id),
    
    INDEX idx_question_answers_session_id (session_id),
    INDEX idx_question_answers_question_id (question_id),
    INDEX idx_question_answers_is_correct (is_correct),
    INDEX idx_question_answers_answered_at (answered_at),
    INDEX idx_question_answers_session_correct (session_id, is_correct)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. 인덱스 최적화
-- =====================================================

-- 복합 인덱스 (자주 함께 조회되는 컬럼들)
CREATE INDEX idx_learning_sessions_user_type_status 
ON learning_sessions(user_id, session_type, status);

CREATE INDEX idx_session_questions_session_order 
ON session_questions(session_id, question_order);

CREATE INDEX idx_user_profiles_purpose_style 
ON user_profiles(learning_purpose, learning_style);

-- =====================================================
-- 9. 뷰 생성 (자주 조회되는 데이터)
-- =====================================================

-- 사용자별 학습 통계 뷰 (실제 문제 답변 데이터 기반)
CREATE VIEW user_learning_stats AS
SELECT 
    ls.user_id,
    COUNT(DISTINCT ls.session_id) as total_sessions,
    COUNT(DISTINCT CASE WHEN ls.status = 'COMPLETED' THEN ls.session_id END) as completed_sessions,
    COUNT(DISTINCT CASE WHEN ls.status = 'ABANDONED' THEN ls.session_id END) as abandoned_sessions,
    
    -- 실제 문제 답변 데이터 기반 통계
    COUNT(qa.id) as total_questions_attempted,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as total_correct_answers,
    SUM(CASE WHEN qa.is_correct = 0 THEN 1 ELSE 0 END) as total_wrong_answers,
    
    -- 정답률 계산
    CASE 
        WHEN COUNT(qa.id) > 0 
        THEN ROUND(SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(qa.id), 2)
        ELSE 0 
    END as accuracy_percentage,
    
    -- 평균 문제 풀이 시간 (초)
    ROUND(AVG(qa.time_spent), 0) as avg_time_per_question,
    
    -- 총 학습 시간 (분)
    ROUND(SUM(qa.time_spent) / 60.0, 1) as total_learning_time_minutes,
    
    -- 마지막 학습 날짜
    MAX(ls.started_at) as last_learning_date,
    
    -- 세션별 평균 점수
    ROUND(AVG(ls.score), 1) as avg_session_score,
    
    -- 문제 유형별 통계
    COUNT(DISTINCT CASE WHEN q.question_type = 'FILL_IN_THE_BLANK' THEN qa.id END) as fill_blank_questions,
    COUNT(DISTINCT CASE WHEN q.question_type = 'IDIOM_IN_CONTEXT' THEN qa.id END) as idiom_questions,
    COUNT(DISTINCT CASE WHEN q.question_type = 'SENTENCE_COMPLETION' THEN qa.id END) as sentence_completion_questions,
    
    -- 소분류별 통계
    COUNT(DISTINCT CASE WHEN q.minor_category = 'MEETING_CONFERENCE' THEN qa.id END) as meeting_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'CUSTOMER_SERVICE' THEN qa.id END) as customer_service_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'EMAIL_REPORT' THEN qa.id END) as email_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'CLASS_LISTENING' THEN qa.id END) as class_listening_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'ASSIGNMENT_EXAM' THEN qa.id END) as assignment_exam_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'BACKPACKING' THEN qa.id END) as backpacking_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'FAMILY_TRIP' THEN qa.id END) as family_trip_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'SHOPPING_DINING' THEN qa.id END) as shopping_dining_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'HOSPITAL_VISIT' THEN qa.id END) as hospital_visit_questions,
    COUNT(DISTINCT CASE WHEN q.minor_category = 'PUBLIC_TRANSPORT' THEN qa.id END) as public_transport_questions
    
FROM learning_sessions ls
LEFT JOIN question_answers qa ON ls.session_id = qa.session_id
LEFT JOIN questions q ON qa.question_id = q.question_id
GROUP BY ls.user_id;

-- 세션별 문제 할당 현황 뷰
CREATE VIEW session_question_assignment AS
SELECT 
    ls.session_id,
    ls.user_id,
    ls.session_type,
    ls.status,
    ls.total_questions,
    sq.question_order,
    q.question_id,
    q.question_text,
    q.major_category,
    q.minor_category,
    q.question_type,
    q.difficulty,
    sq.is_answered,
    sq.assigned_at,
    qa.user_answer,
    qa.is_correct,
    qa.time_spent,
    qa.answered_at
FROM learning_sessions ls
JOIN session_questions sq ON ls.session_id = sq.session_id
JOIN questions q ON sq.question_id = q.question_id
LEFT JOIN question_answers qa ON ls.session_id = qa.session_id AND sq.question_id = qa.question_id
ORDER BY ls.session_id, sq.question_order;

-- 사용자별 소분류별 성과 뷰
CREATE VIEW user_category_performance AS
SELECT 
    ls.user_id,
    q.minor_category,
    q.major_category,
    COUNT(qa.id) as total_questions,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as correct_answers,
    SUM(CASE WHEN qa.is_correct = 0 THEN 1 ELSE 0 END) as wrong_answers,
    ROUND(SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(qa.id), 2) as accuracy_percentage,
    ROUND(AVG(qa.time_spent), 0) as avg_time_per_question,
    MAX(qa.answered_at) as last_attempt_date
FROM learning_sessions ls
JOIN question_answers qa ON ls.session_id = qa.session_id
JOIN questions q ON qa.question_id = q.question_id
WHERE ls.status = 'COMPLETED'
GROUP BY ls.user_id, q.minor_category, q.major_category;

-- 사용자별 문제 유형별 성과 뷰
CREATE VIEW user_question_type_performance AS
SELECT 
    ls.user_id,
    q.question_type,
    COUNT(qa.id) as total_questions,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as correct_answers,
    SUM(CASE WHEN qa.is_correct = 0 THEN 1 ELSE 0 END) as wrong_answers,
    ROUND(SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(qa.id), 2) as accuracy_percentage,
    ROUND(AVG(qa.time_spent), 0) as avg_time_per_question,
    MAX(qa.answered_at) as last_attempt_date
FROM learning_sessions ls
JOIN question_answers qa ON ls.session_id = qa.session_id
JOIN questions q ON qa.question_id = q.question_id
WHERE ls.status = 'COMPLETED'
GROUP BY ls.user_id, q.question_type;

-- 사용자별 학습 시간 패턴 분석 뷰
CREATE VIEW user_learning_time_patterns AS
SELECT 
    ls.user_id,
    ls.session_type,
    
    -- 세션 시간 패턴
    COUNT(ls.session_id) as total_sessions,
    ROUND(AVG(ls.session_duration_minutes), 2) as avg_session_duration,
    MIN(ls.session_duration_minutes) as min_session_duration,
    MAX(ls.session_duration_minutes) as max_session_duration,
    
    -- 문제 풀이 시간 패턴
    ROUND(AVG(ls.avg_time_per_question), 2) as avg_time_per_question,
    ROUND(AVG(ls.longest_question_time), 2) as avg_longest_question_time,
    ROUND(AVG(ls.shortest_question_time), 2) as avg_shortest_question_time,
    
    -- 시간대별 학습 패턴
    HOUR(ls.started_at) as learning_hour,
    COUNT(*) as sessions_at_hour,
    ROUND(AVG(ls.score), 1) as avg_score_at_hour,
    ROUND(AVG(ls.session_duration_minutes), 2) as avg_duration_at_hour
    
FROM learning_sessions ls
WHERE ls.status = 'COMPLETED' AND ls.session_duration_minutes IS NOT NULL
GROUP BY ls.user_id, ls.session_type, HOUR(ls.started_at)
ORDER BY ls.user_id, ls.session_type, learning_hour;

-- 사용자별 학습 효율성 분석 뷰
CREATE VIEW user_learning_efficiency AS
SELECT 
    ls.user_id,
    
    -- 전체 학습 통계
    COUNT(DISTINCT ls.session_id) as total_sessions,
    SUM(ls.session_duration_minutes) as total_learning_time_minutes,
    SUM(ls.answered_questions) as total_questions_answered,
    
    -- 효율성 지표
    ROUND(SUM(ls.answered_questions) / SUM(ls.session_duration_minutes), 3) as questions_per_minute,
    ROUND(SUM(ls.correct_answers) / SUM(ls.session_duration_minutes), 3) as correct_answers_per_minute,
    
    -- 시간 대비 성과
    ROUND(AVG(ls.score), 1) as overall_avg_score,
    ROUND(AVG(ls.session_duration_minutes), 2) as overall_avg_session_duration,
    
    -- 학습 스타일 분류
    CASE 
        WHEN AVG(ls.session_duration_minutes) < 15 THEN 'Fast Learner'
        WHEN AVG(ls.session_duration_minutes) < 30 THEN 'Balanced Learner'
        ELSE 'Thorough Learner'
    END as learning_style_category,
    
    -- 집중력 지표
    CASE 
        WHEN AVG(ls.avg_time_per_question) < 30 THEN 'Quick Problem Solver'
        WHEN AVG(ls.avg_time_per_question) < 60 THEN 'Moderate Problem Solver'
        ELSE 'Careful Problem Solver'
    END as problem_solving_style
    
FROM learning_sessions ls
WHERE ls.status = 'COMPLETED' AND ls.session_duration_minutes IS NOT NULL
GROUP BY ls.user_id;

-- 세션별 상세 시간 분석 뷰
CREATE VIEW session_time_analysis AS
SELECT 
    ls.session_id,
    ls.user_id,
    ls.session_type,
    ls.status,
    
    -- 세션 시간 정보
    ls.started_at,
    ls.completed_at,
    ls.session_duration_minutes,
    ls.session_duration_seconds,
    
    -- 문제 풀이 시간 분석
    ls.avg_time_per_question,
    ls.longest_question_time,
    ls.shortest_question_time,
    
    -- 시간 효율성
    ls.total_questions,
    ls.answered_questions,
    ls.correct_answers,
    ls.score,
    
    -- 시간 대비 성과
    CASE 
        WHEN ls.session_duration_minutes > 0 
        THEN ROUND(ls.score / ls.session_duration_minutes, 2)
        ELSE 0 
    END as score_per_minute,
    
    -- 문제당 평균 시간 vs 세션 평균 시간
    CASE 
        WHEN ls.avg_time_per_question > 0 
        THEN ROUND(ls.session_duration_minutes / ls.total_questions, 2)
        ELSE 0 
    END as time_per_question_calculated
    
FROM learning_sessions ls
WHERE ls.session_duration_minutes IS NOT NULL
ORDER BY ls.user_id, ls.started_at DESC;

-- =====================================================
-- 10. 초기 데이터 삽입 (테스트용)
-- =====================================================

-- 테스트 사용자 프로필
INSERT INTO user_profiles (user_id, learning_purpose, business_domain, language_goal, 
                          difficulty_preference, learning_style, interests_version, 
                          created_at, updated_at) VALUES
('user001', 'BUSINESS', 'IT', '비즈니스 영어', 'INTERMEDIATE', 'VISUAL', 1, 
 NOW(), NOW()),
('user002', 'STUDY', '컴퓨터공학', '학업 영어', 'BEGINNER', 'MIXED', 1, 
 NOW(), NOW()),
('user003', 'TRAVEL', '여행사', '여행 영어', 'ADVANCED', 'AUDITORY', 1, 
 NOW(), NOW());

-- 테스트 사용자 관심사
INSERT INTO user_interests (user_id, interest) VALUES
('user001', 'IT 기술'),
('user001', '비즈니스 회의'),
('user002', '프로그래밍'),
('user002', '학술 논문'),
('user003', '여행 계획'),
('user003', '문화 교류');

-- 테스트 사용자 선택 소분류
INSERT INTO user_selected_minor_categories (user_id, minor_category) VALUES
('user001', 'MEETING_CONFERENCE'),
('user001', 'CUSTOMER_SERVICE'),
('user001', 'EMAIL_REPORT'),
('user002', 'CLASS_LISTENING'),
('user002', 'ASSIGNMENT_EXAM'),
('user003', 'BACKPACKING'),
('user003', 'FAMILY_TRIP');

-- 테스트 문제 데이터
INSERT INTO questions (question_id, question_text, option_a, option_b, option_c, correct_answer,
                      major_category, minor_category, question_type, difficulty, explanation) VALUES
('q001', '회의에서 "Let me think about it"의 의미는?', '생각해보겠습니다', '동의합니다', '반대합니다', 'A', 
 'BUSINESS', 'MEETING_CONFERENCE', 'FILL_IN_THE_BLANK', 1, '회의에서 시간을 달라고 할 때 사용하는 표현입니다.'),
('q002', '고객 응대 시 "I understand your concern"은?', '고객님의 우려를 이해합니다', '고객님을 도와드리겠습니다', '고객님의 요청을 거절합니다', 'A',
 'BUSINESS', 'CUSTOMER_SERVICE', 'SENTENCE_COMPLETION', 2, '고객의 불만이나 우려에 공감을 표현하는 문장입니다.'),
('q003', '이메일에서 "Please find attached" 다음에 오는 것은?', '첨부파일', '회의 일정', '문서 요약', 'A',
 'BUSINESS', 'EMAIL_REPORT', 'IDIOM_IN_CONTEXT', 1, '이메일에서 첨부파일을 언급할 때 사용하는 표현입니다.'),
('q004', '학과 대화에서 "What\'s your major?"의 답변은?', '컴퓨터공학입니다', '영어를 공부합니다', '수학을 좋아합니다', 'A',
 'STUDY', 'DEPARTMENT_CONVERSATION', 'FILL_IN_THE_BLANK', 1, '전공을 묻는 질문에 대한 답변입니다.'),
('q005', '여행 계획에서 "How long are you staying?"의 의미는?', '얼마나 머무를 예정인가요?', '어디에 가실 건가요?', '언제 출발하시나요?', 'A',
 'TRAVEL', 'BACKPACKING', 'SENTENCE_COMPLETION', 2, '체류 기간을 묻는 질문입니다.');

-- =====================================================
-- 11. 권한 설정 (필요시)
-- =====================================================

-- 애플리케이션 사용자 생성 (보안 강화시)
-- CREATE USER 'learning_app'@'localhost' IDENTIFIED BY 'secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON learning_service.* TO 'learning_app'@'localhost';
-- FLUSH PRIVILEGES;

-- =====================================================
-- 스키마 생성 완료
-- =====================================================
