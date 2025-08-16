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

        private final String description;

        MajorCategory(String description) {
            this.description = description;
        }
    }

    /**
     * 소분류 (12개 - 대분류별 3개씩)
     */
    @Getter
    public enum MinorCategory {
        // 학업 관련 (3개)
        GRAMMAR("문법"),
        VOCABULARY("어휘"),
        READING("독해"),
        
        // 비즈니스 관련 (3개)
        MEETING("회의"),
        PRESENTATION("프레젠테이션"),
        NEGOTIATION("협상"),
        
        // 여행 관련 (3개)
        BACKPACKING("배낭 여행"),
        FRIEND_TRIP("친구 여행"),
        FAMILY_TRIP("가족 여행"),
        
        // 일상생활 관련 (3개)
        SHOPPING("쇼핑"),
        COOKING("요리"),
        TRANSPORTATION("교통");

        private final String description;

        MinorCategory(String description) {
            this.description = description;
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
                return new MinorCategory[]{MinorCategory.GRAMMAR, MinorCategory.VOCABULARY, MinorCategory.READING};
            case BUSINESS:
                return new MinorCategory[]{MinorCategory.MEETING, MinorCategory.PRESENTATION, MinorCategory.NEGOTIATION};
            case TRAVEL:
                return new MinorCategory[]{MinorCategory.BACKPACKING, MinorCategory.FRIEND_TRIP, MinorCategory.FAMILY_TRIP};
            case DAILY_LIFE:
                return new MinorCategory[]{MinorCategory.SHOPPING, MinorCategory.COOKING, MinorCategory.TRANSPORTATION};
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
     * 대분류별 총 문제 종류 수 계산
     */
    public static int getTotalQuestionTypesByMajor(MajorCategory majorCategory) {
        MinorCategory[] minorCategories = getMinorCategoriesByMajor(majorCategory);
        QuestionType[] questionTypes = QuestionType.values();
        return minorCategories.length * questionTypes.length; // 3 × 3 = 9
    }

    /**
     * 전체 문제 종류 수 계산
     */
    public static int getTotalQuestionTypes() {
        return MajorCategory.values().length * 9; // 4 × 9 = 36
    }
}
