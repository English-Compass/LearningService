-- =====================================================
-- 학습 서비스 더미 데이터 삽입 스크립트
-- 생성일: 2024년
-- 목적: 학습 통계 지표 테스트를 위한 더미 데이터 생성
-- =====================================================

-- 기존 데이터 삭제 (의존성 순서 고려)
DELETE FROM question_answer;
DELETE FROM session_question;
DELETE FROM learning_sessions;
DELETE FROM question;

-- =====================================================
-- 1. 문제 데이터 삽입 (Question)
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

-- 학습 영어 - 수업 듣기 문제들
('Q006', 'I _____ to school every day.', 
 'go', 'goes', 'going', 
 'A', 'STUDY', 'CLASS_LISTENING', 'FILL_IN_THE_BLANK', 
 '주어가 "I"일 때는 동사 원형을 사용합니다.', 
 1, NOW(), NOW()),

('Q007', 'She _____ her homework yesterday.', 
 'do', 'did', 'does', 
 'B', 'STUDY', 'CLASS_LISTENING', 'FILL_IN_THE_BLANK', 
 '"yesterday"는 과거를 나타내므로 과거형 "did"를 사용합니다.', 
 2, NOW(), NOW()),

-- 학습 영어 - 학과 대화 문제들
('Q008', 'The word "enormous" means:', 
 'very small', 'very large', 'medium size', 
 'B', 'STUDY', 'DEPARTMENT_CONVERSATION', 'IDIOM_IN_CONTEXT', 
 '"enormous"는 "매우 큰"이라는 의미입니다.', 
 2, NOW(), NOW()),

('Q009', 'Choose the synonym of "happy":', 
 'sad', 'joyful', 'angry', 
 'B', 'STUDY', 'DEPARTMENT_CONVERSATION', 'IDIOM_IN_CONTEXT', 
 '"happy"의 동의어는 "joyful"입니다.', 
 1, NOW(), NOW()),

-- 학습 영어 - 과제/시험 준비 문제들
('Q010', 'Which word has the /θ/ sound?', 
 'this', 'think', 'that', 
 'B', 'STUDY', 'ASSIGNMENT_EXAM', 'PHONETIC_SYMBOL_FINDING', 
 '"think"에는 무성음 /θ/ 소리가 포함되어 있습니다.', 
 3, NOW(), NOW());

-- =====================================================
-- 2. 학습 세션 데이터 삽입 (LearningSession)
-- =====================================================

INSERT INTO learning_sessions (session_id, user_id, created_at, updated_at, completed_at, status, session_type, session_metadata, progress_percentage, started_at) VALUES
-- 사용자 user001의 세션들
('SESSION001', 'user001', '2024-01-15 09:00:00', NOW(), '2024-01-15 09:25:00', 
 'COMPLETED', 'PRACTICE', '{"targetCategory": "BUSINESS", "difficulty": 1}', 100.0, '2024-01-15 09:05:00'),

('SESSION002', 'user001', '2024-01-16 14:00:00', NOW(), '2024-01-16 14:18:00',
 'COMPLETED', 'REVIEW', '{"reviewType": "WRONG_ANSWERS"}', 100.0, '2024-01-16 14:02:00'),

('SESSION003', 'user001', '2024-01-17 10:00:00', NOW(), NULL,
 'IN_PROGRESS', 'PRACTICE', '{"targetCategory": "STUDY", "difficulty": 2}', 50.0, '2024-01-17 10:03:00'),

-- 사용자 user002의 세션들
('SESSION004', 'user002', '2024-01-15 11:00:00', NOW(), '2024-01-15 11:30:00',
 'COMPLETED', 'PRACTICE', '{"targetCategory": "BUSINESS", "difficulty": 2}', 100.0, '2024-01-15 11:02:00'),

('SESSION005', 'user002', '2024-01-16 16:00:00', NOW(), '2024-01-16 16:15:00',
 'COMPLETED', 'WRONG_ANSWER', '{"focusArea": "GRAMMAR"}', 100.0, '2024-01-16 16:01:00'),

-- 사용자 user003의 세션들  
('SESSION006', 'user003', '2024-01-14 13:00:00', NOW(), '2024-01-14 13:40:00',
 'COMPLETED', 'PRACTICE', '{"targetCategory": "STUDY", "difficulty": 1}', 100.0, '2024-01-14 13:01:00'),

('SESSION007', 'user003', '2024-01-17 15:00:00', NOW(), NULL,
 'IN_PROGRESS', 'REVIEW', '{"reviewType": "VOCABULARY"}', 25.0, '2024-01-17 15:02:00');

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
INSERT INTO learning_sessions (session_id, user_id, created_at, updated_at, completed_at, status, session_type, session_metadata, progress_percentage, started_at) VALUES
('SESSION008', 'user004', '2024-01-18 09:00:00', NOW(), '2024-01-18 09:20:00',
 'COMPLETED', 'PRACTICE', '{"targetCategory": "BUSINESS", "difficulty": 3}', 100.0, '2024-01-18 09:02:00'),

('SESSION009', 'user005', '2024-01-18 11:00:00', NOW(), '2024-01-18 11:25:00',
 'COMPLETED', 'PRACTICE', '{"targetCategory": "STUDY", "difficulty": 1}', 100.0, '2024-01-18 11:01:00'),

('SESSION010', 'user006', '2024-01-18 14:00:00', NOW(), '2024-01-18 14:35:00',
 'COMPLETED', 'REVIEW', '{"reviewType": "MIXED"}', 100.0, '2024-01-18 14:03:00');

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
-- 더미 데이터 삽입 완료
-- 
-- 생성된 통계 지표 요약:
-- 1. 정답률 (Accuracy Rate): 사용자별로 33% ~ 100% 범위
-- 2. 오답률 (Error Rate): 정답률의 역수
-- 3. 평균 풀이 시간: 15초 ~ 120초 범위 (실력별 차이)
-- 4. 재시도율 (Retry Rate): 0% ~ 50% 범위 (학습 패턴 반영)
-- 5. 학습 진도율: 완료된 세션 비율
-- 6. 난이도별 성취도: 1단계 ~ 3단계별 성과
-- 7. 카테고리별 숙련도: BUSINESS vs STUDY 영역별 성과
-- 
-- 이 데이터로 다양한 학습 패턴을 분석할 수 있습니다:
-- - user001: 중급 학습자 (80% 정답률, 보통 속도)
-- - user002: 중상급 학습자 (향상 중, 60% → 100%)
-- - user003: 초급 학습자 (33% 정답률, 느린 속도)
-- - user004: 상급 학습자 (75% 정답률, 빠른 속도)
-- - user005: 우수 학습자 (100% 정답률, 매우 빠름)
-- - user006: 완전 초보 (17% 정답률, 매우 느림, 많은 재시도)
-- =====================================================
