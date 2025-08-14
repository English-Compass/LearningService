package com.example.demo.controller;

import com.example.demo.dto.LearningSummary;
import com.example.demo.entity.LearningEvent;
import com.example.demo.service.LearningEventAggregationService;
import com.example.demo.repository.LearningEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-events")
@RequiredArgsConstructor
@Slf4j
public class LearningEventController {
    
    private final LearningEventAggregationService aggregationService;
    private final LearningEventRepository learningEventRepository;
    
    /**
     * 학습 이벤트 기록
     */
    @PostMapping
    public ResponseEntity<LearningEvent> recordLearningEvent(@RequestBody LearningEvent event) {
        log.info("학습 이벤트 기록 요청: 사용자={}, 항목={}, 타입={}", 
            event.getUserId(), event.getLearningItemId(), event.getEventType());
        
        event.setTimestamp(LocalDateTime.now());
        LearningEvent savedEvent = learningEventRepository.save(event);
        
        log.info("학습 이벤트 기록 완료: ID={}", savedEvent.getId());
        return ResponseEntity.ok(savedEvent);
    }
    
    /**
     * 사용자의 특정 학습 항목 이력 집계
     */
    @GetMapping("/users/{userId}/items/{learningItemId}/summary")
    public ResponseEntity<LearningSummary> getUserLearningSummary(
            @PathVariable String userId,
            @PathVariable String learningItemId) {
        log.info("사용자 {}의 학습 항목 {} 이력 집계 요청", userId, learningItemId);
        
        LearningSummary summary = aggregationService.aggregateUserLearningHistory(userId, learningItemId);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * 사용자의 전체 학습 이력 집계
     */
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<List<LearningSummary>> getAllUserLearningSummary(@PathVariable String userId) {
        log.info("사용자 {}의 전체 학습 이력 집계 요청", userId);
        
        List<LearningSummary> summaries = aggregationService.aggregateAllUserLearningHistory(userId);
        return ResponseEntity.ok(summaries);
    }
    
    /**
     * 특정 기간의 학습 이력 집계
     */
    @GetMapping("/users/{userId}/items/{learningItemId}/summary/period")
    public ResponseEntity<LearningSummary> getUserLearningSummaryByPeriod(
            @PathVariable String userId,
            @PathVariable String learningItemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("사용자 {}의 학습 항목 {} 기간별 이력 집계 요청: {} ~ {}", 
            userId, learningItemId, startDate, endDate);
        
        LearningSummary summary = aggregationService.aggregateUserLearningHistoryByPeriod(
            userId, learningItemId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * 학습 항목별 전체 사용자 통계
     */
    @GetMapping("/items/{learningItemId}/statistics")
    public ResponseEntity<Map<String, LearningSummary>> getLearningItemStatistics(
            @PathVariable String learningItemId) {
        log.info("학습 항목 {}의 전체 사용자 통계 요청", learningItemId);
        
        Map<String, LearningSummary> statistics = aggregationService.aggregateLearningItemStatistics(learningItemId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 사용자의 최근 학습 이벤트 조회
     */
    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<List<LearningEvent>> getRecentLearningEvents(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("사용자 {}의 최근 학습 이벤트 조회 요청: 최대 {}개", userId, limit);
        
        List<LearningEvent> events = learningEventRepository.findRecentEventsByUserIdWithLimit(userId, limit);
        return ResponseEntity.ok(events);
    }
}
