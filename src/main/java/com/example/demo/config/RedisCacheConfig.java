package com.example.demo.config;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 캐시 키 구조 및 설정 관리
 * 
 * 이 클래스는 Learning Service에서 사용하는 모든 Redis 캐시의 키 구조와 TTL(Time To Live)을 관리합니다.
 * 
 * 주요 기능:
 * 1. 캐시 키 접두사 및 구분자 정의
 * 2. 각 데이터 타입별 TTL 설정
 * 3. 캐시 키 생성 메서드 제공
 * 4. 캐시 키 패턴 정의 (Redis SCAN 명령어용)
 * 5. 캐시 데이터 구조 정의
 */
@Component
public class RedisCacheConfig {

    /**
     * 캐시 키 접두사 상수
     * 모든 Redis 키는 이 접두사로 시작하여 네임스페이스를 구분합니다.
     * 예: learning_service:session:12345
     */
    public static final String KEY_PREFIX = "learning_service";
    
    /**
     * 캐시 키 구분자
     * Redis 키의 각 부분을 구분하는 문자입니다.
     * 예: learning_service:session:12345:progress
     */
    public static final String KEY_SEPARATOR = ":";
    
    // ===== 캐시 TTL(Time To Live) 상수들 =====
    // 각 데이터 타입별로 얼마나 오래 Redis에 보관할지 설정합니다.
    
    /**
     * 학습 세션 캐시 TTL: 1시간
     * 사용자가 학습 세션을 시작하면 세션 정보를 1시간 동안 캐시에 보관합니다.
     */
    public static final Duration SESSION_CACHE_TTL = Duration.ofHours(1);
    
    /**
     * 세션 진행 상황 캐시 TTL: 30분
     * 사용자의 문제 풀이 진행 상황을 30분 동안 캐시에 보관합니다.
     * 짧은 TTL을 사용하는 이유: 진행 상황은 자주 변경되므로 오래된 데이터는 의미가 없습니다.
     */
    public static final Duration SESSION_PROGRESS_CACHE_TTL = Duration.ofMinutes(30);
    
    /**
     * 세션 문제 목록 캐시 TTL: 2시간
     * 특정 세션에서 사용자에게 할당된 문제 목록을 2시간 동안 캐시에 보관합니다.
     * 긴 TTL을 사용하는 이유: 문제 목록은 세션 중에 변경되지 않습니다.
     */
    public static final Duration SESSION_QUESTIONS_CACHE_TTL = Duration.ofHours(2);
    
    /**
     * 사용자 세션 목록 캐시 TTL: 2시간
     * 특정 사용자의 모든 세션 목록을 2시간 동안 캐시에 보관합니다.
     */
    public static final Duration USER_SESSIONS_CACHE_TTL = Duration.ofHours(2);
    
    /**
     * 사용자 현재 세션 캐시 TTL: 1시간
     * 사용자가 현재 진행 중인 세션 정보를 1시간 동안 캐시에 보관합니다.
     */
    public static final Duration CURRENT_SESSION_CACHE_TTL = Duration.ofHours(1);
    
    /**
     * 통계 데이터 캐시 TTL: 6시간
     * 학습 통계 데이터를 6시간 동안 캐시에 보관합니다.
     * 긴 TTL을 사용하는 이유: 통계는 자주 계산할 필요가 없습니다.
     */
    public static final Duration STATISTICS_CACHE_TTL = Duration.ofHours(6);
    
    /**
     * 통계 데이터 캐시 TTL: 6시간 (별칭)
     * 학습 통계 데이터를 6시간 동안 캐시에 보관합니다.
     */
    public static final Duration STATS_CACHE_TTL = Duration.ofHours(6);
    
    /**
     * 이벤트 데이터 캐시 TTL: 1일
     * 학습 이벤트 데이터를 1일 동안 캐시에 보관합니다.
     * 가장 긴 TTL을 사용하는 이유: 이벤트는 분석 목적으로 오래 보관해야 합니다.
     */
    public static final Duration EVENT_CACHE_TTL = Duration.ofDays(1);
    
    // ===== 추가 TTL 상수들 =====
    
    /**
     * 진행 상황 캐시 TTL: 30분
     * 일반적인 진행 상황 데이터를 30분 동안 캐시에 보관합니다.
     */
    public static final Duration PROGRESS_CACHE_TTL = Duration.ofMinutes(30);
    
    /**
     * 사용자 통계 캐시 TTL: 6시간
     * 사용자별 학습 통계를 6시간 동안 캐시에 보관합니다.
     */
    public static final Duration USER_STATS_CACHE_TTL = Duration.ofHours(6);
    
    /**
     * 문제 분류 캐시 TTL: 1일
     * 문제 카테고리 정보를 1일 동안 캐시에 보관합니다.
     * 긴 TTL을 사용하는 이유: 카테고리는 자주 변경되지 않습니다.
     */
    public static final Duration CATEGORY_CACHE_TTL = Duration.ofDays(1);
    
    /**
     * 임시 데이터 캐시 TTL: 15분
     * 임시로 사용되는 데이터를 15분 동안 캐시에 보관합니다.
     * 가장 짧은 TTL을 사용하는 이유: 임시 데이터는 빠르게 만료되어야 합니다.
     */
    public static final Duration TEMP_CACHE_TTL = Duration.ofMinutes(15);

    // ===== 캐시 키 빌더 클래스 =====
    // Redis에 저장할 데이터의 키를 생성하는 메서드들을 제공합니다.
    
    /**
     * 캐시 키 빌더
     * Redis에 저장할 데이터의 키를 체계적으로 생성합니다.
     * 
     * 키 구조 예시:
     * - session:12345 (세션 정보)
     * - session:progress:12345 (세션 진행 상황)
     * - user:sessions:user123 (사용자 세션 목록)
     * - temp:learning-pattern:user123 (임시 학습 패턴 데이터)
     */
    public static class CacheKeyBuilder {
        
        /**
         * 학습 세션 캐시 키 생성
         * @param sessionId 세션 ID
         * @return "session:{sessionId}" 형태의 키
         * 
         * 사용 예시: session:12345
         * 저장 데이터: 세션 기본 정보 (시작 시간, 상태, 사용자 ID 등)
         */
        public static String sessionKey(String sessionId) {
            return "session:" + sessionId;
        }
        
        /**
         * 세션 진행 상황 캐시 키 생성
         * @param sessionId 세션 ID
         * @return "session:progress:{sessionId}" 형태의 키
         * 
         * 사용 예시: session:progress:12345
         * 저장 데이터: 현재 문제 번호, 정답률, 진행률 등
         */
        public static String sessionProgressKey(String sessionId) {
            return "session:progress:" + sessionId;
        }
        
        /**
         * 세션 문제 목록 캐시 키 생성
         * @param sessionId 세션 ID
         * @return "session:questions:{sessionId}" 형태의 키
         * 
         * 사용 예시: session:questions:12345
         * 저장 데이터: 해당 세션에서 사용자에게 할당된 문제 ID 목록
         */
        public static String sessionQuestionsKey(String sessionId) {
            return "session:questions:" + sessionId;
        }
        
        /**
         * 사용자 세션 목록 캐시 키 생성
         * @param userId 사용자 ID
         * @return "user:sessions:{userId}" 형태의 키
         * 
         * 사용 예시: user:sessions:user123
         * 저장 데이터: 특정 사용자의 모든 세션 목록
         */
        public static String userSessionsKey(String userId) {
            return "user:sessions:" + userId;
        }
        
        /**
         * 사용자 현재 세션 캐시 키 생성
         * @param userId 사용자 ID
         * @return "user:current:{userId}" 형태의 키
         * 
         * 사용 예시: user:current:user123
         * 저장 데이터: 사용자가 현재 진행 중인 세션 ID
         */
        public static String userCurrentSessionKey(String userId) {
            return "user:current:" + userId;
        }
        
        /**
         * 세션 통계 캐시 키 생성
         * @param sessionId 세션 ID
         * @return "session:stats:{sessionId}" 형태의 키
         * 
         * 사용 예시: session:stats:12345
         * 저장 데이터: 세션별 통계 (정답률, 평균 소요 시간 등)
         */
        public static String sessionStatisticsKey(String sessionId) {
            return "session:stats:" + sessionId;
        }
        
        /**
         * 이벤트 캐시 키 생성
         * @param eventType 이벤트 타입 (예: SESSION_STARTED, QUESTION_ANSWERED)
         * @param eventId 이벤트 ID
         * @return "event:{eventType}:{eventId}" 형태의 키
         * 
         * 사용 예시: event:SESSION_STARTED:evt123
         * 저장 데이터: 학습 이벤트 상세 정보
         */
        public static String eventKey(String eventType, String eventId) {
            return "event:" + eventType + ":" + eventId;
        }
        
        /**
         * 이벤트 패턴 키 생성 (Redis SCAN 명령어용)
         * @param eventType 이벤트 타입
         * @return "event:{eventType}:*" 형태의 패턴
         * 
         * 사용 예시: event:SESSION_STARTED:*
         * 용도: 특정 타입의 모든 이벤트를 검색할 때 사용
         */
        public static String eventPattern(String eventType) {
            return "event:" + eventType + ":*";
        }
        
        // ===== 추가 키 빌더 메소드들 =====
        
        /**
         * 사용자 통계 캐시 키 생성
         * @param userId 사용자 ID
         * @return "user:stats:{userId}" 형태의 키
         * 
         * 사용 예시: user:stats:user123
         * 저장 데이터: 사용자별 학습 통계 (총 세션 수, 평균 점수 등)
         */
        public static String userStatsKey(String userId) {
            return "user:stats:" + userId;
        }
        
        /**
         * 문제 분류 정보 캐시 키 생성
         * @return "categories:all" 형태의 키
         * 
         * 사용 예시: categories:all
         * 저장 데이터: 모든 문제 카테고리 정보 (주요 카테고리, 세부 카테고리 등)
         */
        public static String categoriesKey() {
            return "categories:all";
        }
        
        /**
         * 사용자 히스토리 캐시 키 생성
         * @param userId 사용자 ID
         * @param date 날짜 (YYYY-MM-DD 형식)
         * @return "user:history:{userId}:{date}" 형태의 키
         * 
         * 사용 예시: user:history:user123:2024-01-15
         * 저장 데이터: 특정 날짜의 사용자 학습 활동 기록
         */
        public static String userHistoryKey(String userId, String date) {
            return "user:history:" + userId + ":" + date;
        }
        
        /**
         * 임시 데이터 캐시 키 생성
         * @param type 데이터 타입 (예: learning-pattern, analysis-result)
         * @param id 식별자 (예: userId, sessionId)
         * @return "temp:{type}:{id}" 형태의 키
         * 
         * 사용 예시: temp:learning-pattern:user123
         * 저장 데이터: 임시로 사용되는 데이터 (분석 결과, 계산된 값 등)
         * 특징: 가장 짧은 TTL(15분)을 가집니다.
         */
        public static String tempKey(String type, String id) {
            return "temp:" + type + ":" + id;
        }
        
        /**
         * 분산 락(Distributed Lock) 캐시 키 생성
         * @param type 락 타입 (예: session, analysis)
         * @param id 락 대상 식별자
         * @return "lock:{type}:{id}" 형태의 키
         * 
         * 사용 예시: lock:session:user123
         * 저장 데이터: 락 상태 (locked/unlocked)
         * 용도: 동시 요청 처리 시 데이터 일관성 보장
         */
        public static String lockKey(String type, String id) {
            return "lock:" + type + ":" + id;
        }
    }

    // ===== 캐시 키 패턴 클래스 =====
    // Redis SCAN 명령어를 사용하여 특정 패턴의 키들을 검색할 때 사용합니다.
    
    /**
     * 캐시 키 패턴 (Redis SCAN 명령어용)
     * Redis에서 특정 패턴의 키들을 검색하거나 일괄 삭제할 때 사용합니다.
     * 
     * 사용 예시:
     * - 특정 사용자의 모든 세션 삭제
     * - 특정 타입의 모든 임시 데이터 정리
     * - 만료된 락 정리
     */
    public static class CacheKeyPattern {
        
        /**
         * 특정 사용자의 모든 세션 키 패턴
         * @param userId 사용자 ID
         * @return "learning_service:session:*" 형태의 패턴
         * 
         * 사용 예시: learning_service:session:*
         * 용도: 사용자 탈퇴 시 모든 세션 데이터 삭제
         */
        public static String userSessionsPattern(String userId) {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "session", "*");
        }
        
        /**
         * 특정 사용자의 모든 진행 상황 키 패턴
         * @param userId 사용자 ID
         * @return "learning_service:progress:*" 형태의 패턴
         * 
         * 사용 예시: learning_service:progress:*
         * 용도: 사용자 데이터 정리 시 진행 상황 데이터 삭제
         */
        public static String userProgressPattern(String userId) {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "progress", "*");
        }
        
        /**
         * 특정 사용자의 모든 통계 키 패턴
         * @param userId 사용자 ID
         * @return "learning_service:user_stats:{userId}" 형태의 패턴
         * 
         * 사용 예시: learning_service:user_stats:user123
         * 용도: 특정 사용자의 통계 데이터 삭제
         */
        public static String userStatsPattern(String userId) {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "user_stats", userId);
        }
        
        /**
         * 모든 임시 데이터 키 패턴
         * @return "learning_service:temp:*" 형태의 패턴
         * 
         * 사용 예시: learning_service:temp:*
         * 용도: 시스템 정리 시 모든 임시 데이터 삭제
         * 특징: 임시 데이터는 자동으로 만료되지만, 필요시 강제 삭제 가능
         */
        public static String tempDataPattern() {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "temp", "*");
        }
        
        /**
         * 모든 락 키 패턴
         * @return "learning_service:lock:*" 형태의 패턴
         * 
         * 사용 예시: learning_service:lock:*
         * 용도: 시스템 재시작 시 모든 락 정리
         * 특징: 락은 보통 TTL이 있지만, 시스템 장애 시 수동 정리가 필요할 수 있음
         */
        public static String lockPattern() {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "lock", "*");
        }
    }

    // ===== 캐시 데이터 구조 정의 클래스 =====
    // Redis에 저장되는 데이터의 구조를 정의합니다.
    // 각 필드명을 상수로 정의하여 일관성을 보장합니다.
    
    /**
     * 캐시 데이터 구조 정의
     * Redis에 저장되는 데이터의 필드명을 상수로 정의합니다.
     * 
     * 장점:
     * 1. 오타 방지: 필드명을 상수로 사용하여 오타 위험 감소
     * 2. 일관성 보장: 모든 곳에서 동일한 필드명 사용
     * 3. 리팩토링 용이: 필드명 변경 시 한 곳만 수정하면 됨
     * 4. 가독성 향상: 코드에서 필드의 의미를 명확하게 파악 가능
     */
    public static class CacheDataStructure {
        
        /**
         * 세션 진행 상황 데이터 구조
         * Redis Hash 형태로 저장됩니다.
         * 
         * 저장 예시:
         * {
         *   "status": "IN_PROGRESS",
         *   "answeredQuestions": "5",
         *   "progressPercentage": "50.0",
         *   "lastUpdatedAt": "2024-01-15T10:30:00",
         *   "currentQuestion": "q123",
         *   "nextQuestion": "q124"
         * }
         */
        public static class SessionProgress {
            /** 세션 상태 (STARTED, IN_PROGRESS, COMPLETED) */
            public static final String STATUS = "status";
            /** 현재까지 답변한 문제 수 */
            public static final String ANSWERED_QUESTIONS = "answeredQuestions";
            /** 진행률 (0.0 ~ 100.0) */
            public static final String PROGRESS_PERCENTAGE = "progressPercentage";
            /** 마지막 업데이트 시간 */
            public static final String LAST_UPDATED_AT = "lastUpdatedAt";
            /** 현재 풀고 있는 문제 ID */
            public static final String CURRENT_QUESTION = "currentQuestion";
            /** 다음 문제 ID */
            public static final String NEXT_QUESTION = "nextQuestion";
        }
        
        /**
         * 사용자 통계 데이터 구조
         * Redis Hash 형태로 저장됩니다.
         * 
         * 저장 예시:
         * {
         *   "totalSessions": "25",
         *   "completedSessions": "20",
         *   "totalScore": "850",
         *   "averageScore": "85.0",
         *   "totalTime": "3600",
         *   "lastActivity": "2024-01-15T10:30:00"
         * }
         */
        public static class UserStats {
            /** 총 학습 세션 수 */
            public static final String TOTAL_SESSIONS = "totalSessions";
            /** 완료된 세션 수 */
            public static final String COMPLETED_SESSIONS = "completedSessions";
            /** 총 점수 */
            public static final String TOTAL_SCORE = "totalScore";
            /** 평균 점수 */
            public static final String AVERAGE_SCORE = "averageScore";
            /** 총 학습 시간 (초) */
            public static final String TOTAL_TIME = "totalTime";
            /** 마지막 활동 시간 */
            public static final String LAST_ACTIVITY = "lastActivity";
        }
        
        /**
         * 문제 분류 데이터 구조
         * Redis Hash 형태로 저장됩니다.
         * 
         * 저장 예시:
         * {
         *   "majorCategories": "[\"BUSINESS\", \"DAILY_LIFE\", \"STUDY\", \"TRAVEL\"]",
         *   "minorCategories": "[\"MEETING_CONFERENCE\", \"EMAIL_REPORT\", ...]",
         *   "questionTypes": "[\"ANSWER_IN_CONTEXT\", \"FILL_IN_THE_BLANK\", \"IDIOM_IN_CONTEXT\"]",
         *   "statistics": "{\"totalQuestions\": 1000, \"byCategory\": {...}}"
         * }
         */
        public static class Categories {
            /** 주요 카테고리 목록 (JSON 배열 문자열) */
            public static final String MAJOR_CATEGORIES = "majorCategories";
            /** 세부 카테고리 목록 (JSON 배열 문자열) */
            public static final String MINOR_CATEGORIES = "minorCategories";
            /** 문제 타입 목록 (JSON 배열 문자열) */
            public static final String QUESTION_TYPES = "questionTypes";
            /** 카테고리별 통계 정보 (JSON 객체 문자열) */
            public static final String STATISTICS = "statistics";
        }
    }
}
