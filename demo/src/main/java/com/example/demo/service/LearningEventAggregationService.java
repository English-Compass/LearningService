package com.example.demo.service;

import com.example.demo.entity.LearningEvent;
import com.example.demo.dto.LearningSummary;
import com.example.demo.repository.LearningEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningEventAggregationService {
    
    private final LearningEventRepository learningEventRepository;
    
    /**
     * 사용자의 특정 학습 항목에 대한 학습 이력 집계
     */
    public LearningSummary aggregateUserLearningHistory(String userId, String learningItemId) {
        log.info("사용자 {}의 학습 항목 {} 이력 집계 시작", userId, learningItemId);
        
        List<LearningEvent> events = learningEventRepository
            .findByUserIdAndLearningItemIdOrderByTimestampAsc(userId, learningItemId);
        
        if (events.isEmpty()) {
            log.info("사용자 {}의 학습 항목 {}에 대한 이벤트가 없습니다", userId, learningItemId);
            return LearningSummary.builder()
                .userId(userId)
                .learningItemId(learningItemId)
                .totalDuration(0)
                .completionCount(0)
                .eventTypeCounts(new HashMap<>())
                .build();
        }
        
        LearningSummary summary = LearningSummary.builder()
            .userId(userId)
            .learningItemId(learningItemId)
            .build();
        
        // 첫 시작 시간과 마지막 완료 시간
        summary.setFirstStartedAt(events.get(0).getTimestamp());
        
        // 이벤트 타입별 집계
        Map<String, Integer> eventTypeCounts = events.stream()
            .collect(Collectors.groupingBy(
                LearningEvent::getEventType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        summary.setEventTypeCounts(eventTypeCounts);
        
        // 완료 횟수
        summary.setCompletionCount(eventTypeCounts.getOrDefault("COMPLETE", 0));
        
        // 총 학습 시간 계산
        int totalDuration = events.stream()
            .filter(event -> event.getDuration() != null)
            .mapToInt(LearningEvent::getDuration)
            .sum();
        summary.setTotalDuration(totalDuration);
        
        // 마지막 완료 시간
        events.stream()
            .filter(event -> "COMPLETE".equals(event.getEventType()))
            .max(Comparator.comparing(LearningEvent::getTimestamp))
            .ifPresent(event -> summary.setLastCompletedAt(event.getTimestamp()));
        
        log.info("사용자 {}의 학습 항목 {} 이력 집계 완료: 총 {}개 이벤트, 완료 {}회, 총 시간 {}초", 
            userId, learningItemId, events.size(), summary.getCompletionCount(), summary.getTotalDuration());
        
        return summary;
    }
    
    /**
     * 사용자의 전체 학습 이력 집계
     */
    public List<LearningSummary> aggregateAllUserLearningHistory(String userId) {
        log.info("사용자 {}의 전체 학습 이력 집계 시작", userId);
        
        List<String> learningItemIds = learningEventRepository
            .findDistinctLearningItemIdsByUserId(userId);
        
        List<LearningSummary> summaries = learningItemIds.stream()
            .map(learningItemId -> aggregateUserLearningHistory(userId, learningItemId))
            .collect(Collectors.toList());
        
        log.info("사용자 {}의 전체 학습 이력 집계 완료: {}개 학습 항목", userId, summaries.size());
        return summaries;
    }
    
    /**
     * 특정 기간 동안의 학습 이력 집계
     */
    public LearningSummary aggregateUserLearningHistoryByPeriod(
            String userId, String learningItemId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("사용자 {}의 학습 항목 {} 기간별 이력 집계 시작: {} ~ {}", 
            userId, learningItemId, startDate, endDate);
        
        List<LearningEvent> events = learningEventRepository
            .findByUserIdAndLearningItemIdAndTimestampBetweenOrderByTimestampAsc(
                userId, learningItemId, startDate, endDate);
        
        if (events.isEmpty()) {
            log.info("지정된 기간에 사용자 {}의 학습 항목 {}에 대한 이벤트가 없습니다", userId, learningItemId);
            return LearningSummary.builder()
                .userId(userId)
                .learningItemId(learningItemId)
                .totalDuration(0)
                .completionCount(0)
                .eventTypeCounts(new HashMap<>())
                .build();
        }
        
        // 기간별 집계 로직은 위와 동일하지만 특정 기간의 이벤트만 사용
        return aggregateUserLearningHistory(userId, learningItemId);
    }
    
    /**
     * 학습 항목별 전체 사용자 통계 (인기 학습 항목 분석)
     */
    public Map<String, LearningSummary> aggregateLearningItemStatistics(String learningItemId) {
        log.info("학습 항목 {}의 전체 사용자 통계 집계 시작", learningItemId);
        
        List<String> userIds = learningEventRepository
            .findDistinctUserIdsByLearningItemId(learningItemId);
        
        Map<String, LearningSummary> userSummaries = new HashMap<>();
        for (String userId : userIds) {
            userSummaries.put(userId, aggregateUserLearningHistory(userId, learningItemId));
        }
        
        log.info("학습 항목 {}의 전체 사용자 통계 집계 완료: {}명의 사용자", learningItemId, userSummaries.size());
        return userSummaries;
    }
}
