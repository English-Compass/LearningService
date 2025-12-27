package com.example.demo.service;

import com.example.demo.dto.problem.SessionDataResponseDto;
import com.example.demo.entity.LearningSession;
import com.example.demo.entity.LearningSessionEvent;
import com.example.demo.entity.QuestionAnswer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProblemService API 응답을 엔티티로 매핑하는 서비스
 * API 응답 DTO를 LearningService의 엔티티로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionDataMappingService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * API 응답을 LearningSession 엔티티로 변환
     */
    public LearningSession mapToLearningSession(SessionDataResponseDto.SessionDto sessionDto) {
        try {
            log.debug("   ├─ LearningSession 매핑 시작: sessionId={}", sessionDto.getSessionId());
            
            // sessionType 변환
            LearningSession.SessionType sessionType = LearningSession.SessionType.valueOf(
                sessionDto.getSessionType()
            );
            
            // status 변환
            LearningSession.SessionStatus status = LearningSession.SessionStatus.valueOf(
                sessionDto.getStatus()
            );
            
            // metadata를 JSON 문자열로 변환
            String metadataJson = sessionDto.getMetadata() != null 
                ? objectMapper.writeValueAsString(sessionDto.getMetadata())
                : null;
            
            LearningSession session = LearningSession.builder()
                .sessionId(sessionDto.getSessionId())
                .userId(sessionDto.getUserId())
                .sessionType(sessionType)
                .status(status)
                .startedAt(sessionDto.getStartedAt())
                .completedAt(sessionDto.getCompletedAt())
                .createdAt(sessionDto.getCreatedAt() != null ? sessionDto.getCreatedAt() : LocalDateTime.now())
                .updatedAt(sessionDto.getUpdatedAt() != null ? sessionDto.getUpdatedAt() : LocalDateTime.now())
                .sessionMetadata(metadataJson)
                .build();
            
            log.debug("   └─ LearningSession 매핑 완료: sessionId={}, type={}, status={}", 
                session.getSessionId(), sessionType, status);
            return session;
                
        } catch (Exception e) {
            log.error("LearningSession 매핑 실패: sessionId={}", sessionDto.getSessionId(), e);
            throw new RuntimeException("LearningSession 매핑 실패", e);
        }
    }
    
    /**
     * API 응답을 QuestionAnswer 엔티티 리스트로 변환
     */
    public List<QuestionAnswer> mapToQuestionAnswers(
            List<SessionDataResponseDto.QuestionAnswerDto> questionDtos, 
            String sessionId,
            String sessionType) {
        
        log.debug("   ├─ QuestionAnswer 매핑 시작: sessionId={}, 문제 수={}", sessionId, 
            questionDtos != null ? questionDtos.size() : 0);
        
        List<QuestionAnswer> answers = questionDtos.stream()
            .map(dto -> mapToQuestionAnswer(dto, sessionId, sessionType))
            .collect(Collectors.toList());
        
        log.debug("   └─ QuestionAnswer 매핑 완료: {}개", answers.size());
        return answers;
    }
    
    /**
     * API 응답을 QuestionAnswer 엔티티로 변환
     */
    private QuestionAnswer mapToQuestionAnswer(
            SessionDataResponseDto.QuestionAnswerDto dto, 
            String sessionId,
            String sessionType) {
        
        try {
            // ProblemService API 응답에서 문제 메타데이터 포함
            return new QuestionAnswer(
                null, // id는 자동 생성
                sessionId,
                dto.getQuestionId(),
                sessionType, // 세션 타입 설정
                dto.getQuestionType(), // 문제 유형 (분석용)
                dto.getMajorCategory(), // 대분류 (분석용)
                dto.getMinorCategory(), // 소분류 (분석용)
                dto.getDifficultyLevel(), // 난이도 (분석용)
                dto.getUserAnswer(),
                dto.getIsCorrect(),
                dto.getTimeSpent(),
                dto.getAnsweredAt(),
                dto.getSolveCount() != null ? dto.getSolveCount() : 1
            );
            
        } catch (Exception e) {
            log.error("QuestionAnswer 매핑 실패: questionId={}, sessionId={}", 
                dto.getQuestionId(), sessionId, e);
            throw new RuntimeException("QuestionAnswer 매핑 실패", e);
        }
    }
    
    /**
     * API 응답을 LearningSessionEvent 엔티티 리스트로 변환
     */
    public List<LearningSessionEvent> mapToLearningSessionEvents(
            List<SessionDataResponseDto.SessionEventDto> eventDtos) {
        
        if (eventDtos == null || eventDtos.isEmpty()) {
            log.debug("   └─ LearningSessionEvent 매핑: 이벤트 없음");
            return List.of();
        }
        
        log.debug("   ├─ LearningSessionEvent 매핑 시작: 이벤트 수={}", eventDtos.size());
        List<LearningSessionEvent> events = eventDtos.stream()
            .map(this::mapToLearningSessionEvent)
            .collect(Collectors.toList());
        log.debug("   └─ LearningSessionEvent 매핑 완료: {}개", events.size());
        return events;
    }
    
    /**
     * API 응답을 LearningSessionEvent 엔티티로 변환
     */
    private LearningSessionEvent mapToLearningSessionEvent(
            SessionDataResponseDto.SessionEventDto dto) {
        
        try {
            // metadata를 JSON 문자열로 변환
            String metadataJson = dto.getMetadata() != null 
                ? objectMapper.writeValueAsString(dto.getMetadata())
                : null;
            
            return LearningSessionEvent.builder()
                .eventId(dto.getEventId())
                .sessionId(dto.getSessionId())
                .userId(dto.getUserId())
                .eventType(dto.getEventType())
                .sessionType(null) // API 응답에 없으면 null
                .createdAt(dto.getCreatedAt())
                .eventMetadata(metadataJson)
                .build();
                
        } catch (Exception e) {
            log.error("LearningSessionEvent 매핑 실패: eventId={}", dto.getEventId(), e);
            throw new RuntimeException("LearningSessionEvent 매핑 실패", e);
        }
    }
}

