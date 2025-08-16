package com.example.demo.repository;

import com.example.demo.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
    
    // 세션 ID로 모든 답변 조회
    List<QuestionAnswer> findBySessionIdOrderByAnsweredAtAsc(String sessionId);
    
    // 세션 ID로 모든 답변 조회 (최신순)
    List<QuestionAnswer> findBySessionIdOrderByAnsweredAtDesc(String sessionId);
    
    // 사용자 ID로 답변 목록 조회 (세션 ID를 통해 조회)
    @Query("SELECT qa FROM QuestionAnswer qa JOIN LearningSession ls ON qa.sessionId = ls.sessionId WHERE ls.userId = :userId ORDER BY qa.answeredAt DESC")
    List<QuestionAnswer> findByUserIdOrderByAnsweredAtDesc(@Param("userId") String userId);
    
    // 특정 문제 ID에 대한 사용자의 답변 조회
    @Query("SELECT qa FROM QuestionAnswer qa JOIN LearningSession ls ON qa.sessionId = ls.sessionId WHERE qa.questionId = :questionId AND ls.userId = :userId ORDER BY qa.answeredAt DESC")
    List<QuestionAnswer> findByQuestionIdAndUserIdOrderByAnsweredAtDesc(@Param("questionId") String questionId, @Param("userId") String userId);
    
    // 정답/오답별 답변 조회
    List<QuestionAnswer> findBySessionIdAndIsCorrectOrderByAnsweredAtAsc(String sessionId, Boolean isCorrect);
    
    // 태그별 답변 조회
    @Query("SELECT qa FROM QuestionAnswer qa JOIN LearningSession ls ON qa.sessionId = ls.sessionId WHERE ls.userId = :userId AND qa.tags LIKE %:tag%")
    List<QuestionAnswer> findByUserIdAndTag(@Param("userId") String userId, @Param("tag") String tag);
    
    // 난이도별 답변 조회
    @Query("SELECT qa FROM QuestionAnswer qa JOIN LearningSession ls ON qa.sessionId = ls.sessionId WHERE ls.userId = :userId AND qa.difficulty = :difficulty ORDER BY qa.answeredAt DESC")
    List<QuestionAnswer> findByUserIdAndDifficultyOrderByAnsweredAtDesc(@Param("userId") String userId, @Param("difficulty") Integer difficulty);
    
    // 사용자별 오답 문제 조회
    @Query("SELECT qa FROM QuestionAnswer qa JOIN LearningSession ls ON qa.sessionId = ls.sessionId WHERE ls.userId = :userId AND qa.isCorrect = false ORDER BY qa.answeredAt DESC")
    List<QuestionAnswer> findWrongAnswersByUserId(@Param("userId") String userId);
    
    // 사용자별 정답 문제 조회
    @Query("SELECT qa FROM QuestionAnswer qa JOIN LearningSession ls ON qa.sessionId = ls.sessionId WHERE ls.userId = :userId AND qa.isCorrect = true ORDER BY qa.answeredAt DESC")
    List<QuestionAnswer> findCorrectAnswersByUserId(@Param("userId") String userId);
}
