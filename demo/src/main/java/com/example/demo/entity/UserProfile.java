package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 프로필 엔티티
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
    private LearningPurpose learningPurpose; // 학습 목적

    @Column(name = "business_domain")
    private String businessDomain; // 비즈니스 도메인 (IT, 금융, 의료 등)

    @Column(name = "language_goal")
    private String languageGoal; // 언어 학습 목표 (비즈니스 영어, 일상 회화 등)

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

    // 학습 목적 열거형
    public enum LearningPurpose {
        BUSINESS_LANGUAGE,    // 비즈니스 언어
        ACADEMIC_STUDY,      // 학술 연구
        DAILY_CONVERSATION,  // 일상 회화
        EXAM_PREPARATION,    // 시험 준비
        CAREER_DEVELOPMENT,  // 경력 개발
        PERSONAL_INTEREST    // 개인 관심사
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
