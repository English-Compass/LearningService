package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 오답/복습 문제 제공 서비스
 * 학습 패턴 분석 결과를 바탕으로 사용자에게 맞춤형 복습 문제 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    /**
     * 학습 진행 분석 결과를 바탕으로 사용자 오답 문제 업데이트
     */
    public void updateUserReviewQuestions(LearningPatternAnalysisService.LearningProgressAnalysis analysis) {
        try {
            if (!analysis.getIsCorrect()) {
                log.info("오답 문제 업데이트: userId={}, questionId={}, sessionId={}", 
                    analysis.getUserId(), analysis.getQuestionId(), analysis.getSessionId());
                
                // TODO: 사용자 오답 풀이 문제 엔티티에 저장
                // UserReviewQuestion reviewQuestion = UserReviewQuestion.builder()
                //     .userId(analysis.getUserId())
                //     .originalQuestionId(analysis.getQuestionId())
                //     .sessionId(analysis.getSessionId())
                //     .weakCategories(identifyWeakCategories(analysis))
                //     .priority(calculatePriority(analysis))
                //     .createdAt(LocalDateTime.now())
                //     .build();
                // 
                // reviewQuestionRepository.save(reviewQuestion);
                
                log.info("오답 문제 업데이트 완료: userId={}, questionId={}", 
                    analysis.getUserId(), analysis.getQuestionId());
            }
        } catch (Exception e) {
            log.error("오답 문제 업데이트 실패: userId={}, questionId={}", 
                analysis.getUserId(), analysis.getQuestionId(), e);
        }
    }

    /**
     * 학습 완료 분석 결과를 바탕으로 개인화된 복습 문제 세트 생성
     */
    public void createPersonalizedReviewSet(LearningPatternAnalysisService.CompleteLearningAnalysis analysis) {
        try {
            log.info("개인화된 복습 문제 세트 생성: userId={}, sessionId={}, weakCategories={}", 
                analysis.getUserId(), analysis.getSessionId(), analysis.getWeakCategories());
            
            // TODO: 취약 영역 기반으로 복습 문제 세트 생성
            // List<ReviewQuestionSet> reviewSets = generateReviewSets(analysis);
            
            // TODO: 사용자에게 맞춤형 복습 문제 제공
            // providePersonalizedReview(analysis.getUserId(), reviewSets);
            
            log.info("개인화된 복습 문제 세트 생성 완료: userId={}, sessionId={}", 
                analysis.getUserId(), analysis.getSessionId());
                
        } catch (Exception e) {
            log.error("개인화된 복습 문제 세트 생성 실패: userId={}, sessionId={}", 
                analysis.getUserId(), analysis.getSessionId(), e);
        }
    }

    /**
     * 취약 영역 식별
     */
    private List<String> identifyWeakCategories(LearningPatternAnalysisService.LearningProgressAnalysis analysis) {
        // TODO: 학습 패턴을 기반으로 취약 영역 식별
        return List.of("ALGEBRA", "CALCULUS");
    }

    /**
     * 문제 우선순위 계산
     */
    private Integer calculatePriority(LearningPatternAnalysisService.LearningProgressAnalysis analysis) {
        // TODO: 학습 패턴을 기반으로 우선순위 계산
        if (analysis.getConsecutiveWrong() >= 2) {
            return 3; // 높은 우선순위
        } else if (analysis.getTimeSpent() != null && analysis.getTimeSpent() > 60) {
            return 2; // 중간 우선순위
        } else {
            return 1; // 낮은 우선순위
        }
    }
}
