package com.example.demo.service;

import com.example.demo.dto.user.UserProfileInfo;
import com.example.demo.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 개인화된 문제 할당 서비스
 * 사용자의 관심사와 학습 목적에 맞춰 문제를 할당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalizedQuestionAssignmentService {

    private final UserServiceClient userServiceClient;

    /**
     * 사용자별 개인화된 문제 할당
     * 한 세션에 3개 유형의 문제를 랜덤으로 할당
     */
    public QuestionAssignmentResult assignPersonalizedQuestions(String userId, String sessionType, int totalQuestions) {
        try {
            log.info("개인화된 문제 할당 시작: userId={}, sessionType={}, totalQuestions={}", 
                userId, sessionType, totalQuestions);

            // 1. 사용자 프로필 조회
            UserProfileInfo userProfile = getUserProfile(userId);
            
            // 2. 사용자 맞춤형 문제 유형 결정
            List<String> selectedQuestionTypes = selectQuestionTypes(userProfile, 3);
            
            // 3. 각 유형별 문제 수 계산 (10문제를 3개 유형에 분배)
            Map<String, Integer> questionsPerType = calculateQuestionsPerType(selectedQuestionTypes, totalQuestions);
            
            // 4. 개인화된 문제 할당
            List<String> assignedQuestionIds = assignQuestionsByType(userProfile, questionsPerType);
            
            QuestionAssignmentResult result = QuestionAssignmentResult.builder()
                .userId(userId)
                .sessionType(sessionType)
                .selectedQuestionTypes(selectedQuestionTypes)
                .questionsPerType(questionsPerType)
                .assignedQuestionIds(assignedQuestionIds)
                .personalizationFactors(extractPersonalizationFactors(userProfile))
                .questionTypeDetails(getQuestionTypeInfo()) // 문제 유형별 상세 정보 추가
                .totalQuestions(totalQuestions) // 총 문제 수 설정
                .build();

            log.info("개인화된 문제 할당 완료: userId={}, types={}, totalAssigned={}", 
                userId, selectedQuestionTypes, assignedQuestionIds.size());

            return result;

        } catch (Exception e) {
            log.error("개인화된 문제 할당 실패: userId={}", userId, e);
            throw new RuntimeException("문제 할당 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 프로필 조회
     */
    private UserProfileInfo getUserProfile(String userId) {
        return userServiceClient.getUserProfile(userId);
    }

    /**
     * 사용자 맞춤형 문제 유형 선택 (3개)
     */
    private List<String> selectQuestionTypes(UserProfileInfo userProfile, int typeCount) {
        // 3가지 고정된 문제 유형
        List<String> availableTypes = List.of(
            "FILL_IN_THE_BLANK",      // 빈칸 채우기
            "SYNONYM_SELECTION",      // 동의어/유사 표현 선택
            "PRONUNCIATION_RECOGNITION" // 발음 인식 및 텍스트 변환
        );
        
        // 사용자 프로필에 맞는 우선순위 부여
        List<String> prioritizedTypes = prioritizeQuestionTypes(availableTypes, userProfile);
        
        // 상위 3개 유형 선택 (실제로는 항상 3개)
        return prioritizedTypes.stream()
            .limit(typeCount)
            .collect(Collectors.toList());
    }

    /**
     * 사용 가능한 문제 유형 목록
     */
    private List<String> getAvailableQuestionTypes(UserProfileInfo userProfile) {
        // 3가지 고정된 문제 유형
        return List.of(
            "FILL_IN_THE_BLANK",      // 빈칸 채우기
            "SYNONYM_SELECTION",      // 동의어/유사 표현 선택  
            "PRONUNCIATION_RECOGNITION" // 발음 인식 및 텍스트 변환
        );
    }

    /**
     * 문제 유형 우선순위 결정
     */
    private List<String> prioritizeQuestionTypes(List<String> availableTypes, UserProfileInfo userProfile) {
        // 사용자 관심사와 비즈니스 도메인에 따른 우선순위 계산
        Map<String, Integer> typeScores = new HashMap<>();
        
        for (String type : availableTypes) {
            int score = calculateTypeScore(type, userProfile);
            typeScores.put(type, score);
        }

        // 점수 순으로 정렬
        return typeScores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * 문제 유형별 점수 계산
     */
    private int calculateTypeScore(String questionType, UserProfileInfo userProfile) {
        int score = 0;

        // 문제 유형별 기본 점수 (난이도 기반)
        switch (questionType) {
            case "FILL_IN_THE_BLANK":
                score += 3; // BEGINNER - 기본 점수
                break;
            case "SYNONYM_SELECTION":
                score += 5; // INTERMEDIATE - 중간 점수
                break;
            case "PRONUNCIATION_RECOGNITION":
                score += 7; // ADVANCED - 높은 점수
                break;
        }

        // 학습 스타일은 대시보드 표시용이므로 문제 할당 점수 계산에서 제외
        // 문제 할당은 사용자 관심사와 난이도 선호도만 고려

        return score;
    }

    /**
     * 유형별 문제 수 계산 (10문제를 3개 유형에 분배)
     */
    private Map<String, Integer> calculateQuestionsPerType(List<String> selectedTypes, int totalQuestions) {
        Map<String, Integer> questionsPerType = new HashMap<>();
        
        if (selectedTypes.isEmpty()) {
            return questionsPerType;
        }

        // 10문제를 3개 유형에 분배: 4, 3, 3
        if (selectedTypes.size() == 3) {
            questionsPerType.put(selectedTypes.get(0), 4);  // 첫 번째 유형: 4문제
            questionsPerType.put(selectedTypes.get(1), 3);  // 두 번째 유형: 3문제
            questionsPerType.put(selectedTypes.get(2), 3);  // 세 번째 유형: 3문제
        } else {
            // 기본 분배 (균등하게)
            int baseQuestions = totalQuestions / selectedTypes.size();
            int remainder = totalQuestions % selectedTypes.size();

            for (int i = 0; i < selectedTypes.size(); i++) {
                String type = selectedTypes.get(i);
                int questions = baseQuestions + (i < remainder ? 1 : 0);
                questionsPerType.put(type, questions);
            }
        }

        return questionsPerType;
    }

    /**
     * 유형별로 문제 할당
     */
    private List<String> assignQuestionsByType(UserProfileInfo userProfile, Map<String, Integer> questionsPerType) {
        List<String> assignedQuestionIds = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : questionsPerType.entrySet()) {
            String questionType = entry.getKey();
            int questionCount = entry.getValue();

            // TODO: QuestionRepository에서 해당 유형의 문제 조회
            List<String> typeQuestions = getQuestionsByType(questionType, questionCount, userProfile);
            assignedQuestionIds.addAll(typeQuestions);
        }

        // 문제 순서 랜덤화
        Collections.shuffle(assignedQuestionIds);
        
        return assignedQuestionIds;
    }

    /**
     * 특정 유형의 문제 조회
     */
    private List<String> getQuestionsByType(String questionType, int count, UserProfileInfo userProfile) {
        // TODO: 실제 구현 시 QuestionRepository 사용
        // 임시로 가상의 문제 ID 생성
        List<String> questionIds = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            questionIds.add(questionType.toLowerCase() + "_" + i);
        }
        return questionIds;
    }

    /**
     * 개인화 요소 추출
     */
    private Map<String, Object> extractPersonalizationFactors(UserProfileInfo userProfile) {
        Map<String, Object> factors = new HashMap<>();
        factors.put("learningPurpose", userProfile.getLearningPurpose());
        factors.put("businessDomain", "IT"); // 더미 데이터
        factors.put("languageGoal", "비즈니스 영어"); // 더미 데이터
        factors.put("difficultyPreference", userProfile.getDifficultyPreference());
        factors.put("learningStyle", userProfile.getLearningStyle());
        return factors;
    }

    /**
     * 문제 유형별 상세 정보
     */
    private Map<String, QuestionTypeInfo> getQuestionTypeInfo() {
        Map<String, QuestionTypeInfo> typeInfo = new HashMap<>();
        
        typeInfo.put("FILL_IN_THE_BLANK", QuestionTypeInfo.builder()
            .typeName("빈칸 채우기")
            .description("문장에 빈칸을 뚫어서 들어갈 올바른 단어/구문을 선택하는 문제")
            .example("나는 ___ 학교에 다닙니다. (보기: a, an, the, -)")
            .estimatedTime(30) // 초
            .build());
            
        typeInfo.put("SYNONYM_SELECTION", QuestionTypeInfo.builder()
            .typeName("동의어/유사 표현 선택")
            .description("주어진 문장과 같은 의미인 문장을 보기에서 고르는 문제")
            .example("I am very tired. (밑줄: very tired) → exhausted")
            .estimatedTime(45) // 초
            .build());
            
        typeInfo.put("PRONUNCIATION_RECOGNITION", QuestionTypeInfo.builder()
            .typeName("발음 인식 및 텍스트 변환")
            .description("문장을 읽고 발음하여 음성 인식으로 텍스트 변환 후 정답 확인")
            .example("I love studying English. → 발음 → 음성 인식 → 텍스트 비교")
            .estimatedTime(60) // 초
            .build());
            
        return typeInfo;
    }

    // ===== DTO 클래스들 =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTypeInfo {
        private String typeName;        // 유형 이름 (한글)
        private String description;     // 유형 설명
        private String example;         // 예시 문제
        private Integer estimatedTime;  // 예상 소요 시간 (초)
        // TODO: 문제 난이도는 추후 다른 서비스에서 REST API로 조회
        // private String difficulty;      // 난이도 (EASY, MEDIUM, HARD)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAssignmentResult {
        private String userId;
        private String sessionType;
        private List<String> selectedQuestionTypes; // 선택된 3개 문제 유형
        private Map<String, Integer> questionsPerType; // 유형별 문제 수
        private List<String> assignedQuestionIds; // 할당된 문제 ID 목록
        private Map<String, Object> personalizationFactors; // 개인화 요소들
        private Map<String, QuestionTypeInfo> questionTypeDetails; // 문제 유형별 상세 정보
        private int totalQuestions; // 총 문제 수
    }
}
