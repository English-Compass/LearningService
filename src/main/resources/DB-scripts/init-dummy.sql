-- =====================================================
-- 학습 서비스 더미 데이터 삽입 스크립트
-- 생성일: 2024년
-- 목적: 학습 통계 지표 테스트를 위한 더미 데이터 생성
-- =====================================================



-- =====================================================
-- 1. 문제 데이터 삽입 (Question)
-- DDL 스키마 컬럼 순서: question_id, question_text, option_a, option_b, option_c, 
--          correct_answer, major_category, minor_category, question_type, explanation, 
--          difficulty_level, created_at, updated_at
-- =====================================================

-- 비즈니스 영어 - 회의/컨퍼런스 문제들
INSERT INTO question VALUES
('Q001', '다음 중 회의에서 사용하는 표현으로 가장 적절한 것은?', 
 'Let''s call it a day', 'Let''s table this discussion', 'Let''s break a leg', 
 'B', 'BUSINESS', 'MEETING_CONFERENCE', 'FILL_IN_THE_BLANK', 
 '회의에서 "table this discussion"은 "이 논의를 나중으로 미루다"라는 의미입니다.', 
 1, NOW(), NOW()),

('Q002', 'The meeting agenda _____ be distributed before the conference.', 
 'should', 'could', 'would', 
 'A', 'BUSINESS', 'MEETING_CONFERENCE', 'FILL_IN_THE_BLANK', 
 '회의 안건은 회의 전에 배포되어야 하므로 "should"가 적절합니다.', 
 2, NOW(), NOW()),

('Q003', '다음 문장에서 "break the ice"의 의미는?', 
 '얼음을 깨뜨리다', '분위기를 부드럽게 만들다', '계약을 파기하다', 
 'B', 'BUSINESS', 'MEETING_CONFERENCE', 'IDIOM_IN_CONTEXT', 
 '"break the ice"는 어색한 분위기를 부드럽게 만든다는 의미의 관용구입니다.', 
 1, NOW(), NOW()),

-- 비즈니스 영어 - 고객 서비스 문제들
('Q004', 'How may I _____ you today?', 
 'help', 'serve', 'assist', 
 'C', 'BUSINESS', 'CUSTOMER_SERVICE', 'FILL_IN_THE_BLANK', 
 '고객 서비스에서는 "How may I assist you?"가 가장 정중한 표현입니다.', 
 1, NOW(), NOW()),

('Q005', '고객 불만 처리 시 사용하는 표현으로 적절한 것은?', 
 'That''s not my problem', 'I understand your concern', 'You are wrong', 
 'B', 'BUSINESS', 'CUSTOMER_SERVICE', 'FILL_IN_THE_BLANK', 
 '고객 불만 처리 시에는 공감을 표하는 "I understand your concern"이 적절합니다.', 
 2, NOW(), NOW()),

-- 학습 영어 - 기초 문법 문제들
('Q006', 'I _____ to school every day.', 
 'go', 'goes', 'going', 
 'A', 'STUDY', 'BASIC_GRAMMAR', 'FILL_IN_THE_BLANK', 
 '주어가 "I"일 때는 동사 원형을 사용합니다.', 
 1, NOW(), NOW()),

('Q007', 'She _____ her homework yesterday.', 
 'do', 'did', 'does', 
 'B', 'STUDY', 'BASIC_GRAMMAR', 'FILL_IN_THE_BLANK', 
 '"yesterday"는 과거를 나타내므로 과거형 "did"를 사용합니다.', 
 2, NOW(), NOW()),

-- 학습 영어 - 어휘 확장 문제들
('Q008', 'The word "enormous" means:', 
 'very small', 'very large', 'medium size', 
 'B', 'STUDY', 'VOCABULARY_EXPANSION', 'SYNONYM_SELECTION', 
 '"enormous"는 "매우 큰"이라는 의미입니다.', 
 2, NOW(), NOW()),

('Q009', 'Choose the synonym of "happy":', 
 'sad', 'joyful', 'angry', 
 'B', 'STUDY', 'VOCABULARY_EXPANSION', 'SYNONYM_SELECTION', 
 '"happy"의 동의어는 "joyful"입니다.', 
 1, NOW(), NOW()),

-- 발음 문제들
('Q010', 'Which word has the /θ/ sound?', 
 'this', 'think', 'that', 
 'B', 'STUDY', 'BASIC_GRAMMAR', 'PRONUNCIATION_RECOGNITION', 
 '"think"에는 무성음 /θ/ 소리가 포함되어 있습니다.', 
 3, NOW(), NOW());

-- =====================================================
-- 2. 학습 세션 데이터 삽입 (LearningSession)
-- DDL 스키마 컬럼 순서: session_id, user_id, created_at, started_at, completed_at, 
--          updated_at, status, session_type, session_metadata
-- =====================================================

INSERT INTO learning_sessions VALUES
-- 사용자 user001의 세션들
('SESSION001', 'user001', NOW(), '2024-01-15 09:00:00', '2024-01-15 09:25:00', NOW(),
 'COMPLETED', 'PRACTICE', '{"targetCategory": "BUSINESS", "difficulty": 1}'),

('SESSION002', 'user001', NOW(), '2024-01-16 14:00:00', '2024-01-16 14:18:00', NOW(),
 'COMPLETED', 'REVIEW', '{"reviewType": "WRONG_ANSWERS"}'),

('SESSION003', 'user001', NOW(), '2024-01-17 10:00:00', NULL, NOW(),
 'IN_PROGRESS', 'PRACTICE', '{"targetCategory": "STUDY", "difficulty": 2}'),

-- 사용자 user002의 세션들
('SESSION004', 'user002', NOW(), '2024-01-15 11:00:00', '2024-01-15 11:30:00', NOW(),
 'COMPLETED', 'PRACTICE', '{"targetCategory": "BUSINESS", "difficulty": 2}'),

('SESSION005', 'user002', NOW(), '2024-01-16 16:00:00', '2024-01-16 16:15:00', NOW(),
 'COMPLETED', 'WRONG_ANSWER', '{"focusArea": "GRAMMAR"}'),

-- 사용자 user003의 세션들  
('SESSION006', 'user003', NOW(), '2024-01-14 13:00:00', '2024-01-14 13:40:00', NOW(),
 'COMPLETED', 'PRACTICE', '{"targetCategory": "STUDY", "difficulty": 1}'),

('SESSION007', 'user003', NOW(), '2024-01-17 15:00:00', NULL, NOW(),
 'IN_PROGRESS', 'REVIEW', '{"reviewType": "VOCABULARY"}');

-- =====================================================
-- 3. 세션-문제 연결 데이터 삽입 (SessionQuestion)
-- =====================================================

INSERT INTO session_question (session_id, question_id, question_order) VALUES
-- SESSION001 (user001의 비즈니스 연습)
('SESSION001', 'Q001', 1),
('SESSION001', 'Q002', 2),
('SESSION001', 'Q003', 3),
('SESSION001', 'Q004', 4),
('SESSION001', 'Q005', 5),

-- SESSION002 (user001의 복습)
('SESSION002', 'Q002', 1),
('SESSION002', 'Q005', 2),
('SESSION002', 'Q007', 3),

-- SESSION003 (user001의 진행 중 - 학습)
('SESSION003', 'Q006', 1),
('SESSION003', 'Q007', 2),
('SESSION003', 'Q008', 3),
('SESSION003', 'Q009', 4),

-- SESSION004 (user002의 비즈니스 연습)
('SESSION004', 'Q001', 1),
('SESSION004', 'Q002', 2),
('SESSION004', 'Q003', 3),
('SESSION004', 'Q004', 4),
('SESSION004', 'Q005', 5),

-- SESSION005 (user002의 오답 노트)
('SESSION005', 'Q006', 1),
('SESSION005', 'Q007', 2),
('SESSION005', 'Q010', 3),

-- SESSION006 (user003의 학습 연습)
('SESSION006', 'Q006', 1),
('SESSION006', 'Q007', 2),
('SESSION006', 'Q008', 3),
('SESSION006', 'Q009', 4),
('SESSION006', 'Q010', 5),
('SESSION006', 'Q001', 6),

-- SESSION007 (user003의 진행 중 - 어휘 복습)
('SESSION007', 'Q008', 1),
('SESSION007', 'Q009', 2),
('SESSION007', 'Q001', 3),
('SESSION007', 'Q003', 4);

-- =====================================================
-- 4. 문제 답변 데이터 삽입 (QuestionAnswer)
-- =====================================================

INSERT INTO question_answer (session_id, question_id, session_type, user_answer, is_correct, time_spent, answered_at, solve_count) VALUES

-- SESSION001 (user001) - 비즈니스 연습: 4/5 정답 (80% 정답률)
('SESSION001', 'Q001', 'PRACTICE', 'B', 1, 45, '2024-01-15 09:07:00', 1),  -- 정답
('SESSION001', 'Q002', 'PRACTICE', 'C', 0, 60, '2024-01-15 09:09:00', 1),  -- 오답 (정답: A)
('SESSION001', 'Q003', 'PRACTICE', 'B', 1, 30, '2024-01-15 09:11:00', 1),  -- 정답
('SESSION001', 'Q004', 'PRACTICE', 'C', 1, 40, '2024-01-15 09:13:00', 1),  -- 정답
('SESSION001', 'Q005', 'PRACTICE', 'B', 1, 50, '2024-01-15 09:15:00', 1),  -- 정답

-- SESSION002 (user001) - 복습: 2/3 정답 (67% 정답률, 재시도 많음)
('SESSION002', 'Q002', 'REVIEW', 'A', 1, 40, '2024-01-16 14:05:00', 2),    -- 정답 (재시도)
('SESSION002', 'Q005', 'REVIEW', 'A', 0, 55, '2024-01-16 14:10:00', 2),    -- 오답 (정답: B, 재시도)
('SESSION002', 'Q007', 'REVIEW', 'B', 1, 35, '2024-01-16 14:14:00', 1),    -- 정답

-- SESSION003 (user001) - 진행 중: 1/2 정답 (50% 정답률)
('SESSION003', 'Q006', 'PRACTICE', 'A', 1, 25, '2024-01-17 10:08:00', 1),  -- 정답
('SESSION003', 'Q007', 'PRACTICE', 'A', 0, 45, '2024-01-17 10:12:00', 1),  -- 오답 (정답: B)

-- SESSION004 (user002) - 비즈니스 연습: 3/5 정답 (60% 정답률)
('SESSION004', 'Q001', 'PRACTICE', 'B', 1, 35, '2024-01-15 11:05:00', 1),  -- 정답
('SESSION004', 'Q002', 'PRACTICE', 'A', 1, 40, '2024-01-15 11:08:00', 1),  -- 정답
('SESSION004', 'Q003', 'PRACTICE', 'A', 0, 50, '2024-01-15 11:12:00', 1),  -- 오답 (정답: B)
('SESSION004', 'Q004', 'PRACTICE', 'B', 0, 45, '2024-01-15 11:16:00', 1),  -- 오답 (정답: C)
('SESSION004', 'Q005', 'PRACTICE', 'B', 1, 60, '2024-01-15 11:20:00', 1),  -- 정답

-- SESSION005 (user002) - 오답 노트: 3/3 정답 (100% 정답률, 빠른 풀이)
('SESSION005', 'Q006', 'WRONG_ANSWER', 'A', 1, 20, '2024-01-16 16:03:00', 1),  -- 정답
('SESSION005', 'Q007', 'WRONG_ANSWER', 'B', 1, 25, '2024-01-16 16:06:00', 1),  -- 정답  
('SESSION005', 'Q010', 'WRONG_ANSWER', 'B', 1, 30, '2024-01-16 16:09:00', 1),  -- 정답

-- SESSION006 (user003) - 학습 연습: 2/6 정답 (33% 정답률, 느린 풀이)
('SESSION006', 'Q006', 'PRACTICE', 'B', 0, 90, '2024-01-14 13:08:00', 1),   -- 오답 (정답: A)
('SESSION006', 'Q007', 'PRACTICE', 'A', 0, 85, '2024-01-14 13:15:00', 1),   -- 오답 (정답: B)
('SESSION006', 'Q008', 'PRACTICE', 'B', 1, 70, '2024-01-14 13:22:00', 1),   -- 정답
('SESSION006', 'Q009', 'PRACTICE', 'A', 0, 80, '2024-01-14 13:28:00', 1),   -- 오답 (정답: B)
('SESSION006', 'Q010', 'PRACTICE', 'A', 0, 95, '2024-01-14 13:35:00', 1),   -- 오답 (정답: B)
('SESSION006', 'Q001', 'PRACTICE', 'B', 1, 75, '2024-01-14 13:40:00', 1),   -- 정답

-- SESSION007 (user003) - 진행 중 어휘 복습: 0/1 정답 (0% 정답률)
('SESSION007', 'Q008', 'REVIEW', 'A', 0, 60, '2024-01-17 15:10:00', 1);     -- 오답 (정답: B)

-- =====================================================
-- 5. 추가 사용자들의 데이터 (통계 다양성을 위해)
-- =====================================================

-- 추가 세션들 삽입
INSERT INTO learning_sessions VALUES
('SESSION008', 'user004', NOW(), '2024-01-18 09:00:00', '2024-01-18 09:20:00', NOW(),
 'COMPLETED', 'PRACTICE', '{"targetCategory": "BUSINESS", "difficulty": 3}'),

('SESSION009', 'user005', NOW(), '2024-01-18 11:00:00', '2024-01-18 11:25:00', NOW(),
 'COMPLETED', 'PRACTICE', '{"targetCategory": "STUDY", "difficulty": 1}'),

('SESSION010', 'user006', NOW(), '2024-01-18 14:00:00', '2024-01-18 14:35:00', NOW(),
 'COMPLETED', 'REVIEW', '{"reviewType": "MIXED"}');

-- 추가 세션-문제 연결
INSERT INTO session_question (session_id, question_id, question_order) VALUES
('SESSION008', 'Q002', 1), ('SESSION008', 'Q005', 2), ('SESSION008', 'Q008', 3), ('SESSION008', 'Q010', 4),
('SESSION009', 'Q006', 1), ('SESSION009', 'Q007', 2), ('SESSION009', 'Q008', 3), ('SESSION009', 'Q009', 4), ('SESSION009', 'Q001', 5),
('SESSION010', 'Q001', 1), ('SESSION010', 'Q003', 2), ('SESSION010', 'Q006', 3), ('SESSION010', 'Q008', 4), ('SESSION010', 'Q009', 5), ('SESSION010', 'Q010', 6);

-- 추가 답변 데이터 (다양한 패턴 생성)
INSERT INTO question_answer (session_id, question_id, session_type, user_answer, is_correct, time_spent, answered_at, solve_count) VALUES

-- SESSION008 (user004) - 상급 비즈니스: 3/4 정답, 빠른 풀이 (고급자)
('SESSION008', 'Q002', 'PRACTICE', 'A', 1, 25, '2024-01-18 09:05:00', 1),
('SESSION008', 'Q005', 'PRACTICE', 'B', 1, 20, '2024-01-18 09:08:00', 1),
('SESSION008', 'Q008', 'PRACTICE', 'A', 0, 30, '2024-01-18 09:12:00', 1),  -- 오답 (정답: B)
('SESSION008', 'Q010', 'PRACTICE', 'B', 1, 35, '2024-01-18 09:16:00', 1),

-- SESSION009 (user005) - 초급 학습: 5/5 정답, 빠른 풀이 (우수 학습자)
('SESSION009', 'Q006', 'PRACTICE', 'A', 1, 15, '2024-01-18 11:03:00', 1),
('SESSION009', 'Q007', 'PRACTICE', 'B', 1, 18, '2024-01-18 11:06:00', 1),
('SESSION009', 'Q008', 'PRACTICE', 'B', 1, 22, '2024-01-18 11:09:00', 1),
('SESSION009', 'Q009', 'PRACTICE', 'B', 1, 20, '2024-01-18 11:12:00', 1),
('SESSION009', 'Q001', 'PRACTICE', 'B', 1, 25, '2024-01-18 11:15:00', 1),

-- SESSION010 (user006) - 복습: 1/6 정답, 매우 느린 풀이 (초보자)
('SESSION010', 'Q001', 'REVIEW', 'A', 0, 120, '2024-01-18 14:08:00', 3),    -- 오답, 많은 재시도
('SESSION010', 'Q003', 'REVIEW', 'A', 0, 110, '2024-01-18 14:15:00', 2),    -- 오답, 재시도
('SESSION010', 'Q006', 'REVIEW', 'A', 1, 100, '2024-01-18 14:22:00', 2),    -- 정답, 재시도
('SESSION010', 'Q008', 'REVIEW', 'A', 0, 95, '2024-01-18 14:28:00', 1),     -- 오답 (정답: B)
('SESSION010', 'Q009', 'REVIEW', 'A', 0, 105, '2024-01-18 14:33:00', 1),    -- 오답 (정답: B)
('SESSION010', 'Q010', 'REVIEW', 'A', 0, 115, '2024-01-18 14:38:00', 1);    -- 오답 (정답: B)

-- =====================================================
-- 6. 학습 세션 이벤트 데이터 삽입 (LearningSessionEvent)
-- =====================================================

INSERT INTO learning_session_events (event_id, session_id, user_id, event_type, session_type, event_metadata, created_at) VALUES
-- SESSION001 이벤트들
('EVT001', 'SESSION001', 'user001', 'SESSION_STARTED', 'PRACTICE', '{"category": "BUSINESS", "difficulty": 1}', '2024-01-15 09:00:00'),
('EVT002', 'SESSION001', 'user001', 'QUESTION_ANSWERED', 'PRACTICE', '{"questionId": "Q001", "isCorrect": true, "timeSpent": 45}', '2024-01-15 09:07:00'),
('EVT003', 'SESSION001', 'user001', 'QUESTION_ANSWERED', 'PRACTICE', '{"questionId": "Q002", "isCorrect": false, "timeSpent": 60}', '2024-01-15 09:09:00'),
('EVT004', 'SESSION001', 'user001', 'SESSION_COMPLETED', 'PRACTICE', '{"totalQuestions": 5, "correctAnswers": 4, "accuracy": 80.0}', '2024-01-15 09:25:00'),

-- SESSION002 이벤트들
('EVT005', 'SESSION002', 'user001', 'SESSION_STARTED', 'REVIEW', '{"reviewType": "WRONG_ANSWERS"}', '2024-01-16 14:00:00'),
('EVT006', 'SESSION002', 'user001', 'SESSION_COMPLETED', 'REVIEW', '{"totalQuestions": 3, "correctAnswers": 2, "accuracy": 66.7}', '2024-01-16 14:18:00'),

-- SESSION003 이벤트들 (진행 중)
('EVT007', 'SESSION003', 'user001', 'SESSION_STARTED', 'PRACTICE', '{"category": "STUDY", "difficulty": 2}', '2024-01-17 10:00:00'),
('EVT008', 'SESSION003', 'user001', 'QUESTION_ANSWERED', 'PRACTICE', '{"questionId": "Q006", "isCorrect": true, "timeSpent": 25}', '2024-01-17 10:08:00'),

-- SESSION004 이벤트들
('EVT009', 'SESSION004', 'user002', 'SESSION_STARTED', 'PRACTICE', '{"category": "BUSINESS", "difficulty": 2}', '2024-01-15 11:00:00'),
('EVT010', 'SESSION004', 'user002', 'SESSION_COMPLETED', 'PRACTICE', '{"totalQuestions": 5, "correctAnswers": 3, "accuracy": 60.0}', '2024-01-15 11:30:00'),

-- SESSION005 이벤트들
('EVT011', 'SESSION005', 'user002', 'SESSION_STARTED', 'WRONG_ANSWER', '{"focusArea": "GRAMMAR"}', '2024-01-16 16:00:00'),
('EVT012', 'SESSION005', 'user002', 'SESSION_COMPLETED', 'WRONG_ANSWER', '{"totalQuestions": 3, "correctAnswers": 3, "accuracy": 100.0}', '2024-01-16 16:15:00'),

-- SESSION006 이벤트들
('EVT013', 'SESSION006', 'user003', 'SESSION_STARTED', 'PRACTICE', '{"category": "STUDY", "difficulty": 1}', '2024-01-14 13:00:00'),
('EVT014', 'SESSION006', 'user003', 'SESSION_COMPLETED', 'PRACTICE', '{"totalQuestions": 6, "correctAnswers": 2, "accuracy": 33.3}', '2024-01-14 13:40:00'),

-- SESSION007 이벤트들 (진행 중)
('EVT015', 'SESSION007', 'user003', 'SESSION_STARTED', 'REVIEW', '{"reviewType": "VOCABULARY"}', '2024-01-17 15:00:00'),

-- 추가 세션 이벤트들
('EVT016', 'SESSION008', 'user004', 'SESSION_STARTED', 'PRACTICE', '{"category": "BUSINESS", "difficulty": 3}', '2024-01-18 09:00:00'),
('EVT017', 'SESSION008', 'user004', 'SESSION_COMPLETED', 'PRACTICE', '{"totalQuestions": 4, "correctAnswers": 3, "accuracy": 75.0}', '2024-01-18 09:20:00'),

('EVT018', 'SESSION009', 'user005', 'SESSION_STARTED', 'PRACTICE', '{"category": "STUDY", "difficulty": 1}', '2024-01-18 11:00:00'),
('EVT019', 'SESSION009', 'user005', 'SESSION_COMPLETED', 'PRACTICE', '{"totalQuestions": 5, "correctAnswers": 5, "accuracy": 100.0}', '2024-01-18 11:25:00'),

('EVT020', 'SESSION010', 'user006', 'SESSION_STARTED', 'REVIEW', '{"reviewType": "MIXED"}', '2024-01-18 14:00:00'), 
('EVT021', 'SESSION010', 'user006', 'SESSION_COMPLETED', 'REVIEW', '{"totalQuestions": 6, "correctAnswers": 1, "accuracy": 16.7}', '2024-01-18 14:35:00');

-- =====================================================
-- 7. 사용자 프로필 데이터 삽입 (UserProfile)
-- =====================================================

INSERT INTO user_profiles (user_id, learning_purpose, learning_style, difficulty_preference, interests_version, created_at, updated_at, interests_last_updated) VALUES
('user001', 'BUSINESS', 'VISUAL', 'INTERMEDIATE', 1, '2024-01-01 00:00:00', '2024-01-15 00:00:00', '2024-01-15 00:00:00'),
('user002', 'BUSINESS', 'MIXED', 'ADVANCED', 1, '2024-01-01 00:00:00', '2024-01-16 00:00:00', '2024-01-16 00:00:00'),
('user003', 'STUDY', 'READING', 'BEGINNER', 1, '2024-01-01 00:00:00', '2024-01-14 00:00:00', '2024-01-14 00:00:00'),
('user004', 'BUSINESS', 'AUDITORY', 'EXPERT', 1, '2024-01-01 00:00:00', '2024-01-18 00:00:00', '2024-01-18 00:00:00'),
('user005', 'STUDY', 'KINESTHETIC', 'BEGINNER', 1, '2024-01-01 00:00:00', '2024-01-18 00:00:00', '2024-01-18 00:00:00'),
('user006', 'STUDY', 'MIXED', 'BEGINNER', 1, '2024-01-01 00:00:00', '2024-01-18 00:00:00', '2024-01-18 00:00:00');

-- =====================================================
-- 8. 사용자 관심사 데이터 삽입 (UserInterests)
-- =====================================================

INSERT INTO user_interests (user_id, interest) VALUES
('user001', 'BUSINESS_MEETINGS'),
('user001', 'CUSTOMER_SERVICE'),
('user001', 'EMAIL_WRITING'),
('user002', 'BUSINESS_NEGOTIATION'),
('user002', 'PRESENTATION_SKILLS'),
('user002', 'CONTRACT_MANAGEMENT'),
('user003', 'BASIC_GRAMMAR'),
('user003', 'VOCABULARY_BUILDING'),
('user003', 'PRONUNCIATION'),
('user004', 'ADVANCED_BUSINESS'),
('user004', 'INTERNATIONAL_TRADE'),
('user004', 'LEADERSHIP_SKILLS'),
('user005', 'ACADEMIC_ENGLISH'),
('user005', 'RESEARCH_METHODS'),
('user005', 'ACADEMIC_WRITING'),
('user006', 'BASIC_CONVERSATION'),
('user006', 'DAILY_EXPRESSIONS'),
('user006', 'SURVIVAL_ENGLISH');

-- =====================================================
-- 9. 사용자 선택 소분류 데이터 삽입 (UserSelectedMinorCategories)
-- =====================================================

INSERT INTO user_selected_minor_categories (user_id, minor_category) VALUES
('user001', 'MEETING_CONFERENCE'),
('user001', 'CUSTOMER_SERVICE'),
('user001', 'EMAIL_REPORT'),
('user002', 'MEETING_CONFERENCE'),
('user002', 'DEPARTMENT_CONVERSATION'),
('user002', 'EMAIL_REPORT'),
('user003', 'CLASS_LISTENING'),
('user003', 'ASSIGNMENT_EXAM'),
('user004', 'MEETING_CONFERENCE'),
('user004', 'DEPARTMENT_CONVERSATION'),
('user005', 'CLASS_LISTENING'),
('user005', 'ASSIGNMENT_EXAM'),
('user006', 'CLASS_LISTENING'),
('user006', 'BASIC_CONVERSATION');

-- =====================================================
-- 10. 학습 패턴 분석 데이터 삽입 (LearningPatternAnalysis)
-- =====================================================

INSERT INTO learning_pattern_analysis (analysis_id, user_id, session_id, analysis_type, start_date, end_date, analyzed_at, basic_statistics, learning_pattern, learning_pattern_info, learning_progress, performance_analysis, question_type_performances) VALUES
('ANALYSIS001', 'user001', 'SESSION001', 'SESSION_ANALYSIS', '2024-01-15 09:00:00', '2024-01-15 09:25:00', '2024-01-15 09:30:00',
 '{"totalQuestions": 5, "correctAnswers": 4, "accuracy": 80.0, "averageTime": 45.0, "totalTime": 225}',
 '{"patternType": "CONSISTENT", "strength": "BUSINESS_VOCABULARY", "weakness": "GRAMMAR_RULES"}',
 '{"learningStyle": "VISUAL", "preferredCategory": "BUSINESS", "difficultyLevel": 1}',
 '{"progressRate": 100.0, "completionTime": 25, "efficiency": "HIGH"}',
 '{"overallScore": 80.0, "categoryBreakdown": {"BUSINESS": 80.0}, "improvementAreas": ["GRAMMAR"]}',
 '{"FILL_IN_THE_BLANK": 75.0, "IDIOM_IN_CONTEXT": 100.0}'),

('ANALYSIS002', 'user001', NULL, 'PERIOD_ANALYSIS', '2024-01-15 00:00:00', '2024-01-17 23:59:59', '2024-01-18 00:00:00',
 '{"totalSessions": 3, "completedSessions": 2, "totalQuestions": 10, "correctAnswers": 7, "overallAccuracy": 70.0}',
 '{"patternType": "IMPROVING", "trend": "POSITIVE", "consistency": "MEDIUM"}',
 '{"learningStyle": "VISUAL", "preferredCategories": ["BUSINESS", "STUDY"], "difficultyProgression": "GRADUAL"}',
 '{"overallProgress": 66.7, "sessionCompletionRate": 66.7, "learningEfficiency": "MEDIUM"}',
 '{"overallScore": 70.0, "categoryBreakdown": {"BUSINESS": 75.0, "STUDY": 50.0}, "improvementAreas": ["GRAMMAR", "VOCABULARY"]}',
 '{"FILL_IN_THE_BLANK": 60.0, "IDIOM_IN_CONTEXT": 100.0, "BASIC_GRAMMAR": 50.0}'),

('ANALYSIS003', 'user002', 'SESSION004', 'SESSION_ANALYSIS', '2024-01-15 11:00:00', '2024-01-15 11:30:00', '2024-01-15 11:35:00',
 '{"totalQuestions": 5, "correctAnswers": 3, "accuracy": 60.0, "averageTime": 46.0, "totalTime": 230}',
 '{"patternType": "VARIABLE", "strength": "BUSINESS_CONTEXT", "weakness": "DETAIL_ATTENTION"}',
 '{"learningStyle": "MIXED", "preferredCategory": "BUSINESS", "difficultyLevel": 2}',
 '{"progressRate": 100.0, "completionTime": 30, "efficiency": "MEDIUM"}',
 '{"overallScore": 60.0, "categoryBreakdown": {"BUSINESS": 60.0}, "improvementAreas": ["ACCURACY", "TIME_MANAGEMENT"]}',
 '{"FILL_IN_THE_BLANK": 60.0, "IDIOM_IN_CONTEXT": 60.0}'),

('ANALYSIS004', 'user003', NULL, 'PERIOD_ANALYSIS', '2024-01-14 00:00:00', '2024-01-17 23:59:59', '2024-01-18 00:00:00',
 '{"totalSessions": 2, "completedSessions": 1, "totalQuestions": 7, "correctAnswers": 2, "overallAccuracy": 28.6}',
 '{"patternType": "BEGINNER", "trend": "STABLE", "consistency": "LOW"}',
 '{"learningStyle": "READING", "preferredCategories": ["STUDY"], "difficultyLevel": "BEGINNER"}',
 '{"overallProgress": 50.0, "sessionCompletionRate": 50.0, "learningEfficiency": "LOW"}',
 '{"overallScore": 28.6, "categoryBreakdown": {"STUDY": 28.6}, "improvementAreas": ["ALL_AREAS"]}',
 '{"BASIC_GRAMMAR": 0.0, "VOCABULARY_EXPANSION": 50.0, "PRONUNCIATION_RECOGNITION": 0.0}');

-- =====================================================
-- 더미 데이터 삽입 완료
-- 
-- 생성된 통계 지표 요약:
-- 1. 정답률 (Accuracy Rate): 사용자별로 16.7% ~ 100% 범위
-- 2. 오답률 (Error Rate): 정답률의 역수
-- 3. 평균 풀이 시간: 15초 ~ 120초 범위 (실력별 차이)
-- 4. 재시도율 (Retry Rate): 0% ~ 50% 범위 (학습 패턴 반영)
-- 5. 학습 진도율: 완료된 세션 비율
-- 6. 난이도별 성취도: 1단계 ~ 3단계별 성과
-- 7. 카테고리별 숙련도: BUSINESS vs STUDY 영역별 성과
-- 8. 학습 이벤트: 세션 시작/완료, 문제 답변 등 상세 이벤트
-- 9. 사용자 프로필: 학습 목적, 스타일, 난이도 선호도
-- 10. 학습 패턴 분석: 세션별, 기간별 상세 분석 결과
-- 
-- 이 데이터로 다양한 학습 패턴을 분석할 수 있습니다:
-- - user001: 중급 학습자 (70% 정답률, 보통 속도, 비즈니스 중심)
-- - user002: 중상급 학습자 (향상 중, 60% → 100%, 비즈니스 전문)
-- - user003: 초급 학습자 (28.6% 정답률, 느린 속도, 학습 중심)
-- - user004: 상급 학습자 (75% 정답률, 빠른 속도, 고급 비즈니스)
-- - user005: 우수 학습자 (100% 정답률, 매우 빠름, 학습 우수)
-- - user006: 완전 초보 (16.7% 정답률, 매우 느림, 많은 재시도)
-- =====================================================
