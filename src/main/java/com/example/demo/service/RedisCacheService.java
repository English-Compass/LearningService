package com.example.demo.service;

import com.example.demo.config.RedisCacheConfig;
import com.example.demo.entity.QuestionAnswer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * Redis 캐시 서비스
 * RedisCacheConfig의 키 구조를 활용하여 캐시 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ===== 세션 관련 캐시 =====

    /**
     * 학습 세션 캐시 저장
     */
    public void cacheSession(String sessionId, Object sessionData) {
        String key = RedisCacheConfig.CacheKeyBuilder.sessionKey(sessionId);
        try {
            redisTemplate.opsForValue().set(key, sessionData, RedisCacheConfig.SESSION_CACHE_TTL);
            log.debug("세션 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.SESSION_CACHE_TTL);
        } catch (Exception e) {
            log.error("세션 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 학습 세션 캐시 조회
     */
    public <T> Optional<T> getSession(String sessionId, Class<T> clazz) {
        String key = RedisCacheConfig.CacheKeyBuilder.sessionKey(sessionId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null && clazz.isInstance(cached)) {
                return Optional.of(clazz.cast(cached));
            }
        } catch (Exception e) {
            log.error("세션 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    /**
     * 세션 진행 상황 캐시 저장
     */
    public void cacheSessionProgress(String sessionId, Map<String, Object> progressData) {
        String key = RedisCacheConfig.CacheKeyBuilder.sessionProgressKey(sessionId);
        try {
            redisTemplate.opsForValue().set(key, progressData, RedisCacheConfig.PROGRESS_CACHE_TTL);
            log.debug("진행 상황 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.PROGRESS_CACHE_TTL);
        } catch (Exception e) {
            log.error("진행 상황 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 세션 진행 상황 캐시 조회
     */
    public Optional<Map<String, Object>> getSessionProgress(String sessionId) {
        String key = RedisCacheConfig.CacheKeyBuilder.sessionProgressKey(sessionId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Map) {
                return Optional.of((Map<String, Object>) cached);
            }
        } catch (Exception e) {
            log.error("진행 상황 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    /**
     * 세션별 할당된 문제 목록 캐시 저장
     */
    public void cacheSessionQuestions(String sessionId, List<String> questionIds) {
        String key = RedisCacheConfig.CacheKeyBuilder.sessionQuestionsKey(sessionId);
        try {
            redisTemplate.opsForValue().set(key, questionIds, RedisCacheConfig.SESSION_QUESTIONS_CACHE_TTL);
            log.debug("세션 문제 목록 캐시 저장 완료: key={}, questionCount={}", key, questionIds.size());
        } catch (Exception e) {
            log.error("세션 문제 목록 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 세션별 할당된 문제 목록 캐시 조회
     */
    public Optional<List<String>> getSessionQuestions(String sessionId) {
        String key = RedisCacheConfig.CacheKeyBuilder.sessionQuestionsKey(sessionId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<?> rawList = (List<?>) cached;
                // String 타입 검증
                List<String> questionIds = rawList.stream()
                    .filter(item -> item instanceof String)
                    .map(String.class::cast)
                    .toList();
                return Optional.of(questionIds);
            }
        } catch (Exception e) {
            log.error("세션 문제 목록 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    // ===== 사용자 관련 캐시 =====

    /**
     * 사용자 통계 캐시 저장
     */
    public void cacheUserStats(String userId, Map<String, Object> statsData) {
        String key = RedisCacheConfig.CacheKeyBuilder.userStatsKey(userId);
        try {
            redisTemplate.opsForValue().set(key, statsData, RedisCacheConfig.USER_STATS_CACHE_TTL);
            log.debug("사용자 통계 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.USER_STATS_CACHE_TTL);
        } catch (Exception e) {
            log.error("사용자 통계 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 사용자 통계 캐시 조회
     */
    public Optional<Map<String, Object>> getUserStats(String userId) {
        String key = RedisCacheConfig.CacheKeyBuilder.userStatsKey(userId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Map) {
                return Optional.of((Map<String, Object>) cached);
            }
        } catch (Exception e) {
            log.error("사용자 통계 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    /**
     * 사용자 현재 세션 캐시 저장
     */
    public void cacheUserCurrentSession(String userId, String sessionId) {
        String key = RedisCacheConfig.CacheKeyBuilder.userCurrentSessionKey(userId);
        try {
            redisTemplate.opsForValue().set(key, sessionId, RedisCacheConfig.SESSION_CACHE_TTL);
            log.debug("사용자 현재 세션 캐시 저장 완료: key={}, sessionId={}", key, sessionId);
        } catch (Exception e) {
            log.error("사용자 현재 세션 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 사용자 현재 세션 캐시 조회
     */
    public Optional<String> getUserCurrentSession(String userId) {
        String key = RedisCacheConfig.CacheKeyBuilder.userCurrentSessionKey(userId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof String) {
                return Optional.of((String) cached);
            }
        } catch (Exception e) {
            log.error("사용자 현재 세션 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    /**
     * 사용자별 세션 목록 캐시 저장
     */
    public void cacheUserSessions(String userId, List<?> sessions) {
        String key = RedisCacheConfig.CacheKeyBuilder.userSessionsKey(userId);
        try {
            redisTemplate.opsForValue().set(key, sessions, RedisCacheConfig.USER_SESSIONS_CACHE_TTL);
            log.debug("사용자 세션 목록 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.USER_SESSIONS_CACHE_TTL);
        } catch (Exception e) {
            log.error("사용자 세션 목록 캐시 저장 실패: key={}", key, e);
        }
    }

    // ===== 문제 분류 캐시 =====

    /**
     * 문제 분류 정보 캐시 저장
     */
    public void cacheCategories(Map<String, Object> categoriesData) {
        String key = RedisCacheConfig.CacheKeyBuilder.categoriesKey();
        try {
            redisTemplate.opsForValue().set(key, categoriesData, RedisCacheConfig.CATEGORY_CACHE_TTL);
            log.debug("문제 분류 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.CATEGORY_CACHE_TTL);
        } catch (Exception e) {
            log.error("문제 분류 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 문제 분류 정보 캐시 조회
     */
    public Optional<Map<String, Object>> getCategories() {
        String key = RedisCacheConfig.CacheKeyBuilder.categoriesKey();
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Map) {
                return Optional.of((Map<String, Object>) cached);
            }
        } catch (Exception e) {
            log.error("문제 분류 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    // ===== 히스토리 캐시 =====

    /**
     * 사용자 히스토리 캐시 저장
     */
    public void cacheUserHistory(String userId, String date, List<Object> historyData) {
        String key = RedisCacheConfig.CacheKeyBuilder.userHistoryKey(userId, date);
        try {
            redisTemplate.opsForValue().set(key, historyData, RedisCacheConfig.USER_STATS_CACHE_TTL);
            log.debug("사용자 히스토리 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.USER_STATS_CACHE_TTL);
        } catch (Exception e) {
            log.error("사용자 히스토리 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 사용자 히스토리 캐시 조회
     */
    public <T> Optional<List<T>> getUserHistory(String userId, String date, Class<T> clazz) {
        String key = RedisCacheConfig.CacheKeyBuilder.userHistoryKey(userId, date);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<?> list = (List<?>) cached;
                List<T> typedList = list.stream()
                    .filter(item -> clazz.isInstance(item))
                    .map(clazz::cast)
                    .toList();
                return Optional.of(typedList);
            }
        } catch (Exception e) {
            log.error("사용자 히스토리 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    // ===== 임시 데이터 캐시 =====

    /**
     * 임시 데이터 캐시 저장
     */
    public void cacheTempData(String type, String id, Object data) {
        String key = RedisCacheConfig.CacheKeyBuilder.tempKey(type, id);
        try {
            redisTemplate.opsForValue().set(key, data, RedisCacheConfig.TEMP_CACHE_TTL);
            log.debug("임시 데이터 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.TEMP_CACHE_TTL);
        } catch (Exception e) {
            log.error("임시 데이터 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 임시 데이터 캐시 조회
     */
    public <T> Optional<T> getTempData(String type, String id, Class<T> clazz) {
        String key = RedisCacheConfig.CacheKeyBuilder.tempKey(type, id);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null && clazz.isInstance(cached)) {
                return Optional.of(clazz.cast(cached));
            }
        } catch (Exception e) {
            log.error("임시 데이터 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }



    /**
     * 문제 답변 캐시 저장 (QuestionAnswer 객체)
     */
    public void cacheQuestionAnswer(String sessionId, QuestionAnswer answerData) {
        String key = RedisCacheConfig.CacheKeyBuilder.tempKey("answer", sessionId);
        try {
            redisTemplate.opsForValue().set(key, answerData, RedisCacheConfig.TEMP_CACHE_TTL);
            log.debug("문제 답변 캐시 저장 완료: key={}", key);
        } catch (Exception e) {
            log.error("문제 답변 캐시 저장 실패: key={}", key, e);
        }
    }

    // ===== 락(Lock) 관리 =====

    /**
     * 분산 락 획득
     */
    public boolean acquireLock(String type, String id, Duration timeout) {
        String key = RedisCacheConfig.CacheKeyBuilder.lockKey(type, id);
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "locked", timeout);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("락 획득 실패: key={}", key, e);
            return false;
        }
    }

    /**
     * 분산 락 해제
     */
    public boolean releaseLock(String type, String id) {
        String key = RedisCacheConfig.CacheKeyBuilder.lockKey(type, id);
        try {
            Boolean result = redisTemplate.delete(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("락 해제 실패: key={}", key, e);
            return false;
        }
    }

    // ===== 캐시 관리 =====

    /**
     * 특정 패턴의 캐시 키들 조회
     */
    public Set<String> getKeysByPattern(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("패턴 기반 키 조회 실패: pattern={}", pattern, e);
            return Set.of();
        }
    }

    /**
     * 특정 패턴의 캐시 삭제
     */
    public long deleteKeysByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.info("패턴 기반 캐시 삭제 완료: pattern={}, deleted={}", pattern, deleted);
                return deleted != null ? deleted : 0;
            }
        } catch (Exception e) {
            log.error("패턴 기반 캐시 삭제 실패: pattern={}", pattern, e);
        }
        return 0;
    }

    /**
     * 특정 키의 TTL 조회
     */
    public Optional<Duration> getTTL(String key) {
        try {
            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttlSeconds != null && ttlSeconds > 0) {
                return Optional.of(Duration.ofSeconds(ttlSeconds));
            }
        } catch (Exception e) {
            log.error("TTL 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    /**
     * 특정 키의 TTL 설정
     */
    public boolean setTTL(String key, Duration ttl) {
        try {
            Boolean result = redisTemplate.expire(key, ttl);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("TTL 설정 실패: key={}, ttl={}", key, ttl, e);
            return false;
        }
    }

    /**
     * 사용자별 현재 세션 캐시 저장
     */
    public void cacheCurrentSession(String userId, Object currentSession) {
        String key = RedisCacheConfig.CacheKeyBuilder.userCurrentSessionKey(userId);
        try {
            redisTemplate.opsForValue().set(key, currentSession, RedisCacheConfig.CURRENT_SESSION_CACHE_TTL);
            log.debug("사용자 현재 세션 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.CURRENT_SESSION_CACHE_TTL);
        } catch (Exception e) {
            log.error("사용자 현재 세션 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 사용자별 현재 세션 캐시 조회
     */
    public <T> Optional<T> getCurrentSession(String userId, Class<T> clazz) {
        String key = RedisCacheConfig.CacheKeyBuilder.userCurrentSessionKey(userId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null && clazz.isInstance(cached)) {
                return Optional.of(clazz.cast(cached));
            }
        } catch (Exception e) {
            log.error("사용자 현재 세션 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    /**
     * 이벤트 캐시 저장
     */
    public void cacheEvent(String eventType, String eventId, Object eventData) {
        String key = RedisCacheConfig.CacheKeyBuilder.eventKey(eventType, eventId);
        try {
            redisTemplate.opsForValue().set(key, eventData, RedisCacheConfig.EVENT_CACHE_TTL);
            log.debug("이벤트 캐시 저장 완료: key={}, ttl={}", key, RedisCacheConfig.EVENT_CACHE_TTL);
        } catch (Exception e) {
            log.error("이벤트 캐시 저장 실패: key={}", key, e);
        }
    }

    /**
     * 이벤트 캐시 조회
     */
    public <T> Optional<T> getEvent(String eventType, String eventId, Class<T> clazz) {
        String key = RedisCacheConfig.CacheKeyBuilder.eventKey(eventType, eventId);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return Optional.of(clazz.cast(cached));
            }
        } catch (Exception e) {
            log.error("이벤트 캐시 조회 실패: key={}", key, e);
        }
        return Optional.empty();
    }

    /**
     * 이벤트 타입별 이벤트 목록 조회
     */
    public <T> List<T> getEventsByType(String eventType, Class<T> clazz) {
        String pattern = RedisCacheConfig.CacheKeyBuilder.eventPattern(eventType);
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                List<T> events = new ArrayList<>();
                for (String key : keys) {
                    Object cached = redisTemplate.opsForValue().get(key);
                    if (cached != null && clazz.isInstance(cached)) {
                        events.add(clazz.cast(cached));
                    }
                }
                return events;
            }
        } catch (Exception e) {
            log.error("이벤트 타입별 조회 실패: pattern={}", pattern, e);
        }
        return List.of();
    }
}
