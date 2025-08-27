package com.example.demo.repository;

import com.example.demo.entity.Question;
import com.example.demo.entity.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 문제 Repository
 * 문제 정보를 관리
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    /**
     * 문제 유형으로 문제 목록 조회
     */
    List<Question> findByQuestionType(QuestionCategory.QuestionType questionType);

    /**
     * 대분류로 문제 목록 조회
     */
    List<Question> findByMajorCategory(QuestionCategory.MajorCategory majorCategory);

    /**
     * 소분류로 문제 목록 조회
     */
    List<Question> findByMinorCategory(QuestionCategory.MinorCategory minorCategory);

    /**
     * 난이도로 문제 목록 조회
     */
    List<Question> findByDifficultyLevel(Integer difficultyLevel);

    /**
     * 문제 유형과 난이도로 문제 목록 조회
     */
    List<Question> findByQuestionTypeAndDifficultyLevel(QuestionCategory.QuestionType questionType, 
                                                       Integer difficultyLevel);

    /**
     * 문제 ID로 문제 조회 (Optional 반환)
     */
    Optional<Question> findByQuestionId(String questionId);

    /**
     * 문제 유형별 문제 개수 조회
     */
    @Query("SELECT q.questionType, COUNT(q) FROM Question q GROUP BY q.questionType")
    List<Object[]> countByQuestionType();

    /**
     * 난이도별 문제 개수 조회
     */
    @Query("SELECT q.difficultyLevel, COUNT(q) FROM Question q GROUP BY q.difficultyLevel ORDER BY q.difficultyLevel")
    List<Object[]> countByDifficultyLevel();
}
