package com.example.demo.service;

import com.example.demo.entity.LearningSession;
import com.example.demo.entity.QuestionCategory;
import com.example.demo.repository.LearningSessionRepository;
import com.example.demo.repository.QuestionAnswerRepository;
import com.example.demo.service.PersonalizedQuestionAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * LearningSessionService 단위 테스트
 * 사용자 프로필을 받아서 세션을 생성하는 기능을 테스트
 */
@ExtendWith(MockitoExtension.class)
class LearningSessionServiceTest {

    @Mock
    private LearningSessionRepository sessionRepository;

    @Mock
    private QuestionAnswerRepository questionAnswerRepository;

    @InjectMocks
    private LearningSessionService learningSessionService;

    private PersonalizedQuestionAssignmentService.QuestionAssignmentResult mockAssignment;
    private LearningSession mockSession;

    @BeforeEach
    void setUp() {
        // Mock 문제 할당 결과 생성
        mockAssignment = PersonalizedQuestionAssignmentService.QuestionAssignmentResult.builder()
            .userId("user123")
            .sessionType("PRACTICE")
            .selectedQuestionTypes(Arrays.asList("FILL_IN_THE_BLANK", "SYNONYM_SELECTION", "PRONUNCIATION_RECOGNITION"))
            .questionsPerType(java.util.Map.of(
                "FILL_IN_THE_BLANK", 4,
                "SYNONYM_SELECTION", 3,
                "PRONUNCIATION_RECOGNITION", 3
            ))
            .assignedQuestionIds(Arrays.asList("q1", "q2", "q3", "q4", "q5", "q6", "q7", "q8", "q9", "q10"))
            .personalizationFactors(java.util.Map.of("learningStyle", "AUDITORY", "difficulty", "INTERMEDIATE"))
            .questionTypeDetails(java.util.Map.of("FILL_IN_THE_BLANK", 
                PersonalizedQuestionAssignmentService.QuestionTypeInfo.builder()
                    .typeName("빈칸 채우기")
                    .description("문장에 빈칸을 뚫어서 들어갈 올바른 단어/구문을 선택하는 문제")
                    .example("나는 ___ 학교에 다닙니다.")
                    .difficulty("BEGINNER")
                    .estimatedTime(30)
                    .build()))
            .build();

        // Mock 학습 세션 생성
        mockSession = LearningSession.builder()
            .sessionId("session123")
            .userId("user123")
            .learningItemId("PRACTICE")
            .sessionType(LearningSession.SessionType.PRACTICE)
            .startedAt(LocalDateTime.now())
            .lastUpdatedAt(LocalDateTime.now())
            .status(LearningSession.SessionStatus.STARTED)
            .totalQuestions(10)
            .answeredQuestions(0)
            .correctAnswers(0)
            .wrongAnswers(0)
            .build();
    }

    @Test
    @DisplayName("사용자 프로필 기반으로 학습 세션을 성공적으로 생성해야 함")
    void startLearningSession_사용자프로필기반_세션생성_성공() {
        // given
        String userId = "user123";
        String sessionType = "PRACTICE";
        
        when(sessionRepository.save(any(LearningSession.class)))
            .thenAnswer(invocation -> {
                LearningSession session = invocation.getArgument(0);
                session.setSessionId("session123");
                return session;
            });

        // when
        LearningSession result = learningSessionService.startLearningSession(userId, sessionType, mockAssignment);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo("session123");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getLearningItemId()).isEqualTo(sessionType);
        assertThat(result.getSessionType()).isEqualTo(LearningSession.SessionType.PRACTICE);
        assertThat(result.getStatus()).isEqualTo(LearningSession.SessionStatus.STARTED);
        assertThat(result.getTotalQuestions()).isEqualTo(10);
        assertThat(result.getAnsweredQuestions()).isEqualTo(0);
        assertThat(result.getCorrectAnswers()).isEqualTo(0);
        assertThat(result.getWrongAnswers()).isEqualTo(0);
        assertThat(result.getStartedAt()).isNotNull();
        assertThat(result.getLastUpdatedAt()).isNotNull();

        // verify
        verify(sessionRepository).save(any(LearningSession.class));
    }

    @Test
    @DisplayName("세션 생성 시 할당된 문제 수가 정확히 반영되어야 함")
    void startLearningSession_할당된문제수_정확반영() {
        // given
        String userId = "user123";
        String sessionType = "PRACTICE";
        
        // 문제 수가 다른 할당 결과
        PersonalizedQuestionAssignmentService.QuestionAssignmentResult customAssignment = 
            PersonalizedQuestionAssignmentService.QuestionAssignmentResult.builder()
                .userId(userId)
                .sessionType(sessionType)
                .selectedQuestionTypes(Arrays.asList("FILL_IN_THE_BLANK"))
                .questionsPerType(java.util.Map.of("FILL_IN_THE_BLANK", 5))
                .assignedQuestionIds(Arrays.asList("q1", "q2", "q3", "q4", "q5"))
                .personalizationFactors(java.util.Map.of())
                .questionTypeDetails(java.util.Map.of())
                .build();

        when(sessionRepository.save(any(LearningSession.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        LearningSession result = learningSessionService.startLearningSession(userId, sessionType, customAssignment);

        // then
        assertThat(result.getTotalQuestions()).isEqualTo(5);
        assertThat(result.getTotalQuestions()).isEqualTo(customAssignment.getAssignedQuestionIds().size());
    }

    @Test
    @DisplayName("세션 ID가 UUID 형태로 생성되어야 함")
    void startLearningSession_세션ID_UUID_생성() {
        // given
        when(sessionRepository.save(any(LearningSession.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        LearningSession result = learningSessionService.startLearningSession("user123", "PRACTICE", mockAssignment);

        // then
        assertThat(result.getSessionId()).isNotNull();
        assertThat(result.getSessionId()).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    }

    @Test
    @DisplayName("세션 생성 시 시간 정보가 정확히 설정되어야 함")
    void startLearningSession_시간정보_정확설정() {
        // given
        LocalDateTime beforeTest = LocalDateTime.now();
        
        when(sessionRepository.save(any(LearningSession.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        LearningSession result = learningSessionService.startLearningSession("user123", "PRACTICE", mockAssignment);
        
        LocalDateTime afterTest = LocalDateTime.now();

        // then
        assertThat(result.getStartedAt()).isBetween(beforeTest, afterTest);
        assertThat(result.getLastUpdatedAt()).isBetween(beforeTest, afterTest);
        assertThat(result.getStartedAt()).isEqualTo(result.getLastUpdatedAt());
    }

    @Test
    @DisplayName("세션 조회 시 존재하지 않는 세션 ID로 조회하면 예외가 발생해야 함")
    void getLearningSession_존재하지않는세션_예외발생() {
        // given
        String nonExistentSessionId = "non-existent-session";
        when(sessionRepository.findBySessionId(nonExistentSessionId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> learningSessionService.getLearningSession(nonExistentSessionId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 세션입니다: " + nonExistentSessionId);
    }

    @Test
    @DisplayName("세션 조회 시 정상적으로 세션 정보를 반환해야 함")
    void getLearningSession_정상세션_조회성공() {
        // given
        String sessionId = "session123";
        when(sessionRepository.findBySessionId(sessionId))
            .thenReturn(Optional.of(mockSession));

        // when
        LearningSession result = learningSessionService.getLearningSession(sessionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(sessionId);
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getStatus()).isEqualTo(LearningSession.SessionStatus.STARTED);
    }

    @Test
    @DisplayName("다양한 세션 타입으로 세션을 생성할 수 있어야 함")
    void startLearningSession_다양한세션타입_생성가능() {
        // given
        String[] sessionTypes = {"PRACTICE", "REVIEW", "WRONG_ANSWER"};
        
        when(sessionRepository.save(any(LearningSession.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when & then
        for (String sessionType : sessionTypes) {
            LearningSession result = learningSessionService.startLearningSession("user123", sessionType, mockAssignment);
            assertThat(result.getLearningItemId()).isEqualTo(sessionType);
        }
    }
}
