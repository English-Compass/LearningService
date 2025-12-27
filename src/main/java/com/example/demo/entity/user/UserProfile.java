package com.example.demo.entity.user;

import com.example.demo.entity.QuestionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 프로필 엔티티 (UserService에서 관리)
 * 사용자의 관심사, 학습 목적, 선호도 등을 저장
 */
@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "learning_purpose", nullable = false)
    @Enumerated(EnumType.STRING)
    private LearningPurpose learningPurpose; // 학습 목적 (대분류)

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private List<String> interests; // 관심 분야들

    @Column(name = "difficulty_preference")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyPreference; // 선호 난이도

    @Column(name = "learning_style")
    @Enumerated(EnumType.STRING)
    private LearningStyle learningStyle; // 학습 스타일

    // 사용자가 선택한 문제 분류 정보 (관심사)
    @ElementCollection
    @CollectionTable(name = "user_selected_minor_categories", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "minor_category")
    private List<QuestionCategory.MinorCategory> selectedMinorCategories; // 선택된 소분류들

    // 관심사 관련 메타데이터
    @Column(name = "interests_last_updated")
    private LocalDateTime interestsLastUpdated; // 관심사 마지막 수정 시간

    @Column(name = "interests_version")
    private Integer interestsVersion; // 관심사 버전 (수정 횟수 추적)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== 관심사 관리 메서드들 =====

    /**
     * 초기 관심사 설정 (회원가입 후)
     */
    public void setInitialInterests(QuestionCategory.MajorCategory majorCategory, 
                                   List<QuestionCategory.MinorCategory> minorCategories) {
        this.learningPurpose = LearningPurpose.fromMajorCategory(majorCategory);
        this.selectedMinorCategories = minorCategories;
        this.interestsLastUpdated = LocalDateTime.now();
        this.interestsVersion = 1;
    }

    /**
     * 관심사 수정
     */
    public void updateInterests(QuestionCategory.MajorCategory majorCategory,
                               List<QuestionCategory.MinorCategory> minorCategories) {
        this.learningPurpose = LearningPurpose.fromMajorCategory(majorCategory);
        this.selectedMinorCategories = minorCategories;
        this.interestsLastUpdated = LocalDateTime.now();
        this.interestsVersion = (this.interestsVersion != null) ? this.interestsVersion + 1 : 1;
    }

    /**
     * 현재 관심사 기반 문제 분류 조회
     */
    public QuestionCategory.MajorCategory getCurrentMajorCategory() {
        return this.learningPurpose.toMajorCategory();
    }

    /**
     * 관심사가 설정되었는지 확인
     */
    public boolean hasInterestsSet() {
        return this.learningPurpose != null && 
               this.selectedMinorCategories != null && 
               !this.selectedMinorCategories.isEmpty();
    }

    /**
     * 관심사 수정 가능 여부 확인 (최소 1개 소분류)
     */
    public boolean canUpdateInterests(List<QuestionCategory.MinorCategory> newMinorCategories) {
        return newMinorCategories != null && !newMinorCategories.isEmpty();
    }

    // 학습 목적 열거형 (대분류와 일치)
    public enum LearningPurpose {
        STUDY("학업"),           // QuestionCategory.MajorCategory.STUDY
        BUSINESS("비즈니스"),    // QuestionCategory.MajorCategory.BUSINESS  
        TRAVEL("여행"),         // QuestionCategory.MajorCategory.TRAVEL
        DAILY_LIFE("일상생활"); // QuestionCategory.MajorCategory.DAILY_LIFE

        private final String displayName;

        LearningPurpose(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * LearningPurpose를 QuestionCategory.MajorCategory로 변환
         */
        public QuestionCategory.MajorCategory toMajorCategory() {
            switch (this) {
                case STUDY:
                    return QuestionCategory.MajorCategory.STUDY;
                case BUSINESS:
                    return QuestionCategory.MajorCategory.BUSINESS;
                case TRAVEL:
                    return QuestionCategory.MajorCategory.TRAVEL;
                case DAILY_LIFE:
                    return QuestionCategory.MajorCategory.DAILY_LIFE;
                default:
                    throw new IllegalArgumentException("Unknown LearningPurpose: " + this);
            }
        }

        /**
         * QuestionCategory.MajorCategory를 LearningPurpose로 변환
         */
        public static LearningPurpose fromMajorCategory(QuestionCategory.MajorCategory majorCategory) {
            switch (majorCategory) {
                case STUDY:
                    return STUDY;
                case BUSINESS:
                    return BUSINESS;
                case TRAVEL:
                    return TRAVEL;
                case DAILY_LIFE:
                    return DAILY_LIFE;
                default:
                    throw new IllegalArgumentException("Unknown MajorCategory: " + majorCategory);
            }
        }
    }

    // 난이도 열거형
    public enum DifficultyLevel {
        BEGINNER,     // 초급
        INTERMEDIATE, // 중급
        ADVANCED,     // 고급
        EXPERT        // 전문가
    }

    // 학습 스타일 열거형
    public enum LearningStyle {
        VISUAL,       // 시각적 학습자
        AUDITORY,     // 청각적 학습자
        KINESTHETIC,  // 체감적 학습자
        READING,      // 읽기 중심 학습자
        MIXED         // 혼합형
    }
}
