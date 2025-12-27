package com.example.demo.client;

import com.example.demo.dto.problem.SessionDataResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * ProblemService API 호출 구현체
 * RestTemplate을 사용하여 ProblemService의 내부 API를 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemServiceClientImpl implements ProblemServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${problem.service.url:http://localhost:8082}")
    private String problemServiceUrl;
    
    @Value("${problem.service.timeout:5000}")
    private int timeout;
    
    @Override
    public SessionDataResponseDto getSessionData(String sessionId, String userId) {
        try {
            // API Gateway를 통해 내부 API 호출
            // 경로: /api/problem/internal/** → JWT 검증 없이 라우팅됨
            String url = UriComponentsBuilder
                .fromUriString(problemServiceUrl)
                .path("/api/problem/internal/sessions/{sessionId}")
                .queryParam("userId", userId)
                .buildAndExpand(sessionId)
                .toUriString();
            
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📞 ProblemService 내부 API 호출 (JWT 검증 없음)");
            log.info("   URL: {}", url);
            log.info("   sessionId: {}, userId: {}", sessionId, userId);
            log.info("   API Gateway 경로: /api/problem/internal/** → JWT 필터 없음");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            ResponseEntity<SessionDataResponseDto> response = restTemplate.getForEntity(
                url, 
                SessionDataResponseDto.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SessionDataResponseDto body = response.getBody();
                int questionCount = body.getQuestions() != null ? body.getQuestions().size() : 0;
                int eventCount = body.getEvents() != null ? body.getEvents().size() : 0;
                log.info("   ├─ API 응답 수신: 문제 {}개, 이벤트 {}개", questionCount, eventCount);
                return body;
            } else {
                log.warn("ProblemService API 응답이 비어있음: sessionId={}, userId={}, status={}", 
                    sessionId, userId, response.getStatusCode());
                throw new RuntimeException("ProblemService API 응답이 비어있습니다: " + sessionId);
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            log.error("ProblemService에서 세션을 찾을 수 없음: sessionId={}, userId={}, status={}", 
                sessionId, userId, e.getStatusCode(), e);
            throw new RuntimeException("세션을 찾을 수 없습니다: " + sessionId, e);
            
        } catch (HttpClientErrorException e) {
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("❌ ProblemService API 클라이언트 에러");
            log.error("   sessionId={}, userId={}, status={}", sessionId, userId, e.getStatusCode());
            log.error("   상태 텍스트: {}", e.getStatusText());
            log.error("   응답 본문: {}", e.getResponseBodyAsString());
            log.error("   응답 본문 길이: {} bytes", 
                e.getResponseBodyAsString() != null ? e.getResponseBodyAsString().length() : 0);
            
            // 응답 헤더 상세 로깅
            if (e.getResponseHeaders() != null) {
                log.error("   응답 헤더:");
                e.getResponseHeaders().forEach((key, values) -> 
                    log.error("     {}: {}", key, values));
            } else {
                log.error("   응답 헤더: null");
            }
            
            // 원본 바이트 확인 (인코딩 문제 확인용)
            if (e.getResponseBodyAsByteArray() != null && e.getResponseBodyAsByteArray().length > 0) {
                log.error("   응답 본문 (바이트): {} bytes", e.getResponseBodyAsByteArray().length);
                // 처음 200바이트만 출력 (너무 길면 잘림)
                int length = Math.min(200, e.getResponseBodyAsByteArray().length);
                String preview = new String(e.getResponseBodyAsByteArray(), 0, length, 
                    java.nio.charset.StandardCharsets.UTF_8);
                log.error("   응답 본문 미리보기: {}", preview);
            }
            
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("⚠️  문제 해결 방법:");
            log.error("   1. API Gateway 설정 확인: /api/problem/internal/** 경로가 JWT 필터 없이 라우팅되는지 확인");
            log.error("   2. ProblemService 컨트롤러 확인: @PreAuthorize 등 인증 어노테이션 제거 확인");
            log.error("   3. API Gateway 라우트 우선순위 확인: 더 구체적인 경로가 먼저 매칭되는지 확인");
            log.error("   4. curl로 직접 테스트: curl -v http://localhost:8082/api/problem/internal/sessions/{sessionId}?userId={userId}");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            throw new RuntimeException("ProblemService API 호출 실패: " + e.getStatusCode(), e);
            
        } catch (HttpServerErrorException e) {
            log.error("ProblemService API 서버 에러: sessionId={}, userId={}, status={}", 
                sessionId, userId, e.getStatusCode(), e);
            throw new RuntimeException("ProblemService 서버 오류: " + e.getStatusCode(), e);
            
        } catch (RestClientException e) {
            log.error("ProblemService API 통신 에러: sessionId={}, userId={}", 
                sessionId, userId, e);
            throw new RuntimeException("ProblemService API 통신 실패", e);
        }
    }
}

