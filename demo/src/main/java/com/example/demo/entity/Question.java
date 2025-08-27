package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    private String questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText; // 문제 내용

    @Column(nullable = false)
    private String optionA; // 선택지 A

    @Column(nullable = false)
    private Integer difficultyLevel = 1; // 난이도 (1: 초급, 2: 중급, 3: 상급)

    @Column(nullable = false)
    private String optionB; // 선택지 B

    @Column(nullable = false)
    private String optionC; // 선택지 C

    @Column(nullable = false)
    private String correctAnswer; // 정답

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionCategory.MajorCategory majorCategory; // 대분류

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionCategory.MinorCategory minorCategory; // 소분류

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionCategory.QuestionType questionType; // 문제 유형

    @Column(columnDefinition = "TEXT")
    private String explanation; // 해설

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ===== 연관 관계 제거 (성능 및 매핑 충돌 방지) =====
    // 필요시 Repository를 통해 개별 조회
    // @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<SessionQuestion> sessionQuestions;

    // @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<QuestionAnswer> questionAnswers;

    // ===== 편의 메서드들 =====

    /**
     * 문제 난이도 레벨 반환
     */
    public String getDifficultyLevel() {
        switch (this.difficultyLevel) {
            case 1:
                return "초급";
            case 2:
                return "중급";
            case 3:
                return "상급";
            default:
                return "알 수 없음";
        }
    }

    /**
     * 문제 카테고리 정보 반환
     */
    public String getCategoryInfo() {
        return String.format("%s > %s > %s", 
            this.majorCategory.getDisplayName(),
            this.minorCategory.getDisplayName(),
            this.questionType.getDisplayName());
    }

    /**
     * 문제가 특정 카테고리에 속하는지 확인
     */
    public boolean belongsToCategory(QuestionCategory.MajorCategory major, 
                                   QuestionCategory.MinorCategory minor) {
        return this.majorCategory == major && this.minorCategory == minor;
    }

    /**
     * 문제가 특정 유형인지 확인
     */
    public boolean isQuestionType(QuestionCategory.QuestionType type) {
        return this.questionType == type;
    }

    /**
     * 문제가 특정 난이도인지 확인
     */
    public boolean isDifficulty(int difficulty) {
        return this.difficultyLevel == difficulty;
    }

    /**
     * 문제가 특정 난이도 범위에 속하는지 확인
     */
    public boolean isDifficultyRange(int minDifficulty, int maxDifficulty) {
        return this.difficultyLevel >= minDifficulty && this.difficultyLevel <= maxDifficulty;
    }


    /**
     * 문제 생성 시간으로부터 경과 시간 계산 (일)
     */
    public long getDaysSinceCreation() {
        if (this.createdAt == null) {
            return 0;
        }
        return java.time.Duration.between(this.createdAt, LocalDateTime.now()).toDays();
    }

    /**
     * 문제가 최근에 생성되었는지 확인 (지정된 일수 이내)
     */
    public boolean isRecentlyCreated(int days) {
        return getDaysSinceCreation() <= days;
    }

    // ===== JPA 생명주기 메서드 =====

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
