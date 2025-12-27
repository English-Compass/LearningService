package com.example.demo.client;

import com.example.demo.dto.problem.SessionDataResponseDto;

/**
 * ProblemService API 호출을 위한 클라이언트 인터페이스
 * LearningService가 세션 데이터를 조회하기 위해 사용
 */
public interface ProblemServiceClient {
    
    /**
     * 세션 상세 데이터 조회
     * ProblemService의 내부 API를 호출하여 세션 정보, 문제 답변 기록, 세션 이벤트를 조회
     * 
     * @param sessionId 조회할 세션 ID
     * @param userId 사용자 ID (보안 검증용)
     * @return 세션 데이터 (세션 정보, 문제 답변 기록, 세션 이벤트)
     * @throws RuntimeException 세션을 찾을 수 없거나 userId가 일치하지 않는 경우
     */
    SessionDataResponseDto getSessionData(String sessionId, String userId);
}

