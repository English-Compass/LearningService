# MSA 전환 가이드

## 개요
현재 모놀리식 구조의 LearningService를 MSA(Microservice Architecture)로 전환하기 위한 가이드입니다.

## 서비스 분리 구조

### 1. UserService (사용자 관리 서비스)
- **책임**: 사용자 인증, 권한, 프로필, 관심사 관리
- **주요 엔티티**: `UserProfile`, `User`, `UserRole`
- **API 엔드포인트**: `/api/users/*`
- **포트**: 8081

### 2. LearningHistoryService (학습 이력 서비스)
- **책임**: 학습 세션, 문제 풀이 기록, 성과 분석
- **주요 엔티티**: `LearningSession`, `QuestionAnswer`, `LearningAnalysis`
- **API 엔드포인트**: `/api/learning/*`
- **포트**: 8082

## 현재 상태

### ✅ 완료된 작업
1. **UserProfile 분리**: `UserProfile`을 `user` 패키지로 이동
2. **인터페이스 설계**: `UserProfileInfo` 인터페이스 생성
3. **Feign Client**: `UserServiceClient` 생성
4. **의존성 제거**: LearningHistoryService에서 UserProfile 직접 참조 제거

### 🔄 다음 단계
1. **UserService 모듈 생성**: 별도 프로젝트로 분리
2. **API 구현**: UserService에 REST API 구현
3. **데이터베이스 분리**: 각 서비스별 독립적인 DB
4. **서비스 디스커버리**: Eureka/Consul 설정
5. **API Gateway**: Spring Cloud Gateway 설정

## 서비스 간 통신

### REST API 호출
```java
// LearningHistoryService에서 UserService 호출
@Autowired
private UserServiceClient userServiceClient;

public LearningAnalysis analyzeLearning(String userId) {
    // UserService에서 사용자 정보 조회
    UserProfileInfo userProfile = userServiceClient.getUserProfile(userId);
    
    // 학습 데이터 분석
    return learningAnalysisService.analyze(userId, userProfile);
}
```

### Circuit Breaker 패턴
```java
@CircuitBreaker(name = "userServiceClient")
public UserProfileInfo getUserProfile(String userId) {
    return userServiceClient.getUserProfile(userId);
}
```

## 설정 파일

### application-msa.properties
- UserService API 설정
- Feign Client 설정
- Circuit Breaker 설정
- 서비스 디스커버리 설정

## 배포 구조

### 현재 (모놀리식)
```
LearningService (단일 서버:8080)
├── UserProfile
├── LearningSession
└── QuestionAnswer
```

### 향후 (MSA)
```
UserService (서버:8081)
├── UserProfile
├── User
└── UserRole

LearningHistoryService (서버:8082)
├── LearningSession
├── QuestionAnswer
└── LearningAnalysis

API Gateway (서버:8080)
└── 라우팅 및 인증

Eureka Server (서버:8761)
└── 서비스 디스커버리
```

## 마이그레이션 체크리스트

- [ ] UserService 모듈 생성
- [ ] UserProfile 관련 코드 이동
- [ ] REST API 구현
- [ ] 데이터베이스 분리
- [ ] 서비스 디스커버리 설정
- [ ] API Gateway 설정
- [ ] 테스트 및 검증
- [ ] 배포 및 모니터링

## 주의사항

1. **데이터 일관성**: 서비스 간 데이터 동기화 고려
2. **장애 처리**: Circuit Breaker, Retry, Fallback 구현
3. **보안**: 서비스 간 인증/권한 관리
4. **모니터링**: 각 서비스별 성능 및 상태 모니터링
5. **로깅**: 분산 추적을 위한 로깅 전략 수립
