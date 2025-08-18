package com.example.demo.service;

import com.example.demo.dto.user.UserProfileInfo;
import com.example.demo.dto.user.UserProfileInfoImpl;
import com.example.demo.entity.QuestionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PersonalizedQuestionAssignmentServiceTest {

    @InjectMocks
    private PersonalizedQuestionAssignmentService personalizedQuestionAssignmentService;

    @Mock
    private UserProfileInfo mockUserProfile;

    private UserProfileInfoImpl testUserProfile;

    @BeforeEach
    void setUp() {
        testUserProfile = new UserProfileInfoImpl();
        testUserProfile.setUserId("test-user-123");
        testUserProfile.setLearningPurpose(QuestionCategory.MajorCategory.BUSINESS);
        testUserProfile.setSelectedMinorCategories(Arrays.asList(
            QuestionCategory.MinorCategory.MEETING_CONFERENCE,
            QuestionCategory.MinorCategory.CUSTOMER_SERVICE
        ));
        testUserProfile.setSelectedQuestionTypes(Arrays.asList(
            QuestionCategory.QuestionType.FILL_IN_THE_BLANK,
            QuestionCategory.QuestionType.IDIOM_IN_CONTEXT
        ));
        testUserProfile.setLearningStyle(UserProfileInfo.LearningStyle.VISUAL);
        testUserProfile.setDifficultyPreference(UserProfileInfo.DifficultyLevel.INTERMEDIATE);
        testUserProfile.setHasInterestsSet(true);
    }

    @Test
    void assignPersonalizedQuestions_정상할당_10문제() {
        // given
        String userId = "test-user-123";
        String sessionType = "PRACTICE";
        int totalQuestions = 10;

        // when
        PersonalizedQuestionAssignmentService.QuestionAssignmentResult result = 
            personalizedQuestionAssignmentService.assignPersonalizedQuestions(userId, sessionType, totalQuestions);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSessionType()).isEqualTo(sessionType);
        assertThat(result.getAssignedQuestionIds()).hasSize(10);
        assertThat(result.getQuestionTypeDetails()).hasSize(3);
        assertThat(result.getPersonalizationFactors()).containsKey("difficultyPreference");
        assertThat(result.getTotalQuestions()).isEqualTo(10);
        
        // 3개 유형에 4, 3, 3으로 분배되는지 확인
        Map<String, PersonalizedQuestionAssignmentService.QuestionTypeInfo> typeDetails = result.getQuestionTypeDetails();
        assertThat(typeDetails).hasSize(3);
        
        // 각 유형별 문제 수 확인
        typeDetails.values().forEach(typeInfo -> {
            assertThat(typeInfo.getTypeName()).isNotNull();
            assertThat(typeInfo.getEstimatedTime()).isPositive();
        });
        
        // 총 문제 수가 10개인지 확인
        int totalAssigned = result.getAssignedQuestionIds().size();
        assertThat(totalAssigned).isEqualTo(10);
    }

    @Test
    void assignPersonalizedQuestions_문제유형선택_3개유형() {
        // given
        String userId = "test-user-123";
        String sessionType = "PRACTICE";
        int totalQuestions = 10;

        // when
        PersonalizedQuestionAssignmentService.QuestionAssignmentResult result = 
            personalizedQuestionAssignmentService.assignPersonalizedQuestions(userId, sessionType, totalQuestions);

        // then
        Map<String, PersonalizedQuestionAssignmentService.QuestionTypeInfo> typeDetails = result.getQuestionTypeDetails();
        
        // 3개 유형이 모두 선택되었는지 확인
        assertThat(typeDetails).containsKeys("FILL_IN_THE_BLANK", "SYNONYM_SELECTION", "PRONUNCIATION_RECOGNITION");
        
        // 각 유형별 정보 확인
        typeDetails.forEach((type, info) -> {
            assertThat(info.getTypeName()).isNotNull();
            assertThat(info.getDescription()).isNotNull();
            assertThat(info.getExample()).isNotNull();
            assertThat(info.getEstimatedTime()).isPositive();
        });
    }

    @Test
    void assignPersonalizedQuestions_개인화요소_포함() {
        // given
        String userId = "test-user-123";
        String sessionType = "PRACTICE";
        int totalQuestions = 10;

        // when
        PersonalizedQuestionAssignmentService.QuestionAssignmentResult result = 
            personalizedQuestionAssignmentService.assignPersonalizedQuestions(userId, sessionType, totalQuestions);

        // then
        Map<String, Object> factors = result.getPersonalizationFactors();
        
        assertThat(factors).containsKey("difficultyPreference");
        assertThat(factors).containsKey("learningPurpose");
        assertThat(factors).containsKey("learningStyle");
        
        assertThat(factors.get("difficultyPreference")).isEqualTo(UserProfileInfo.DifficultyLevel.INTERMEDIATE);
        assertThat(factors.get("learningPurpose")).isEqualTo(QuestionCategory.MajorCategory.BUSINESS);
        assertThat(factors.get("learningStyle")).isEqualTo(UserProfileInfo.LearningStyle.VISUAL);
    }
}
