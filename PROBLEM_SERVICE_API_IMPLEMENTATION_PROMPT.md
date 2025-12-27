# ProblemService API 구현 프롬프트

## 📝 AI 프롬프트 (자연어)

다음 요구사항에 따라 ProblemService에 REST API 엔드포인트를 구현해주세요.

### 1. API 엔드포인트 명세

**엔드포인트**: `GET /api/problem/internal/sessions/{sessionId}?userId={userId}`

**목적**: LearningService가 학습 세션 완료 이벤트를 받았을 때, 해당 세션의 상세 데이터(세션 정보, 문제 답변 기록, 세션 이벤트)를 조회하기 위한 내부 API입니다.

**요청 파라미터**:
- `sessionId` (Path Variable): 조회할 세션 ID (필수)
- `userId` (Query Parameter): 사용자 ID (필수, 보안 검증용)

**응답 형식**: JSON (하이브리드 구조)

---

### 2. 응답 데이터 구조

응답은 다음 3가지 섹션으로 구성되어야 합니다:

#### 2.1 세션 메타데이터 (session)
다음 필드들을 포함하는 세션 객체를 반환해야 합니다:
- `sessionId` (String): 세션 ID
- `userId` (String): 사용자 ID (요청 파라미터의 userId와 일치해야 함)
- `sessionType` (Enum): 세션 타입 - PRACTICE, REVIEW, WRONG_ANSWER 중 하나
- `status` (Enum): 세션 상태 - STARTED, IN_PROGRESS, COMPLETED 중 하나
- `startedAt` (LocalDateTime): 세션 시작 시각 (ISO 8601 형식)
- `completedAt` (LocalDateTime): 세션 완료 시각 (ISO 8601 형식, 완료되지 않은 경우 null)
- `createdAt` (LocalDateTime): 세션 생성 시각
- `updatedAt` (LocalDateTime): 세션 수정 시각
- `metadata` (Map<String, Object>): 확장 필드 (기기 정보, 모드 등, 선택 사항)

#### 2.2 문항 + 사용자 답변 데이터 (questions[])
해당 세션에 포함된 모든 문제와 사용자의 답변 기록을 배열로 반환해야 합니다. 각 문제 객체는 다음 필드를 포함해야 합니다:
- `questionId` (String): 문제 ID
- `questionType` (String/Enum): 문제 유형 (예: VOCABULARY, GRAMMAR, READING 등)
- `majorCategory` (String/Enum): 대분류 (예: READING, LISTENING 등)
- `minorCategory` (String/Enum): 소분류 (예: WORD_USAGE, SENTENCE_STRUCTURE 등)
- `difficultyLevel` (Integer): 난이도 (1~3 등급 또는 숫자)
- `userAnswer` (String): 사용자가 선택한 답안 (보기 번호 또는 텍스트)
- `isCorrect` (Boolean): 정답 여부 (이미 계산된 값, LearningService는 재계산하지 않음)
- `timeSpent` (Integer): 풀이 시간 (초 단위)
- `answeredAt` (LocalDateTime): 답안 제출 시각 (ISO 8601 형식)
- `solveCount` (Integer): 해당 문제를 풀이한 누적 횟수
- `metadata` (Map<String, Object>): 확장 필드 (보기 텍스트, 해설 등, 선택 사항)

**정렬**: `answeredAt` 기준 오름차순 (시간 순서대로)

#### 2.3 세션 이벤트 로그 (events[]) - 선택 사항
세션 진행 중 발생한 이벤트들을 배열로 반환합니다. 필수는 아니지만, 가능하면 포함해주세요. 각 이벤트 객체는 다음 필드를 포함합니다:
- `eventId` (String): 이벤트 ID
- `eventType` (String): 이벤트 타입 (예: SESSION_COMPLETED, QUESTION_SKIPPED, QUESTION_ANSWERED 등)
- `sessionId` (String): 세션 ID
- `userId` (String): 사용자 ID
- `createdAt` (LocalDateTime): 이벤트 발생 시각 (ISO 8601 형식)
- `metadata` (Map<String, Object>): 확장 필드 (선택 사항)

---

### 3. 구현 요구사항

#### 3.1 컨트롤러 구현
- Spring Boot `@RestController` 사용
- 경로: `/api/problem/internal/sessions/{sessionId}`
- HTTP 메서드: GET
- `@GetMapping` 어노테이션 사용
- Path Variable로 `sessionId` 받기
- Query Parameter로 `userId` 받기
- 응답 타입: ResponseEntity로 래핑하여 반환

#### 3.2 서비스 로직 구현
서비스 클래스에서 다음 로직을 수행해야 합니다:

1. **세션 조회 및 검증**:
   - `sessionId`로 세션 엔티티 조회
   - 세션이 존재하지 않으면 404 에러 반환
   - 세션의 `userId`가 요청 파라미터의 `userId`와 일치하는지 검증
   - 일치하지 않으면 404 에러 반환 (보안상 다른 사용자의 세션 정보를 노출하지 않기 위함)

2. **문제 답변 기록 조회**:
   - 해당 세션에 속한 모든 문제 답변 기록 조회
   - `QuestionAnswer` 엔티티와 `Question` 엔티티를 JOIN하여 문제 정보 포함
   - `answeredAt` 기준 오름차순 정렬
   - 각 답변 기록에 다음 정보 포함:
     - 문제 기본 정보 (questionId, questionType, majorCategory, minorCategory, difficultyLevel)
     - 사용자 답변 정보 (userAnswer, isCorrect, timeSpent, answeredAt, solveCount)

3. **세션 이벤트 조회** (선택 사항):
   - 해당 세션에 발생한 모든 이벤트 조회
   - `createdAt` 기준 정렬 (선택 사항)

4. **응답 DTO 생성**:
   - 세션 정보, 문제 답변 배열, 이벤트 배열을 포함하는 응답 DTO 생성
   - 엔티티를 DTO로 변환 (필요한 필드만 포함)

#### 3.3 에러 처리
- 세션을 찾을 수 없는 경우: HTTP 404 (NOT_FOUND)
- userId가 일치하지 않는 경우: HTTP 404 (NOT_FOUND) - 보안상 존재하지 않는 것처럼 처리
- 서버 내부 오류: HTTP 500 (INTERNAL_SERVER_ERROR)
- 잘못된 요청 파라미터: HTTP 400 (BAD_REQUEST)

#### 3.4 보안 고려사항
- 이 API는 내부 서비스 간 통신용이므로 인증/인가 로직은 선택 사항
- 다만, `userId` 검증은 반드시 수행하여 다른 사용자의 세션 정보가 노출되지 않도록 해야 함
- API 경로에 `/internal/`이 포함되어 있으므로, 외부 노출을 방지하기 위한 네트워크 레벨 보안 설정 권장

---

### 4. 응답 예시

#### 성공 응답 (200 OK)
```json
{
  "session": {
    "sessionId": "S-20241124-001",
    "userId": "USER-123",
    "sessionType": "PRACTICE",
    "status": "COMPLETED",
    "startedAt": "2025-11-24T10:00:00",
    "completedAt": "2025-11-24T10:25:30",
    "createdAt": "2025-11-24T09:55:00",
    "updatedAt": "2025-11-24T10:25:30",
    "metadata": {
      "device": "mobile",
      "mode": "practice"
    }
  },
  "questions": [
    {
      "questionId": "Q-10001",
      "questionType": "VOCABULARY",
      "majorCategory": "READING",
      "minorCategory": "WORD_USAGE",
      "difficultyLevel": 2,
      "userAnswer": "B",
      "isCorrect": true,
      "timeSpent": 42,
      "answeredAt": "2025-11-24T10:05:12",
      "solveCount": 1,
      "metadata": {
        "options": ["A", "B", "C", "D"],
        "explanation": "..."
      }
    },
    {
      "questionId": "Q-10002",
      "questionType": "GRAMMAR",
      "majorCategory": "READING",
      "minorCategory": "SENTENCE_STRUCTURE",
      "difficultyLevel": 3,
      "userAnswer": "A",
      "isCorrect": false,
      "timeSpent": 65,
      "answeredAt": "2025-11-24T10:06:30",
      "solveCount": 2,
      "metadata": {}
    }
  ],
  "events": [
    {
      "eventId": "EVT-001",
      "eventType": "QUESTION_SKIPPED",
      "sessionId": "S-20241124-001",
      "userId": "USER-123",
      "createdAt": "2025-11-24T10:03:00",
      "metadata": {
        "questionId": "Q-10005"
      }
    },
    {
      "eventId": "EVT-002",
      "eventType": "SESSION_COMPLETED",
      "sessionId": "S-20241124-001",
      "userId": "USER-123",
      "createdAt": "2025-11-24T10:25:30",
      "metadata": {}
    }
  ]
}
```

#### 에러 응답 (404 NOT_FOUND)
```json
{
  "error": "Session not found",
  "message": "Session with id S-20241124-001 not found or access denied",
  "status": 404,
  "timestamp": "2025-11-24T10:30:00"
}
```

---

### 5. 데이터베이스 조회 최적화

다음과 같은 조회 방식을 권장합니다:

1. **세션 조회**: `SessionRepository.findById(sessionId)` 사용
2. **문제 답변 조회**: 
   - `QuestionAnswerRepository`에서 `sessionId`로 조회
   - `Question` 엔티티와 JOIN하여 문제 정보 포함
   - `answeredAt` 기준 정렬
   - N+1 쿼리 문제를 방지하기 위해 `@EntityGraph` 또는 `JOIN FETCH` 사용 권장
3. **세션 이벤트 조회**: `SessionEventRepository.findBySessionId(sessionId)` 사용 (선택 사항)

---

### 6. 구현 체크리스트

구현 시 다음 사항들을 확인해주세요:

- [ ] GET `/api/problem/internal/sessions/{sessionId}?userId={userId}` 엔드포인트 구현
- [ ] 세션 존재 여부 검증
- [ ] userId 일치 여부 검증 (보안)
- [ ] 문제 답변 기록 조회 (Question과 JOIN)
- [ ] `answeredAt` 기준 오름차순 정렬
- [ ] 세션 이벤트 조회 (선택 사항)
- [ ] 엔티티 → DTO 변환
- [ ] 에러 처리 (404, 500 등)
- [ ] 응답 JSON 구조 검증
- [ ] 모든 필수 필드 포함 확인

---

### 7. 추가 참고사항

- LearningService는 이 API의 응답을 받아서 `LearningSession`, `QuestionAnswer`, `LearningSessionEvent` 엔티티로 매핑하여 분석 로직을 수행합니다.
- 따라서 응답 스키마는 LearningService의 엔티티 구조와 호환되어야 합니다.
- `isCorrect`와 `timeSpent`는 이미 계산된 값으로 전달해야 하며, LearningService는 재계산하지 않습니다.
- `events` 배열은 선택 사항이지만, 가능하면 포함해주시면 증분 분석이나 리포트 생성에 유용합니다.

---

## 🎯 요약

ProblemService에 `GET /api/problem/internal/sessions/{sessionId}?userId={userId}` 엔드포인트를 구현하여, 세션 정보, 문제 답변 기록, 세션 이벤트를 하이브리드 구조의 JSON으로 반환하도록 해주세요. userId 검증을 통해 보안을 보장하고, 에러 처리를 포함하여 구현해주세요.

