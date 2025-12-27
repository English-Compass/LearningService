package com.example.demo.dto;

import com.example.demo.entity.QuestionCategory;
import com.example.demo.dto.user.UserProfileInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 프로필 관련 DTO 클래스들
 */
public class UserProfileDto {

    /**
     * 초기 관심사 설정 요청 DTO (회원가입 후)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialInterestsRequest {
        private QuestionCategory.MajorCategory majorCategory;           // 선택된 대분류
        private List<QuestionCategory.MinorCategory> minorCategories;  // 선택된 소분류들 (1~3개)
        private List<QuestionCategory.QuestionType> questionTypes;     // 선택된 문제 유형들 (1~3개)
        private UserProfileInfo.LearningStyle learningStyle;           // 학습 스타일
        private UserProfileInfo.DifficultyLevel difficultyPreference;  // 선호 난이도
    }

    /**
     * 관심사 수정 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateInterestsRequest {
        private QuestionCategory.MajorCategory majorCategory;           // 수정할 대분류
        private List<QuestionCategory.MinorCategory> minorCategories;  // 수정할 소분류들 (1~3개)
        private List<QuestionCategory.QuestionType> questionTypes;     // 수정할 문제 유형들 (1~3개)
    }

    /**
     * 관심사 정보 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestsResponse {
        private String userId;
        private QuestionCategory.MajorCategory majorCategory;           // 현재 대분류
        private List<QuestionCategory.MinorCategory> minorCategories;  // 현재 소분류들
        private List<QuestionCategory.QuestionType> questionTypes;     // 현재 문제 유형들
        private UserProfileInfo.LearningStyle learningStyle;           // 학습 스타일
        private UserProfileInfo.DifficultyLevel difficultyPreference;  // 선호 난이도
        private LocalDateTime interestsLastUpdated;                    // 마지막 수정 시간
        private Integer interestsVersion;                              // 관심사 버전
        private boolean hasInterestsSet;                               // 관심사 설정 여부
    }

    /**
     * 관심사 통계 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestsStatistics {
        private String userId;
        private QuestionCategory.MajorCategory majorCategory;
        private Integer totalMinorCategories;                          // 선택 가능한 총 소분류 수
        private Integer selectedMinorCategories;                       // 현재 선택된 소분류 수
        private Integer totalQuestionTypes;                           // 선택 가능한 총 문제 유형 수
        private Integer selectedQuestionTypes;                         // 현재 선택된 문제 유형 수
        private LocalDateTime firstInterestsSet;                       // 최초 관심사 설정 시간
        private LocalDateTime lastInterestsUpdated;                    // 마지막 관심사 수정 시간
        private Integer totalUpdates;                                  // 총 수정 횟수
    }
}
