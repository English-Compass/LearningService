package com.example.demo.repository;

import com.example.demo.entity.LearningPatternAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 학습 패턴 분석 결과 레포지토리
 */
@Repository
public interface LearningPatternAnalysisRepository extends JpaRepository<LearningPatternAnalysis, String> {

    /**
     * 사용자 ID로 분석 결과 조회 (최신순)
     */
    List<LearningPatternAnalysis> findByUserIdOrderByAnalyzedAtDesc(String userId);

    /**
     * 사용자 ID로 분석 결과 조회 (페이징)
     */
    Page<LearningPatternAnalysis> findByUserIdOrderByAnalyzedAtDesc(String userId, Pageable pageable);

    /**
     * 세션 ID로 분석 결과 조회
     */
    Optional<LearningPatternAnalysis> findBySessionId(String sessionId);

    /**
     * 사용자 ID와 분석 타입으로 조회
     */
    List<LearningPatternAnalysis> findByUserIdAndAnalysisTypeOrderByAnalyzedAtDesc(String userId, String analysisType);

    /**
     * 특정 기간 동안의 분석 결과 조회
     */
    @Query("SELECT lpa FROM LearningPatternAnalysis lpa WHERE lpa.userId = :userId AND lpa.analyzedAt BETWEEN :startDate AND :endDate ORDER BY lpa.analyzedAt DESC")
    List<LearningPatternAnalysis> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 사용자별 최근 분석 결과 조회 (최대 10개)
     */
    @Query("SELECT lpa FROM LearningPatternAnalysis lpa WHERE lpa.userId = :userId ORDER BY lpa.analyzedAt DESC")
    List<LearningPatternAnalysis> findTop10ByUserIdOrderByAnalyzedAtDesc(@Param("userId") String userId);

    /**
     * 특정 세션의 분석 결과가 존재하는지 확인
     */
    boolean existsBySessionId(String sessionId);

    /**
     * 사용자별 분석 결과 개수 조회
     */
    long countByUserId(String userId);

    /**
     * 특정 기간 동안의 분석 결과 개수 조회
     */
    @Query("SELECT COUNT(lpa) FROM LearningPatternAnalysis lpa WHERE lpa.userId = :userId AND lpa.analyzedAt BETWEEN :startDate AND :endDate")
    long countByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 세션에 대한 학습 패턴 분석 결과 조회
     */
    Optional<LearningPatternAnalysis> findByUserIdAndSessionId(String userId, String sessionId);

    /**
     * 사용자의 최근 학습 패턴 분석 결과 조회
     */
    @Query("SELECT lpa FROM LearningPatternAnalysis lpa WHERE lpa.userId = :userId ORDER BY lpa.analyzedAt DESC")
    List<LearningPatternAnalysis> findRecentByUserId(@Param("userId") String userId);
}
