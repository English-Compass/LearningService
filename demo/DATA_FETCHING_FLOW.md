# 분석 데이터 조회 로직 흐름

## 📋 개요

`learning-session-completed` Kafka 이벤트를 수신하면, 분석에 필요한 데이터를 조회하고 학습 패턴 분석을 수행합니다.

---

## 🔄 전체 흐름도

```
Kafka 메시지 수신
    ↓
LearningSessionCompletedKafkaConsumer.consumeLearningSessionCompleted()
    ↓
LearningSessionEventListener.handleLearningSessionCompleted()
    ↓
[데이터 조회 단계]
    ├─ 1. 세션 기본 정보 조회
    ├─ 2. 세션 이벤트 히스토리 조회
    └─ 3. 문제 답변 기록 조회
    ↓
[데이터 통합 단계]
    └─ LearningSessionResult 객체 생성
    ↓
[분석 수행 단계]
    ├─ 개별 세션 분석
    └─ 전체 학습 분석 (30일)
    ↓
[결과 저장 단계]
    ├─ 개별 세션 분석 결과 저장
    └─ 전체 학습 분석 결과 저장
    ↓
[이벤트 발행 단계]
    └─ 분석 완료 이벤트 발행
```

---

## 📊 데이터 조회 상세 흐름

### 1️⃣ 개별 세션 분석용 데이터 조회

**위치**: `LearningSessionEventListener.handleLearningSessionCompleted()`

#### Step 1: 세션 기본 정보 조회
```java
// 파일: LearningSessionEventListener.java (59-61줄)
LearningSession session = sessionRepository.findById(sessionId)
    .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
```

**조회 데이터**:
- `sessionId`: 세션 ID
- `userId`: 사용자 ID
- `sessionType`: 세션 타입 (PRACTICE/REVIEW/WRONG_ANSWER)
- `status`: 세션 상태 (STARTED/IN_PROGRESS/COMPLETED)
- `startedAt`: 세션 시작 시간
- `completedAt`: 세션 완료 시간
- `metadata`: 확장 메타데이터

**Repository**: `LearningSessionRepository.findById()`

---

#### Step 2: 세션 이벤트 히스토리 조회
```java
// 파일: LearningSessionEventListener.java (63-67줄)
List<LearningSessionEvent> sessionEvents = eventRepository
    .findBySessionId(sessionId)
    .map(List::of)
    .orElse(List.of());
```

**조회 데이터**:
- `eventId`: 이벤트 ID
- `eventType`: 이벤트 타입 (SESSION_COMPLETED, QUESTION_SKIPPED 등)
- `sessionId`: 세션 ID
- `userId`: 사용자 ID
- `createdAt`: 이벤트 발생 시간
- `metadata`: 확장 메타데이터

**Repository**: `LearningSessionEventRepository.findBySessionId()`

**참고**: 현재는 Optional을 List로 변환하여 사용 (실제로는 단일 이벤트만 반환)

---

#### Step 3: 문제 답변 기록 조회
```java
// 파일: LearningSessionEventListener.java (69-71줄)
List<QuestionAnswer> questionAnswers = answerRepository
    .findBySessionIdOrderByAnsweredAtAsc(sessionId);
```

**조회 데이터** (각 QuestionAnswer):
- `questionId`: 문제 ID
- `sessionId`: 세션 ID
- `userAnswer`: 사용자가 선택한 답안
- `isCorrect`: 정답 여부 (boolean)
- `timeSpent`: 풀이 시간 (초 단위)
- `answeredAt`: 답안 제출 시간
- `questionType`: 문제 유형 (Question 엔티티와 JOIN 필요)
- `majorCategory`: 대분류 (Question 엔티티와 JOIN 필요)
- `minorCategory`: 소분류 (Question 엔티티와 JOIN 필요)
- `difficultyLevel`: 난이도 (Question 엔티티와 JOIN 필요)

**Repository**: `QuestionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc()`

**정렬**: `answeredAt` 기준 오름차순 (시간 순서대로)

---

#### Step 4: 세션 결과 객체 생성
```java
// 파일: LearningSessionEventListener.java (73-74줄)
LearningSessionResult sessionResult = buildSessionResult(session, sessionEvents, questionAnswers);
```

**생성되는 통계 데이터**:
- `totalQuestions`: 전체 문제 수 (questionAnswers.size())
- `correctAnswers`: 정답 수 (isCorrect == true인 개수)
- `totalDuration`: 총 학습 시간 (모든 timeSpent의 합, 초 단위)

**구현 코드** (115-138줄):
```java
private LearningSessionResult buildSessionResult(LearningSession session, 
                                               List<LearningSessionEvent> sessionEvents, 
                                               List<QuestionAnswer> questionAnswers) {
    int totalQuestions = questionAnswers.size();
    int correctAnswers = (int) questionAnswers.stream()
        .filter(QuestionAnswer::getIsCorrect).count();
    
    long totalTimeSpent = questionAnswers.stream()
        .filter(answer -> answer.getTimeSpent() != null)
        .mapToLong(QuestionAnswer::getTimeSpent)
        .sum();
    
    return LearningSessionResult.builder()
        .sessionId(session.getSessionId())
        .userId(session.getUserId())
        .totalQuestions(totalQuestions)
        .correctAnswers(correctAnswers)
        .totalDuration(totalTimeSpent)
        .questionAnswers(questionAnswers)
        .build();
}
```

---

### 2️⃣ 전체 학습 분석용 데이터 조회

**위치**: `LearningPatternAnalysisService.analyzeCompleteLearningIncremental()`

#### Step 1: 기간별 세션 목록 조회
```java
// 파일: LearningPatternAnalysisService.java (91-92줄)
List<LearningSession> sessions = learningSessionRepository
    .findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
```

**조회 조건**:
- `userId`: 사용자 ID
- `startDate`: 시작 날짜 (현재 - 30일)
- `endDate`: 종료 날짜 (현재)

**정렬**: `createdAt` 기준 내림차순 (최신순)

---

#### Step 2: 각 세션별 문제 답변 기록 조회
```java
// 파일: LearningPatternAnalysisService.java (96-100줄)
List<QuestionAnswer> allAnswers = new ArrayList<>();
for (LearningSession session : sessions) {
    List<QuestionAnswer> sessionAnswers = questionAnswerRepository
        .findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
    allAnswers.addAll(sessionAnswers);
}
```

**조회 방식**:
- 각 세션별로 순회하며 `QuestionAnswer` 조회
- 모든 세션의 답변 기록을 하나의 리스트로 통합

**성능 고려사항**:
- 세션 수가 많을 경우 N+1 쿼리 문제 발생 가능
- 향후 개선: 배치 조회 또는 JOIN 쿼리로 최적화 가능

---

### 3️⃣ 문제 유형별 성과 분석용 데이터 조회

**위치**: `LearningPatternAnalysisService.analyzeQuestionTypePerformanceCommon()`

#### 개별 세션 분석 시
```java
// 파일: LearningPatternAnalysisService.java (152줄)
typeAnswers = questionAnswerRepository.findBySessionIdAndQuestionType(sessionId, type.name());
```

**Repository 메서드**: `QuestionAnswerRepository.findBySessionIdAndQuestionType()`

**쿼리 구조** (188-189줄):
```sql
SELECT qa FROM QuestionAnswer qa 
JOIN Question q ON qa.questionId = q.questionId 
WHERE qa.sessionId = :sessionId AND q.questionType = :questionType
```

---

#### 전체 학습 분석 시
```java
// 파일: LearningPatternAnalysisService.java (156-163줄)
List<LearningSession> userSessions = learningSessionRepository
    .findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
for (LearningSession session : userSessions) {
    List<QuestionAnswer> sessionAnswers = questionAnswerRepository
        .findBySessionIdAndQuestionType(session.getSessionId(), type.name());
    typeAnswers.addAll(sessionAnswers);
}
```

**조회 방식**:
1. 기간별 세션 목록 조회
2. 각 세션별로 문제 유형별 답변 기록 조회
3. 모든 결과를 하나의 리스트로 통합

---

## 🗄️ 데이터베이스 스키마 관계

```
LearningSession (1) ──< (N) QuestionAnswer
    │
    └── (1) ──< (N) LearningSessionEvent
```

**주요 관계**:
- `QuestionAnswer.sessionId` → `LearningSession.sessionId` (외래키)
- `LearningSessionEvent.sessionId` → `LearningSession.sessionId` (외래키)
- `QuestionAnswer.questionId` → `Question.questionId` (JOIN 필요)

---

## 📝 데이터 조회 메서드 요약

### LearningSessionRepository
| 메서드 | 용도 | 사용 위치 |
|--------|------|-----------|
| `findById(sessionId)` | 개별 세션 조회 | `LearningSessionEventListener` |
| `findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc()` | 기간별 세션 목록 조회 | `LearningPatternAnalysisService` |

### QuestionAnswerRepository
| 메서드 | 용도 | 사용 위치 |
|--------|------|-----------|
| `findBySessionIdOrderByAnsweredAtAsc()` | 세션별 답변 기록 조회 | `LearningSessionEventListener`, `LearningPatternAnalysisService` |
| `findBySessionIdAndQuestionType()` | 세션별 문제 유형별 답변 조회 | `LearningPatternAnalysisService` |

### LearningSessionEventRepository
| 메서드 | 용도 | 사용 위치 |
|--------|------|-----------|
| `findBySessionId()` | 세션별 이벤트 조회 | `LearningSessionEventListener` |

---

## ⚠️ 현재 구현의 특징 및 제한사항

### ✅ 현재 구현 방식
- **로컬 데이터베이스 직접 조회**: LearningService의 자체 데이터베이스에서 조회
- **JPA Repository 사용**: Spring Data JPA를 통한 타입 안전한 쿼리
- **트랜잭션 관리**: `@Transactional`을 통한 일관성 보장

### ⚠️ 가이드 문서와의 차이점
**ANALYSIS_SERVICE_GUIDE.md**에는 다음과 같이 명시되어 있습니다:
> LearningService는 **Kafka 이벤트**를 통해 `sessionId`, `userId`를 전달받은 뒤, **ProblemService REST API를 호출**해 실제 세션/문항 데이터를 조회한 다음 분석을 수행한다.

**현재 구현**:
- ProblemService REST API 호출 없이 로컬 DB에서 직접 조회
- 데이터가 이미 LearningService DB에 저장되어 있다고 가정

**향후 개선 방향**:
1. ProblemService REST API 클라이언트 추가
2. Kafka 이벤트 수신 시 ProblemService에서 데이터 조회
3. 조회한 데이터를 LearningService DB에 저장 (선택적)
4. 저장된 데이터로 분석 수행

---

## 🔍 성능 최적화 포인트

### 1. N+1 쿼리 문제
**현재**: 각 세션별로 QuestionAnswer를 개별 조회
```java
for (LearningSession session : sessions) {
    List<QuestionAnswer> sessionAnswers = questionAnswerRepository
        .findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
    allAnswers.addAll(sessionAnswers);
}
```

**개선 방안**: 배치 조회 또는 IN 절 사용
```java
List<String> sessionIds = sessions.stream()
    .map(LearningSession::getSessionId)
    .collect(Collectors.toList());
List<QuestionAnswer> allAnswers = questionAnswerRepository
    .findBySessionIdInOrderByAnsweredAtAsc(sessionIds);
```

### 2. 문제 유형별 조회 최적화
**현재**: 각 문제 유형별로 세션을 순회하며 조회
```java
for (LearningSession session : userSessions) {
    List<QuestionAnswer> sessionAnswers = questionAnswerRepository
        .findBySessionIdAndQuestionType(session.getSessionId(), type.name());
    typeAnswers.addAll(sessionAnswers);
}
```

**개선 방안**: 한 번의 쿼리로 모든 문제 유형별 데이터 조회
```java
// Repository에 추가
@Query("SELECT qa FROM QuestionAnswer qa " +
       "JOIN Question q ON qa.questionId = q.questionId " +
       "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
       "WHERE ls.userId = :userId " +
       "AND ls.startedAt BETWEEN :startDate AND :endDate " +
       "AND q.questionType = :questionType")
List<QuestionAnswer> findByUserIdAndDateRangeAndQuestionType(
    @Param("userId") String userId,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate,
    @Param("questionType") String questionType);
```

---

## 📌 핵심 포인트

1. **데이터 소스**: 현재는 로컬 데이터베이스에서 직접 조회
2. **조회 순서**: 세션 정보 → 이벤트 히스토리 → 문제 답변 기록
3. **통합 방식**: 조회한 데이터를 `LearningSessionResult` 객체로 통합
4. **분석 범위**: 개별 세션 분석 + 전체 학습 분석 (30일) 동시 수행
5. **성능 이슈**: N+1 쿼리 문제 존재, 향후 최적화 필요

---

## 🔗 관련 파일

- **이벤트 리스너**: `LearningSessionEventListener.java`
- **분석 서비스**: `LearningPatternAnalysisService.java`
- **Kafka 컨슈머**: `LearningSessionCompletedKafkaConsumer.java`
- **Repository**:
  - `LearningSessionRepository.java`
  - `QuestionAnswerRepository.java`
  - `LearningSessionEventRepository.java`
- **가이드 문서**: `ANALYSIS_SERVICE_GUIDE.md`

