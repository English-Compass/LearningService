package com.example.demo.dto.user;

import com.example.demo.entity.QuestionCategory;

import java.util.List;

/**
 * 사용자 프로필 정보 인터페이스
 * MSA 전환 시 UserService와의 통신에 사용
 */
public interface UserProfileInfo {
    
    /**
     * 사용자 ID
     */
    String getUserId();
    
    /**
     * 학습 목적 (대분류)
     */
    QuestionCategory.MajorCategory getLearningPurpose();
    
    /**
     * 선택된 소분류들
     */
    List<QuestionCategory.MinorCategory> getSelectedMinorCategories();
    
    /**
     * 학습 스타일
     */
    LearningStyle getLearningStyle();
    
    /**
     * 선호 난이도
     */
    DifficultyLevel getDifficultyPreference();
    
    /**
     * 관심사가 설정되었는지 확인
     */
    boolean hasInterestsSet();
    
    // 학습 스타일 열거형
    enum LearningStyle {
        VISUAL,       // 시각적 학습자
        AUDITORY,     // 청각적 학습자
        KINESTHETIC,  // 체감적 학습자
        READING,      // 읽기 중심 학습자
        MIXED         // 혼합형
    }
    
    // 난이도 열거형
    enum DifficultyLevel {
        BEGINNER,     // 초급
        INTERMEDIATE, // 중급
        ADVANCED,     // 고급
        EXPERT        // 전문가
    }
}
