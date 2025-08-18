package com.example.demo.entity;

import lombok.Getter;

/**
 * 문제 분류를 위한 열거형 클래스들
 */
public class QuestionCategory {

    /**
     * 대분류 (4개)
     */
    @Getter
    public enum MajorCategory {
        STUDY("학업"),
        BUSINESS("비즈니스"),
        TRAVEL("여행"),
        DAILY_LIFE("일상생활");

        private final String displayName;

        MajorCategory(String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * 소분류 (12개 - 대분류별 3개씩)
     */
    @Getter
    public enum MinorCategory {
        // 학업 관련 (3개)
        CLASS_LISTENING("수업 듣기"),
        DEPARTMENT_CONVERSATION("학과 대화"),
        ASSIGNMENT_EXAM("과제/시험 준비"),
        
        // 비즈니스 관련 (3개)
        MEETING_CONFERENCE("미팅/회의"),
        CUSTOMER_SERVICE("고객 응대"),
        EMAIL_REPORT("이메일/보고서"),
        
        // 여행 관련 (3개)
        BACKPACKING("배낭 여행"),
        FAMILY_TRIP("가족 여행"),
        FRIEND_TRIP("친구와 여행"),
        
        // 일상생활 관련 (3개)
        SHOPPING_DINING("쇼핑/외식"),
        HOSPITAL_VISIT("병원 이용"),
        PUBLIC_TRANSPORT("대중교통 이용");

        private final String displayName;

        MinorCategory(String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * 문제 유형 (3개)
     */
    @Getter
    public enum QuestionType {
        FILL_IN_THE_BLANK("빈칸 채우기"),
        IDIOM_IN_CONTEXT("문장 속 특정 숙어"),
        SENTENCE_COMPLETION("문장 완성");

        private final String description;

        QuestionType(String description) {
            this.description = description;
        }
        
        public String getDisplayName() {
            return this.description;
        }
    }

    /**
     * 보기(선택지) 옵션 (3개)
     */
    @Getter
    public enum Option {
        A("A"),
        B("B"),
        C("C");

        private final String value;

        Option(String value) {
            this.value = value;
        }

        public static Option fromString(String value) {
            for (Option option : Option.values()) {
                if (option.getValue().equalsIgnoreCase(value)) {
                    return option;
                }
            }
            throw new IllegalArgumentException("Invalid option: " + value);
        }
    }

    /**
     * 대분류별 소분류 매핑
     */
    public static MinorCategory[] getMinorCategoriesByMajor(MajorCategory majorCategory) {
        switch (majorCategory) {
            case STUDY:
                return new MinorCategory[]{MinorCategory.CLASS_LISTENING, MinorCategory.DEPARTMENT_CONVERSATION, MinorCategory.ASSIGNMENT_EXAM};
            case BUSINESS:
                return new MinorCategory[]{MinorCategory.MEETING_CONFERENCE, MinorCategory.CUSTOMER_SERVICE, MinorCategory.EMAIL_REPORT};
            case TRAVEL:
                return new MinorCategory[]{MinorCategory.BACKPACKING, MinorCategory.FAMILY_TRIP, MinorCategory.FRIEND_TRIP};
            case DAILY_LIFE:
                return new MinorCategory[]{MinorCategory.SHOPPING_DINING, MinorCategory.HOSPITAL_VISIT, MinorCategory.PUBLIC_TRANSPORT};
            default:
                return new MinorCategory[0];
        }
    }

    /**
     * 소분류별 문제 유형 매핑
     */
    public static QuestionType[] getQuestionTypesByMinor(MinorCategory minorCategory) {
        // 모든 소분류에서 동일한 3가지 문제 유형 사용
        return new QuestionType[]{
            QuestionType.FILL_IN_THE_BLANK,
            QuestionType.IDIOM_IN_CONTEXT,
            QuestionType.SENTENCE_COMPLETION
        };
    }

    /**
     * 대분류별 총 문제 종류 수 계산 (모든 소분류 포함)
     */
    public static int getTotalQuestionTypesByMajor(MajorCategory majorCategory) {
        MinorCategory[] minorCategories = getMinorCategoriesByMajor(majorCategory);
        QuestionType[] questionTypes = QuestionType.values();
        return minorCategories.length * questionTypes.length; // 3 × 3 = 9
    }

    /**
     * 선택된 소분류 수에 따른 문제 종류 수 계산
     * @param majorCategory 대분류
     * @param selectedMinorCount 선택된 소분류 수 (1~3)
     * @return 선택된 소분류 수 × 3개 문제유형
     */
    public static int getTotalQuestionTypesByMajor(MajorCategory majorCategory, int selectedMinorCount) {
        if (selectedMinorCount < 1 || selectedMinorCount > 3) {
            throw new IllegalArgumentException("선택된 소분류 수는 1~3개여야 합니다. 현재: " + selectedMinorCount);
        }
        QuestionType[] questionTypes = QuestionType.values();
        return selectedMinorCount * questionTypes.length; // 선택된 소분류 수 × 3
    }

    /**
     * 전체 문제 종류 수 계산
     */
    public static int getTotalQuestionTypes() {
        return MajorCategory.values().length * 9; // 4 × 9 = 36
    }
}
