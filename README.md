# 📚 Learning Analytics Service

> **AI 기반 영어 학습 패턴 분석 및 개인화 추천 서비스**  
> Kafka 이벤트 기반 MSA 아키텍처로 구현된 실시간 학습 데이터 분석 플랫폼

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)](https://www.docker.com/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-black?logo=apachekafka)](https://kafka.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)](https://www.mysql.com/)

---

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [핵심 기능 상세](#-핵심-기능-상세)
- [API 명세](#-api-명세)
- [데이터베이스 설계](#-데이터베이스-설계)
- [설치 및 실행](#-설치-및-실행)
- [CI/CD 파이프라인](#-cicd-파이프라인)
- [성능 최적화](#-성능-최적화)
- [향후 계획](#-향후-계획)

---

## 🎯 프로젝트 개요

**Learning Analytics Service**는 영어 학습자의 학습 패턴을 실시간으로 분석하고, 개인화된 학습 인사이트를 제공하는 MSA 기반 백엔드 서비스입니다.

### 🎪 주요 특징

- **📊 실시간 학습 패턴 분석**: Kafka 이벤트 기반 비동기 분석 처리
- **🤖 AI 기반 개인화**: 사용자별 취약점 분석 및 맞춤형 학습 추천
- **📈 데이터 시각화**: 대시보드용 다양한 분석 데이터 제공
- **⚡ 고성능 처리**: Redis 캐싱 및 DB View 기반 집계
- **🔄 이벤트 기반 아키텍처**: 느슨한 결합의 마이크로서비스 구조
- **📝 체계적인 로깅**: 378+ 로깅 포인트로 전 과정 추적 가능
- **🐳 완전한 컨테이너화**: Docker 기반 배포 및 운영

---

## ✨ 주요 기능

### 1. 📊 학습 세션 분석

- **실시간 분석 처리**
  - Kafka 이벤트 구독 (`learning-session-completed`)
  - ProblemService REST API 호출을 통한 세션 데이터 조회
  - 문제 유형별, 난이도별 정답률 분석
  
- **다층 분석 구조**
  - **개별 세션 분석**: 한 세션의 상세 학습 패턴
  - **종합 학습 분석**: 전체 학습 이력 기반 누적 분석
  - **시계열 분석**: 일별/주별 학습 추이 분석

### 2. 🎯 개인화 학습 인사이트

- **취약점 진단**
  - 문제 유형별 정답률 분석
  - 난이도별 성취도 측정
  - 카테고리별 성능 평가
  
- **학습 패턴 추적**
  - 학습 시간대 분석
  - 문제 풀이 속도 분석
  - 오답 패턴 탐지

### 3. 📈 대시보드 데이터 제공

- **학습 통계 API**
  - 일별 학습 활동 히트맵
  - 주간 학습 추이 그래프
  - 문제 유형별 정답률 차트
  - 취약점 분포 분석
  
- **성과 지표**
  - 총 학습 세션 수
  - 누적 정답률
  - 평균 풀이 시간
  - 학습 진도율

### 4. 🔄 이벤트 기반 통신

- **이벤트 구독**
  - `learning-session-completed`: 학습 완료 이벤트 수신
  
- **이벤트 발행**
  - `learning-analysis-completed`: 분석 완료 이벤트 발행
  - 다른 서비스(ProblemService)에서 분석 결과 조회 가능

---

## 🛠️ 기술 스택

### Backend Framework
- **Java 17** - LTS 버전, Record 및 Pattern Matching 활용
- **Spring Boot 3.5.4** - 최신 Spring 생태계
- **Spring Data JPA** - 선언적 데이터 액세스
- **Spring Kafka** - 이벤트 기반 메시징

### Database & Caching
- **MySQL 8.0** - 메인 데이터베이스
  - Database Views를 활용한 집계 데이터 최적화
  - 복잡한 조인 쿼리 사전 컴파일
- **Redis** - 세션 캐싱 및 실시간 데이터 저장

### Messaging & Events
- **Apache Kafka** - 이벤트 스트리밍 플랫폼
  - 이벤트 기반 마이크로서비스 통신
  - 비동기 분석 처리
  - Consumer Group 기반 확장성

### Infrastructure & DevOps
- **Docker** - 컨테이너 기반 배포
- **GitHub Actions** - CI/CD 자동화
  - 자동 빌드 및 Docker Hub 배포
- **API Gateway (Spring Cloud Gateway)** - 서비스 라우팅 및 인증

### Logging & Health Check
- **Spring Boot Actuator** - 헬스 체크 엔드포인트
- **SLF4J + Logback** - 구조화된 애플리케이션 로깅
  - **378+ 로깅 포인트**로 상세한 디버깅 지원
  - Kafka 이벤트 처리 전 과정 추적
  - API 호출 및 에러 상황별 로깅
  - 6단계 분석 프로세스 실시간 모니터링

---

## 🏗️ 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway                              │
│              (Spring Cloud Gateway + JWT)                        │
└───────────────────────┬─────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   User      │  │  Problem    │  │  Learning   │
│  Service    │  │  Service    │  │  Analytics  │ ◀─ 본 프로젝트
└─────────────┘  └─────────────┘  │  Service    │
                        │          └─────────────┘
                        │                 │
                        │                 │
                  ┌─────▼─────┐    ┌─────▼─────┐
                  │   Kafka   │◀───│  Consumer │
                  │  Cluster  │    │   Group   │
                  └───────────┘    └───────────┘
                        │
                  ┌─────▼─────────────────┐
                  │ learning-session-     │
                  │    completed          │
                  │ learning-analysis-    │
                  │    completed          │
                  └───────────────────────┘
```

### 데이터 흐름

```
1️⃣ 사용자 학습 완료
   └─> ProblemService가 Kafka에 이벤트 발행
   
2️⃣ LearningService가 이벤트 수신
   └─> Consumer Group: learning-service-analysis-group
   
3️⃣ ProblemService REST API 호출
   └─> GET /problem/internal/sessions/{sessionId}?userId={userId}
   └─> 세션 데이터, 문제 정보, 답변 내역 조회
   
4️⃣ 데이터 분석 수행
   ├─> 개별 세션 분석 (SessionAnalysis)
   ├─> 종합 학습 분석 (CompleteAnalysis)
   └─> 분석 결과 DB 저장
   
5️⃣ 분석 완료 이벤트 발행
   └─> Kafka: learning-analysis-completed
   └─> ProblemService가 분석 결과 조회 가능
   
6️⃣ 프론트엔드 대시보드 요청
   └─> API Gateway를 통해 분석 데이터 제공
```

---

## 🔥 핵심 기능 상세

### 1. Kafka 이벤트 기반 분석 처리

```java
@KafkaListener(
    topics = "learning-session-completed",
    groupId = "learning-service-analysis-group"
)
public void consumeSessionCompletedEvent(
    LearningCompletedEvent event,
    Acknowledgment ack
) {
    // 1. ProblemService API 호출로 세션 데이터 조회
    // 2. 데이터 매핑 및 분석
    // 3. 결과 저장 및 이벤트 발행
    // 4. 수동 Offset Commit
}
```

**특징:**
- ✅ Manual Offset Commit으로 정확한 처리 보장
- ✅ ErrorHandlingDeserializer로 안정적인 메시지 처리
- ✅ @Transactional로 원자성 보장

### 2. REST API 기반 서비스 간 통신

```java
@Override
public SessionDataResponseDto getSessionData(String sessionId, String userId) {
    String url = UriComponentsBuilder
        .fromUriString(problemServiceUrl)
        .path("/problem/internal/sessions/{sessionId}")
        .queryParam("userId", userId)
        .buildAndExpand(sessionId)
        .toUriString();
    
    return restTemplate.getForObject(url, SessionDataResponseDto.class);
}
```

**특징:**
- ✅ API Gateway를 통한 내부 서비스 통신
- ✅ RestTemplate 기반 동기 HTTP 통신
- ✅ UriComponentsBuilder를 통한 안전한 URL 구성
- ✅ 상세한 에러 로깅 (403, 404, 5xx 등 상황별 로깅)

### 3. 다층 분석 엔진

#### 📌 개별 세션 분석
```java
SessionAnalysisResult analyzeSession(LearningSession session, List<QuestionAnswer> answers)
```
- 문제 유형별 정답률
- 난이도별 성취도
- 평균 풀이 시간
- 카테고리별 성능

#### 📌 종합 학습 분석
```java
CompleteAnalysisResult analyzeCompletePattern(String userId, LocalDateTime startDate, LocalDateTime endDate)
```
- 전체 학습 이력 기반 패턴 분석
- 취약 영역 식별
- 학습 성장 추이
- 개인화 추천 데이터

### 4. Database View 기반 집계 최적화

```sql
-- 문제 통계 뷰
CREATE OR REPLACE VIEW question_stats_view AS
SELECT
    qa.question_id,
    qa.question_type,
    qa.major_category as category,
    qa.difficulty_level,
    COUNT(*) as total_attempts,
    SUM(CASE WHEN qa.is_correct = 1 THEN 1 ELSE 0 END) as correct_count,
    ROUND(AVG(CASE WHEN qa.is_correct = 1 THEN 100.0 ELSE 0.0 END), 2) as success_rate
FROM question_answer qa
LEFT JOIN learning_sessions ls ON qa.session_id = ls.session_id
GROUP BY qa.question_id, qa.question_type, qa.major_category, qa.difficulty_level;
```

**장점:**
- ✅ 복잡한 집계 쿼리 사전 컴파일
- ✅ API 응답 속도 대폭 향상
- ✅ 데이터베이스 부하 감소

### 5. 체계적인 로깅 시스템

**378+ 로깅 포인트로 전체 프로세스 추적:**

```java
// 6단계 분석 프로세스 로깅 예시
log.info("┌─ [1단계] 📨 Kafka 이벤트 수신");
log.info("   ├─ Topic: learning-session-completed");
log.info("   ├─ SessionId: {}", event.getSessionId());
log.info("   └─ UserId: {}", event.getUserId());

log.info("┌─ [2단계] 🔗 ProblemService API 호출");
log.info("   ├─ URL: {}", apiUrl);
log.info("   └─ Response: 200 OK");

log.info("┌─ [3단계] 🔄 데이터 매핑");
log.info("   ├─ Session 매핑 완료");
log.info("   ├─ QuestionAnswers 매핑: {} 개", answers.size());
log.info("   └─ Events 매핑: {} 개", events.size());

// ... 4, 5, 6단계 로그
```

**특징:**
- ✅ 단계별 구조화된 로그 (Emoji + 들여쓰기)
- ✅ Kafka 메시지 수신부터 이벤트 발행까지 전 과정 추적
- ✅ API 호출 성공/실패 및 상세 에러 정보
- ✅ 분석 결과 통계 (정답률, 평균 시간 등)
- ✅ 트랜잭션 및 DB 저장 상태 모니터링

**로깅 레벨별 활용:**
- `INFO`: 정상 흐름 추적
- `WARN`: 경고 상황 (재시도, 지연 등)
- `ERROR`: 에러 상황 상세 로깅 + 스택 트레이스

---

## 📡 API 명세

### 📊 학습 분석 데이터 API

#### 1️⃣ 일별 학습 활동 조회
```http
GET /analysis/users/{userId}/daily-activity
GET /analysis/users/{userId}/calendar-heatmap

Query Parameters:
- fromDate: 시작 날짜 (yyyy-MM-dd)
- toDate: 종료 날짜 (yyyy-MM-dd)
- year: 연도 (calendar-heatmap용)
- month: 월 (calendar-heatmap용)
```

#### 2️⃣ 주간 학습 추이
```http
GET /analysis/users/{userId}/weekly-trend
GET /analysis/users/{userId}/weekly-stats/recent
GET /analysis/users/{userId}/weekly-graph

Query Parameters:
- weeks: 조회할 주 수 (기본값: 4)
- weekStartDate: 시작 주의 날짜
```

#### 3️⃣ 취약점 분포 분석
```http
GET /analysis/users/{userId}/weakness-distribution
```

#### 4️⃣ 문제 유형별 정답률
```http
GET /analysis/users/{userId}/question-type-accuracy

Query Parameters:
- fromDate: 시작 날짜
- toDate: 종료 날짜
```

#### 5️⃣ 종합 학습 통계
```http
GET /analysis/users/{userId}
```

**Response Example:**
```json
{
  "totalSessions": 45,
  "totalQuestionsSolved": 450,
  "totalCorrectAnswers": 360,
  "accuracyRate": 80.0,
  "errorRate": 20.0,
  "avgSolveTime": 35,
  "retryRate": 15.5,
  "learningProgressRate": 75.0,
  "lastLearningDate": "2025-01-08T14:30:00",
  "totalLearningTimeMinutes": 675
}
```

---

## 🗃️ 데이터베이스 설계

### 핵심 테이블

#### 1. `learning_sessions` - 학습 세션
```sql
- session_id (PK)
- user_id
- session_type (PRACTICE, REVIEW, WRONG_ANSWER)
- status (STARTED, IN_PROGRESS, COMPLETED, ABANDONED)
- total_questions, correct_answers, wrong_answers
- started_at, completed_at
```

#### 2. `question_answer` - 문제 답변
```sql
- id (PK)
- session_id (FK)
- question_id (ProblemService 참조)
- question_type, major_category, minor_category
- difficulty_level
- user_answer, is_correct
- time_spent, answered_at
```

#### 3. `learning_pattern_analysis` - 분석 결과
```sql
- analysis_id (PK)
- user_id
- analysis_type (SESSION, COMPLETE)
- session_id
- start_date, end_date
- analyzed_at
- analysis_result (JSON)
```

### Database Views

- **`question_stats_view`**: 문제별 통계
- **`category_performance_view`**: 카테고리별 성능
- **`difficulty_achievement_view`**: 난이도별 성취도
- **`user_learning_analytics_view`**: 사용자별 종합 분석

---

## 🚀 설치 및 실행

### 요구사항

- Java 17+
- Docker & Docker Compose
- MySQL 8.0+
- Redis
- Apache Kafka

### 로컬 실행

```bash
# 1. 저장소 클론
git clone https://github.com/yourusername/learning-service.git
cd learning-service

# 2. 의존성 설치 및 빌드
./gradlew clean build -x test

# 3. 애플리케이션 실행
./gradlew bootRun
```

### Docker 실행

```bash
# 1. Docker 이미지 빌드
docker build -t learning-service:latest .

# 2. 컨테이너 실행
docker run -p 8083:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/learning_service_db \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9094 \
  learning-service:latest
```

### 환경 변수

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/learning_service_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9094

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# External Services
PROBLEM_SERVICE_URL=http://localhost:8082
```

---

## 🔄 CI/CD 파이프라인

### GitHub Actions Workflow

```yaml
name: Java CI with Gradle & Docker Push

on:
  push:
    branches: [ "main" ]

jobs:
  build-docker:
    - Checkout source code
    - Set up JDK 17
    - Build with Gradle (테스트 제외)
    - Login to Docker Hub
    - Build and push Docker image
```

**자동화된 프로세스:**
1. ✅ `main` 브랜치 Push 시 자동 실행
2. ✅ Java 17 환경 설정
3. ✅ Gradle 빌드 수행
4. ✅ Docker 이미지 생성
5. ✅ Docker Hub 자동 배포

**Docker Hub:**
```bash
docker pull yourusername/learning-service:latest
```

---

## ⚡ 성능 최적화

### 1. Database Optimization
- ✅ **Database Views** 활용으로 복잡한 조인 쿼리 최적화
- ✅ **복합 인덱스** 설계로 쿼리 성능 향상
- ✅ **Batch Insert** 적용으로 데이터 저장 속도 개선

### 2. Caching Strategy
- ✅ **Redis** 기반 세션 캐싱
- ✅ 자주 조회되는 분석 결과 캐싱
- ✅ TTL 기반 캐시 무효화

### 3. Async Processing
- ✅ **Kafka Consumer** 비동기 분석 처리
- ✅ Manual Offset Commit으로 정확성 보장
- ✅ 백프레셔 관리로 안정적인 처리

### 4. Database Connection Pool
- ✅ HikariCP 기본 설정 활용
- ✅ Spring Boot Auto-Configuration 기반 최적화

---

## 🎓 기술적 챌린지 및 해결

### 1. MSA 환경에서의 데이터 정합성
**문제:** 외래 키 제약 조건으로 인한 서비스 간 데이터 동기화 이슈

**해결:**
- UserService와 ProblemService의 데이터를 직접 참조하지 않음
- 필요한 메타데이터만 로컬 DB에 비정규화하여 저장
- 이벤트 기반 데이터 동기화 구현

### 2. Kafka Deserialization 오류
**문제:** 서비스 간 DTO 타입 불일치로 ClassNotFoundException 발생

**해결:**
- `ErrorHandlingDeserializer` 래퍼 적용
- 커스텀 `LearningCompletedEventDeserializer` 구현
- 타입 헤더 무시 설정으로 유연한 역직렬화

### 3. API Gateway 인증/인가 이슈
**문제:** 내부 서비스 통신에서 403 Forbidden 에러

**해결:**
- API Gateway에서 `/problem/internal/**` 경로를 JWT 검증 제외
- `stripPrefix(1)` 설정으로 경로 매핑 최적화
- 헤더 기반 서비스 간 인증 구현

---

## 📊 프로젝트 통계

- **코드 라인 수**: ~8,000+ lines
- **로깅 포인트**: 378+ (13개 파일)
- **API 엔드포인트**: 15+
- **Database Tables**: 5
- **Database Views**: 4
- **Kafka Topics**: 2 (구독 1, 발행 1)
- **Docker Images**: 1

---

## 📝 향후 계획

### Phase 1: 모니터링 & 관측성 강화
- [ ] **Prometheus** - 메트릭 수집 및 시계열 데이터 저장
- [ ] **Grafana** - 실시간 모니터링 대시보드
  - Kafka Consumer Lag 모니터링
  - API 응답 시간 추적
  - JVM 메모리 및 GC 메트릭
- [ ] **ELK Stack** - 중앙 집중식 로그 관리
  - Logstash: 로그 수집 및 파싱
  - Elasticsearch: 로그 저장 및 검색
  - Kibana: 로그 시각화 및 분석
- [ ] **APM (Application Performance Monitoring)** 도구 도입

### Phase 2: 기능 고도화
- [ ] 실시간 학습 추천 알고리즘 개선
- [ ] 머신러닝 기반 취약점 예측
- [ ] WebSocket 기반 실시간 대시보드
- [ ] A/B 테스팅 프레임워크 구축

### Phase 3: 확장성 & 안정성
- [ ] Kubernetes 기반 배포 및 오토스케일링
- [ ] Circuit Breaker 패턴 적용 (Resilience4j)
- [ ] 분산 트레이싱 (Zipkin/Jaeger)
- [ ] Blue-Green 배포 전략

### Phase 4: 추가 기능
- [ ] 학습 그룹 비교 분석
- [ ] 학습 목표 설정 및 추적
- [ ] 게임화 요소 추가 (배지, 레벨업, 리더보드)

---

**사용된 오픈소스:**
- Spring Framework Team
- Apache Kafka Community
- MySQL Development Team

---

