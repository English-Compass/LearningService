package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 분석 완료 이벤트 DTO — ProblemService 문제 할당에 필요한 핵심 데이터만 포함
 * 정답률·학습시간 등 통계 지표는 프론트가 직접 산출하므로 제외
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisCompletedEvent {

    @Builder.Default
    private String eventType = "ANALYSIS_COMPLETED";

    private String userId;
    private String sessionId;

    /** 취약 문제 유형 (정답률 60% 미만): word / sentence / conversation */
    private List<String> weakQuestionTypes;

    /** 최근 2주 내 오답 문제 ID 목록 */
    private List<String> wrongQuestionIds;

    /** 복습 추천 문제 ID 목록 */
    private List<String> recommendedReviewQuestions;

    /** 학습 패턴: IMPROVING / STABLE / STRUGGLING */
    private String learningPattern;

    /** 이벤트 타임스탬프 (epoch millis) */
    private long timestamp;
}

