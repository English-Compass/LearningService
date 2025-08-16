package com.example.demo.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 캐시 키 구조 및 설정 관리
 */
@Component
public class RedisCacheConfig {

    /**
     * 캐시 키 접두사 상수
     */
    public static final String KEY_PREFIX = "learning_service";
    
    /**
     * 캐시 키 구분자
     */
    public static final String KEY_SEPARATOR = ":";
    
    // 캐시 TTL 상수들
    public static final Duration SESSION_CACHE_TTL = Duration.ofHours(1);
    public static final Duration SESSION_PROGRESS_CACHE_TTL = Duration.ofMinutes(30);
    public static final Duration SESSION_QUESTIONS_CACHE_TTL = Duration.ofHours(2); // 세션 문제 목록은 2시간
    public static final Duration USER_SESSIONS_CACHE_TTL = Duration.ofHours(2);
    public static final Duration CURRENT_SESSION_CACHE_TTL = Duration.ofHours(1);
    public static final Duration STATISTICS_CACHE_TTL = Duration.ofHours(6);
    public static final Duration EVENT_CACHE_TTL = Duration.ofDays(1); // 이벤트는 1일간 보관
    
    // 추가 TTL 상수들
    public static final Duration PROGRESS_CACHE_TTL = Duration.ofMinutes(30); // 진행 상황
    public static final Duration USER_STATS_CACHE_TTL = Duration.ofHours(6); // 사용자 통계
    public static final Duration CATEGORY_CACHE_TTL = Duration.ofDays(1); // 문제 분류
    public static final Duration TEMP_CACHE_TTL = Duration.ofMinutes(15); // 임시 데이터

    // 캐시 키 빌더
    public static class CacheKeyBuilder {
        
        public static String sessionKey(String sessionId) {
            return "session:" + sessionId;
        }
        
        public static String sessionProgressKey(String sessionId) {
            return "session:progress:" + sessionId;
        }
        
        public static String sessionQuestionsKey(String sessionId) {
            return "session:questions:" + sessionId;
        }
        
        public static String userSessionsKey(String userId) {
            return "user:sessions:" + userId;
        }
        
        public static String userCurrentSessionKey(String userId) {
            return "user:current:" + userId;
        }
        
        public static String sessionStatisticsKey(String sessionId) {
            return "session:stats:" + sessionId;
        }
        
        public static String eventKey(String eventType, String eventId) {
            return "event:" + eventType + ":" + eventId;
        }
        
        public static String eventPattern(String eventType) {
            return "event:" + eventType + ":*";
        }
        
        // 추가 키 빌더 메소드들
        public static String userStatsKey(String userId) {
            return "user:stats:" + userId;
        }
        
        public static String categoriesKey() {
            return "categories:all";
        }
        
        public static String userHistoryKey(String userId, String date) {
            return "user:history:" + userId + ":" + date;
        }
        
        public static String tempKey(String type, String id) {
            return "temp:" + type + ":" + id;
        }
        
        public static String lockKey(String type, String id) {
            return "lock:" + type + ":" + id;
        }
    }

    /**
     * 캐시 키 패턴 (Redis SCAN 명령어용)
     */
    public static class CacheKeyPattern {
        
        /**
         * 특정 사용자의 모든 세션 키 패턴
         */
        public static String userSessionsPattern(String userId) {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "session", "*");
        }
        
        /**
         * 특정 사용자의 모든 진행 상황 키 패턴
         */
        public static String userProgressPattern(String userId) {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "progress", "*");
        }
        
        /**
         * 특정 사용자의 모든 통계 키 패턴
         */
        public static String userStatsPattern(String userId) {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "user_stats", userId);
        }
        
        /**
         * 모든 임시 데이터 키 패턴
         */
        public static String tempDataPattern() {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "temp", "*");
        }
        
        /**
         * 모든 락 키 패턴
         */
        public static String lockPattern() {
            return String.join(KEY_SEPARATOR, KEY_PREFIX, "lock", "*");
        }
    }

    /**
     * 캐시 데이터 구조 정의
     */
    public static class CacheDataStructure {
        
        /**
         * 세션 진행 상황 데이터 구조
         */
        public static class SessionProgress {
            public static final String STATUS = "status";
            public static final String ANSWERED_QUESTIONS = "answeredQuestions";
            public static final String PROGRESS_PERCENTAGE = "progressPercentage";
            public static final String LAST_UPDATED_AT = "lastUpdatedAt";
            public static final String CURRENT_QUESTION = "currentQuestion";
            public static final String NEXT_QUESTION = "nextQuestion";
        }
        
        /**
         * 사용자 통계 데이터 구조
         */
        public static class UserStats {
            public static final String TOTAL_SESSIONS = "totalSessions";
            public static final String COMPLETED_SESSIONS = "completedSessions";
            public static final String TOTAL_SCORE = "totalScore";
            public static final String AVERAGE_SCORE = "averageScore";
            public static final String TOTAL_TIME = "totalTime";
            public static final String LAST_ACTIVITY = "lastActivity";
        }
        
        /**
         * 문제 분류 데이터 구조
         */
        public static class Categories {
            public static final String MAJOR_CATEGORIES = "majorCategories";
            public static final String MINOR_CATEGORIES = "minorCategories";
            public static final String QUESTION_TYPES = "questionTypes";
            public static final String STATISTICS = "statistics";
        }
    }
}
