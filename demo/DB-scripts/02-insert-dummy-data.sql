-- 학습 패턴 분석과 복습 세션 테스트를 위한 더미 데이터

-- 1. 사용자 프로필 데이터 추가
INSERT INTO user_profiles (user_id, major_category, business_domain, difficulty_level, learning_style, selected_minor_categories, selected_question_types, has_interests_set, created_at, updated_at) VALUES
('user001', 'STUDY', 'STUDENT', 'MEDIUM', 'VISUAL', 'GRAMMAR,VOCABULARY', 'FILL_IN_THE_BLANK,IDIOM_IN_CONTEXT', true, NOW(), NOW()),
('user002', 'BUSINESS', 'ENGINEER', 'HARD', 'AUDITORY', 'MEETING,PRESENTATION', 'SENTENCE_COMPLETION,IDIOM_IN_CONTEXT', true, NOW(), NOW()),
('user003', 'TRAVEL', 'TOURIST', 'EASY', 'KINESTHETIC', 'AIRPORT,HOTEL', 'FILL_IN_THE_BLANK,SENTENCE_COMPLETION', true, NOW(), NOW());

-- 2. 학습 세션 데이터 추가 (완료된 세션들)
INSERT INTO learning_sessions (session_id, user_id, learning_item_id, session_type, status, total_questions, answered_questions, correct_answers, wrong_answers, score, started_at, completed_at, last_updated_at) VALUES
-- user001: 일관된 학습자 (CONSISTENT_LEARNER)
('session001', 'user001', 'GRAMMAR_001', 'GRAMMAR', 'COMPLETED', 10, 10, 8, 2, 80, '2024-01-01 09:00:00', '2024-01-01 10:30:00', '2024-01-01 10:30:00'),
('session002', 'user001', 'VOCABULARY_001', 'VOCABULARY', 'COMPLETED', 10, 10, 7, 3, 70, '2024-01-02 09:00:00', '2024-01-02 10:15:00', '2024-01-02 10:15:00'),
('session003', 'user001', 'GRAMMAR_002', 'GRAMMAR', 'COMPLETED', 10, 10, 9, 1, 90, '2024-01-03 09:00:00', '2024-01-03 10:00:00', '2024-01-03 10:00:00'),

-- user002: 변동이 큰 학습자 (VARIABLE_LEARNER)
('session004', 'user002', 'MEETING_001', 'MEETING', 'COMPLETED', 10, 10, 5, 5, 50, '2024-01-01 14:00:00', '2024-01-01 15:30:00', '2024-01-01 15:30:00'),
('session005', 'user002', 'PRESENTATION_001', 'PRESENTATION', 'COMPLETED', 10, 10, 9, 1, 90, '2024-01-02 14:00:00', '2024-01-02 15:00:00', '2024-01-02 15:00:00'),
('session006', 'user002', 'MEETING_002', 'MEETING', 'COMPLETED', 10, 10, 3, 7, 30, '2024-01-03 14:00:00', '2024-01-03 16:00:00', '2024-01-03 16:00:00'),

-- user003: 어려움을 겪는 학습자 (STRUGGLING_LEARNER)
('session007', 'user003', 'AIRPORT_001', 'AIRPORT', 'COMPLETED', 10, 10, 2, 8, 20, '2024-01-01 19:00:00', '2024-01-01 21:00:00', '2024-01-01 21:00:00'),
('session008', 'user003', 'HOTEL_001', 'HOTEL', 'COMPLETED', 10, 10, 3, 7, 30, '2024-01-02 19:00:00', '2024-01-02 20:45:00', '2024-01-02 20:45:00'),
('session009', 'user003', 'AIRPORT_002', 'AIRPORT', 'COMPLETED', 10, 10, 1, 9, 10, '2024-01-03 19:00:00', '2024-01-03 21:30:00', '2024-01-03 21:30:00');

-- 3. 문제 답변 데이터 추가 (오답 문제들)
INSERT INTO question_answers (session_id, question_id, question_text, option_a, option_b, option_c, user_answer, correct_answer, is_correct, points_per_question, earned_points, time_spent, answered_at, explanation, user_notes, difficulty, major_category, minor_category, question_type, tags) VALUES
-- user001의 오답 문제들 (GRAMMAR, VOCABULARY)
('session001', 'q001', 'She _____ to the store yesterday.', 'go', 'goes', 'went', 'goes', 'went', false, 10, 0, 45, '2024-01-01 09:15:00', '과거 시제는 went를 사용합니다.', '과거 시제 헷갈림', 2, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'past_tense,verb'),
('session001', 'q002', 'I have _____ this book before.', 'read', 'readed', 'reading', 'reading', 'read', false, 10, 0, 60, '2024-01-01 09:20:00', '현재완료 시제는 과거분사를 사용합니다.', '현재완료 시제 어려움', 3, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'present_perfect,past_participle'),
('session002', 'q003', 'The weather is _____ today.', 'nice', 'nicely', 'niceness', 'nicely', 'nice', false, 10, 0, 30, '2024-01-02 09:10:00', '형용사 nice를 사용합니다.', '형용사와 부사 구분 어려움', 2, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'adjective,adverb'),

-- user002의 오답 문제들 (MEETING, PRESENTATION)
('session004', 'q004', 'Let me _____ the meeting.', 'start', 'starting', 'started', 'starting', 'start', false, 10, 0, 90, '2024-01-01 14:10:00', 'let me + 동사원형을 사용합니다.', 'let me 뒤에 동사형 헷갈림', 2, 'BUSINESS', 'MEETING', 'SENTENCE_COMPLETION', 'let_me,verb_form'),
('session004', 'q005', 'I would like to _____ my opinion.', 'share', 'sharing', 'shared', 'sharing', 'share', false, 10, 0, 75, '2024-01-01 14:15:00', 'would like to + 동사원형을 사용합니다.', 'would like to 뒤에 동사형 헷갈림', 2, 'BUSINESS', 'MEETING', 'SENTENCE_COMPLETION', 'would_like_to,verb_form'),
('session006', 'q006', 'The project _____ behind schedule.', 'is', 'are', 'be', 'are', 'is', false, 10, 0, 120, '2024-01-03 14:10:00', '단수 주어 project에는 is를 사용합니다.', '단수/복수 주어 구분 어려움', 3, 'BUSINESS', 'MEETING', 'SENTENCE_COMPLETION', 'subject_verb_agreement,singular'),

-- user003의 오답 문제들 (AIRPORT, HOTEL)
('session007', 'q007', 'Where is the _____ desk?', 'check-in', 'checking-in', 'checked-in', 'checking-in', 'check-in', false, 10, 0, 180, '2024-01-01 19:10:00', 'check-in은 복합명사입니다.', '복합명사 어려움', 1, 'TRAVEL', 'AIRPORT', 'FILL_IN_THE_BLANK', 'compound_noun,airport'),
('session007', 'q008', 'I need to _____ my luggage.', 'check', 'checking', 'checked', 'checking', 'check', false, 10, 0, 150, '2024-01-01 19:15:00', 'need to + 동사원형을 사용합니다.', 'need to 뒤에 동사형 헷갈림', 2, 'TRAVEL', 'AIRPORT', 'FILL_IN_THE_BLANK', 'need_to,verb_form'),
('session008', 'q009', 'What time is _____?', 'check-out', 'checking-out', 'checked-out', 'checking-out', 'check-out', false, 10, 0, 120, '2024-01-02 19:10:00', 'check-out은 복합명사입니다.', '복합명사 어려움', 1, 'TRAVEL', 'HOTEL', 'FILL_IN_THE_BLANK', 'compound_noun,hotel'),
('session009', 'q010', 'Can you _____ me a taxi?', 'call', 'calling', 'called', 'calling', 'call', false, 10, 0, 200, '2024-01-03 19:10:00', 'can you + 동사원형을 사용합니다.', 'can you 뒤에 동사형 헷갈림', 2, 'TRAVEL', 'AIRPORT', 'FILL_IN_THE_BLANK', 'can_you,verb_form');

-- 4. 문제 카테고리 데이터 추가
INSERT INTO question_categories (major_category, minor_category, question_type, difficulty_level, question_count, created_at) VALUES
('STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 2, 50, NOW()),
('STUDY', 'GRAMMAR', 'IDIOM_IN_CONTEXT', 3, 30, NOW()),
('STUDY', 'VOCABULARY', 'FILL_IN_THE_BLANK', 2, 40, NOW()),
('STUDY', 'VOCABULARY', 'SENTENCE_COMPLETION', 2, 35, NOW()),
('BUSINESS', 'MEETING', 'SENTENCE_COMPLETION', 2, 45, NOW()),
('BUSINESS', 'PRESENTATION', 'IDIOM_IN_CONTEXT', 3, 25, NOW()),
('TRAVEL', 'AIRPORT', 'FILL_IN_THE_BLANK', 1, 60, NOW()),
('TRAVEL', 'HOTEL', 'FILL_IN_THE_BLANK', 1, 55, NOW());

-- 5. 현재 진행 중인 학습 세션 추가 (테스트용)
INSERT INTO learning_sessions (session_id, user_id, learning_item_id, session_type, status, total_questions, answered_questions, correct_answers, wrong_answers, score, started_at, last_updated_at) VALUES
('session010', 'user001', 'GRAMMAR_003', 'GRAMMAR', 'IN_PROGRESS', 10, 3, 2, 1, 20, '2024-01-04 09:00:00', '2024-01-04 09:30:00'),
('session011', 'user002', 'MEETING_003', 'MEETING', 'IN_PROGRESS', 10, 5, 3, 2, 30, '2024-01-04 14:00:00', '2024-01-04 14:45:00'),
('session012', 'user003', 'HOTEL_002', 'HOTEL', 'IN_PROGRESS', 10, 2, 0, 2, 0, '2024-01-04 19:00:00', '2024-01-04 19:20:00');

-- 6. 진행 중인 세션의 문제 답변 데이터 추가
INSERT INTO question_answers (session_id, question_id, question_text, option_a, option_b, option_c, user_answer, correct_answer, is_correct, points_per_question, earned_points, time_spent, answered_at, explanation, user_notes, difficulty, major_category, minor_category, question_type, tags) VALUES
-- user001 진행 중인 세션
('session010', 'q011', 'He _____ English very well.', 'speak', 'speaks', 'speaking', 'speaks', 'speaks', true, 10, 10, 35, '2024-01-04 09:05:00', '3인칭 단수 주어에는 s를 붙입니다.', '잘 풀었음', 2, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'third_person_singular,verb'),
('session010', 'q012', 'They _____ to school every day.', 'go', 'goes', 'going', 'go', 'go', true, 10, 10, 40, '2024-01-04 09:10:00', '복수 주어에는 동사원형을 사용합니다.', '복수 주어 파악 잘함', 2, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'plural_subject,verb'),
('session010', 'q013', 'She _____ a beautiful dress.', 'wear', 'wears', 'wearing', 'wearing', 'wears', false, 10, 0, 50, '2024-01-04 09:15:00', '3인칭 단수 주어에는 s를 붙입니다.', '동사형 헷갈림', 2, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'third_person_singular,verb');

-- 7. 사용자별 학습 통계 요약
-- user001: 일관된 학습자 (평균 정답률 80%, 일관성 높음)
-- user002: 변동이 큰 학습자 (평균 정답률 57%, 일관성 낮음)  
-- user003: 어려움을 겪는 학습자 (평균 정답률 20%, 일관성 낮음)

-- 8. 복습 세션 테스트를 위한 완료된 세션 추가
INSERT INTO learning_sessions (session_id, user_id, learning_item_id, session_type, status, total_questions, answered_questions, correct_answers, wrong_answers, score, started_at, completed_at, last_updated_at) VALUES
('session013', 'user001', 'REVIEW_GRAMMAR_001', 'REVIEW', 'COMPLETED', 10, 10, 9, 1, 90, '2024-01-05 09:00:00', '2024-01-05 10:00:00', '2024-01-05 10:00:00'),
('session014', 'user002', 'REVIEW_MEETING_001', 'REVIEW', 'COMPLETED', 10, 10, 6, 4, 60, '2024-01-05 14:00:00', '2024-01-05 15:15:00', '2024-01-05 15:15:00'),
('session015', 'user003', 'REVIEW_AIRPORT_001', 'REVIEW', 'COMPLETED', 10, 10, 4, 6, 40, '2024-01-05 19:00:00', '2024-01-05 20:30:00', '2024-01-05 20:30:00');

-- 복습 세션의 문제 답변 데이터 (정답 처리된 문제들)
INSERT INTO question_answers (session_id, question_id, question_text, option_a, option_b, option_c, user_answer, correct_answer, is_correct, points_per_question, earned_points, time_spent, answered_at, explanation, user_notes, difficulty, major_category, minor_category, question_type, tags) VALUES
-- user001 복습 세션 (이전에 틀렸던 문제들을 맞춤)
('session013', 'q001', 'She _____ to the store yesterday.', 'go', 'goes', 'went', 'went', 'went', true, 10, 10, 30, '2024-01-05 09:05:00', '과거 시제는 went를 사용합니다.', '이제 이해함!', 2, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'past_tense,verb'),
('session013', 'q002', 'I have _____ this book before.', 'read', 'readed', 'reading', 'read', 'read', true, 10, 10, 45, '2024-01-05 09:10:00', '현재완료 시제는 과거분사를 사용합니다.', '현재완료 시제 마스터!', 3, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'present_perfect,past_participle');

-- 9. 테스트용 사용자 추가 (빠른 학습자)
INSERT INTO user_profiles (user_id, major_category, business_domain, difficulty_level, learning_style, selected_minor_categories, selected_question_types, has_interests_set, created_at, updated_at) VALUES
('user004', 'STUDY', 'STUDENT', 'EASY', 'VISUAL', 'GRAMMAR,VOCABULARY', 'FILL_IN_THE_BLANK,SENTENCE_COMPLETION', true, NOW(), NOW());

-- user004: 빠른 학습자 (FAST_LEARNER) - 완료된 세션들
INSERT INTO learning_sessions (session_id, user_id, learning_item_id, session_type, status, total_questions, answered_questions, correct_answers, wrong_answers, score, started_at, completed_at, last_updated_at) VALUES
('session016', 'user004', 'GRAMMAR_001', 'GRAMMAR', 'COMPLETED', 10, 10, 9, 1, 90, '2024-01-01 08:00:00', '2024-01-01 08:45:00', '2024-01-01 08:45:00'),
('session017', 'user004', 'VOCABULARY_001', 'VOCABULARY', 'COMPLETED', 10, 10, 8, 2, 80, '2024-01-02 08:00:00', '2024-01-02 08:50:00', '2024-01-02 08:50:00'),
('session018', 'user004', 'GRAMMAR_002', 'GRAMMAR', 'COMPLETED', 10, 10, 10, 0, 100, '2024-01-03 08:00:00', '2024-01-03 08:40:00', '2024-01-03 08:40:00');

-- user004의 오답 문제들
INSERT INTO question_answers (session_id, question_id, question_text, option_a, option_b, option_c, user_answer, correct_answer, is_correct, points_per_question, earned_points, time_spent, answered_at, explanation, user_notes, difficulty, major_category, minor_category, question_type, tags) VALUES
('session016', 'q019', 'The weather _____ nice today.', 'is', 'are', 'be', 'are', 'is', false, 10, 0, 25, '2024-01-01 08:20:00', '단수 주어 weather에는 is를 사용합니다.', '단수/복수 헷갈림', 2, 'STUDY', 'GRAMMAR', 'FILL_IN_THE_BLANK', 'subject_verb_agreement,singular'),
('session017', 'q020', 'I _____ to school by bus.', 'go', 'goes', 'going', 'going', 'go', false, 10, 0, 30, '2024-01-02 08:15:00', '일반현재시제는 동사원형을 사용합니다.', '동사형 헷갈림', 2, 'STUDY', 'VOCABULARY', 'FILL_IN_THE_BLANK', 'simple_present,verb_form');


