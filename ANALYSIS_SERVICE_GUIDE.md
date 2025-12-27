# 학습 분석 서비스 – 세션 데이터 요구사항

LearningService는 **Kafka 이벤트**를 통해 `sessionId`, `userId`를 전달받은 뒤, ProblemService REST API를 호출해 실제 세션/문항 데이터를 조회한 다음 분석을 수행한다. 아래 목록은 분석 로직(`LearningSessionEventListener`, `LearningPatternAnalysisService`, `LearningAnalyticsService`)에서 반드시 필요로 하는 필드들이다.

---

## 1. ProblemService 조회 엔드포인트

```
GET /api/problem/internal/sessions/{sessionId}?userId={userId}
```

### Response 스키마 (하이브리드 구조)

```json
{
  "session": {
    "sessionId": "S-20241124-001",
    "userId": "USER-123",
    "sessionType": "PRACTICE",
    "status": "COMPLETED",
    "startedAt": "2025-11-24T10:00:00",
    "completedAt": "2025-11-24T10:25:30",
    "metadata": {
      "...확장 필드..."
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
        "...확장 필드..."
      }
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
        "...확장 필드..."
      }
    }
  ]
}
```

---

## 2. 세션 메타데이터 (session)

| 필드 | 타입 | 설명 | 분석에서의 사용처 |
|------|------|------|-------------------|
| `sessionId` | string | 학습 세션 ID | 분석 대상 식별 |
| `userId` | string | 사용자 ID | 사용자별 통계 그룹화 |
| `sessionType` | enum (PRACTICE/REVIEW/WRONG_ANSWER) | 세션 성격 | 유형별 성과/추천 로직 |
| `status` | enum (STARTED/IN_PROGRESS/COMPLETED) | 완료 여부 | 미완료 세션 필터링 |
| `startedAt` | datetime | 세션 시작 시각 | 학습 시간/패턴 분석 |
| `completedAt` | datetime | 세션 종료 시각 | 학습 시간/패턴 분석 |
| `createdAt`, `updatedAt` | datetime | 생성/수정 시각 | 증분 분석 기준 |
| `metadata` | json | 확장 필드 (기기 정보, 모드 등) | 향후 추천 로직 확장 |

> **필수 조건**: `session.userId`는 이벤트로 받은 값과 일치해야 하며, 그렇지 않으면 LearningService가 404로 처리.

---

## 3. 문항 + 사용자 답변 데이터 (questions[])

분석 로직은 `QuestionAnswer` + `Question` 정보를 조합해 정답률, 카테고리, 난이도, 풀이 시간 등을 계산한다. 한 문항 레코드는 아래 필드를 반드시 포함해야 한다.

| 필드 | 타입 | 설명 | 분석에서의 사용처 |
|------|------|------|-------------------|
| `questionId` | string | 문제 ID | 식별자 / 오답 리스트 |
| `questionType` | enum (QuestionCategory.QuestionType) | 문제 유형 | 유형별 정답률, 추천 전략 |
| `majorCategory` | enum | 대분류 | 카테고리별 분석 |
| `minorCategory` | enum | 소분류 | 카테고리별 분석 |
| `difficultyLevel` | number 또는 enum | 난이도(1~3 등급) | 난이도별 성취도 |
| `userAnswer` | string | 사용자가 선택한 보기 | 정답률 계산 |
| `isCorrect` | boolean | 정답 여부 | 정답률/오답 추적 |
| `timeSpent` | integer(초) | 풀이 시간 | 평균 풀이시간, 슬로우 패턴 |
| `answeredAt` | datetime | 답안 제출 시각 | 일/주/월 통계, 히트맵 |
| `solveCount` | integer | 해당 문제 풀이 누적 횟수 | 반복 학습 패턴 분석 |
| `metadata` | json | 확장 필드 (보기 텍스트, 해설 등) | 리포트/추천 설명 |

> **주의**: `isCorrect`와 `timeSpent`은 계산이 완료된 값으로 전달되어야 한다. LearningService는 재계산하지 않는다.

---

## 4. 세션 이벤트 로그 (events[]) – 선택 사항

`LearningSessionEventListener`는 추가적으로 세션 이벤트 히스토리를 저장해 증분 분석이나 리포트에 활용한다. 필수는 아니지만, 아래 필드를 포함하면 된다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `eventId` | string | 이벤트 ID |
| `eventType` | string | SESSION_COMPLETED, QUESTION_SKIPPED 등 |
| `sessionId` | string | 세션 ID |
| `userId` | string | 사용자 ID |
| `sessionType` | string | 세션 타입(인덱스용) |
| `createdAt` | datetime | 이벤트 발생 시각 |
| `metadata` | json | 확장 필드 |

---

## 5. 최소 응답 예시

```json
{
  "session": {
    "sessionId": "S-20241124-0001",
    "userId": "USER-123",
    "sessionType": "PRACTICE",
    "status": "COMPLETED",
    "startedAt": "2025-11-24T10:00:00",
    "completedAt": "2025-11-24T10:25:30"
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
      "solveCount": 1
    }
  ]
}
```

---

## 6. 요약

1. **Kafka 이벤트**는 `sessionId`, `userId`만 담는다. (필요 시 `sessionType` 정도 추가 가능)  
2. **ProblemService API**는 위 표에 명시된 세션 + 문항 데이터를 반환해야 한다.  
3. LearningService는 반환값을 그대로 `QuestionAnswer`/`LearningSession` 엔티티에 매핑해 분석 로직을 실행한다.  

이 스펙을 준수하면 ProblemService가 세션/문항을 소유한 상태에서도 분석 서비스가 기존 로직을 그대로 재사용할 수 있다.

