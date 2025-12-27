package com.example.demo.dto;

import com.example.demo.entity.QuestionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * QuestionCategory 엔티티 기반 DTO 클래스들
 */
public class QuestionCategoryDto {

    /**
     * 문제 분류 선택 요청 DTO
     * QuestionCategory 열거형 기반
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySelectionRequest {
        // QuestionCategory.MajorCategory 기반
        private List<QuestionCategory.MajorCategory> selectedMajorCategories;
        
        // QuestionCategory.MinorCategory 기반
        private Map<QuestionCategory.MajorCategory, List<QuestionCategory.MinorCategory>> selectedMinorCategories;
        
        // QuestionCategory.QuestionType 기반
        private List<QuestionCategory.QuestionType> selectedQuestionTypes;
        
        // 난이도 범위 (QuestionAnswer.difficulty 1-3)
        private Integer minDifficulty;
        private Integer maxDifficulty;
        
        // 문제 수 설정
        private Integer questionCount;
    }

    /**
     * 문제 분류 응답 DTO
     * QuestionCategory 열거형 정보 제공
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfoResponse {
        // QuestionCategory.MajorCategory 정보
        private List<MajorCategoryInfo> majorCategories;
        
        // QuestionCategory.MinorCategory 정보
        private List<MinorCategoryInfo> minorCategories;
        
        // QuestionCategory.QuestionType 정보
        private List<QuestionTypeInfo> questionTypes;
        
        // QuestionCategory.Option 정보
        private List<OptionInfo> options;
        
        // 통계 정보
        private CategoryStatistics statistics;
    }

    /**
     * 대분류 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorCategoryInfo {
        private QuestionCategory.MajorCategory category;
        private String description;
        private Integer minorCategoryCount;
        private Integer totalQuestionTypes;
        private List<QuestionCategory.MinorCategory> minorCategories;
    }

    /**
     * 소분류 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinorCategoryInfo {
        private QuestionCategory.MinorCategory category;
        private String description;
        private QuestionCategory.MajorCategory majorCategory;
        private Integer questionTypeCount;
        private List<QuestionCategory.QuestionType> questionTypes;
    }

    /**
     * 문제 유형 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTypeInfo {
        private QuestionCategory.QuestionType type;
        private String description;
        private List<QuestionCategory.MinorCategory> applicableMinorCategories;
    }

    /**
     * 보기 옵션 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private QuestionCategory.Option option;
        private String value;
        private String displayText;
    }

    /**
     * 분류 통계 DTO
     * QuestionCategory 유틸리티 메서드 기반
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatistics {
        private Integer totalMajorCategories;
        private Integer totalMinorCategories;
        private Integer totalQuestionTypes;
        private Integer totalCombinations;
        
        // QuestionCategory.getTotalQuestionTypes() 기반
        private Map<QuestionCategory.MajorCategory, Integer> questionsPerMajorCategory;
        
        // QuestionCategory.getTotalQuestionTypes() 기반
        private Integer totalQuestions;
    }

    /**
     * 문제 풀이 세션용 문제 분류 DTO
     * 실제 문제 풀이 시 사용
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionCategoryInfo {
        private String sessionId;
        private String userId;
        
        // 선택된 분류들
        private List<QuestionCategory.MajorCategory> selectedMajorCategories;
        private List<QuestionCategory.MinorCategory> selectedMinorCategories;
        private List<QuestionCategory.QuestionType> selectedQuestionTypes;
        
        // 문제 수 정보
        private Integer totalQuestions;
        private Integer questionsPerCategory;
        
        // 난이도 설정
        private Integer difficultyLevel;
        private Integer minDifficulty;
        private Integer maxDifficulty;
    }
}
