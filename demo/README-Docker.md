# 학습 이력 관리 서비스 - Docker 실행 가이드

## 사전 요구사항

- Docker
- Docker Compose
- 최소 4GB RAM

## 빠른 시작

### 1. 서비스 실행

```bash
# 모든 서비스 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 2. 서비스 상태 확인

```bash
# 모든 서비스 상태 확인
docker-compose ps

# 특정 서비스 로그 확인
docker-compose logs app
docker-compose logs db
docker-compose logs redis
docker-compose logs kafka
```

### 3. 서비스 중지

```bash
# 모든 서비스 중지
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```

## 서비스 구성

### 애플리케이션 (App)
- **포트**: 8080
- **환경**: Spring Boot + Java 17
- **기능**: 학습 이력 이벤트 집계 API

### 데이터베이스 (MySQL)
- **포트**: 3306
- **데이터베이스**: learning_service_db
- **사용자**: learning_user
- **비밀번호**: learning_password

### Redis
- **포트**: 6379
- **용도**: 학습 세션 캐싱

### Kafka
- **포트**: 29092 (외부 접근용)
- **용도**: 학습 이벤트 스트리밍

### Zookeeper
- **포트**: 2181
- **용도**: Kafka 클러스터 관리

### Nginx
- **포트**: 80, 443
- **용도**: 웹 서버 및 로드 밸런서

## API 테스트

### 1. 헬스 체크
```bash
curl http://localhost:8080/actuator/health
```

### 2. 학습 이벤트 기록
```bash
curl -X POST http://localhost:8080/api/learning-events \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "eventType": "START",
    "learningItemId": "lesson001",
    "duration": 300
  }'
```

### 3. 학습 이력 집계 조회
```bash
curl http://localhost:8080/api/learning-events/users/user123/items/lesson001/summary
```

## 문제 해결

### 포트 충돌
```bash
# 사용 중인 포트 확인
netstat -tulpn | grep :8080

# Docker 컨테이너 포트 변경
# docker-compose.yml에서 ports 섹션 수정
```

### 데이터베이스 연결 실패
```bash
# MySQL 컨테이너 상태 확인
docker-compose logs db

# MySQL 컨테이너 재시작
docker-compose restart db
```

### 메모리 부족
```bash
# Docker 메모리 제한 확인
docker stats

# docker-compose.yml에서 메모리 제한 설정
```

## 개발 환경

### 로컬 개발
```bash
# 로컬에서 실행 (Docker 없이)
./gradlew bootRun
```

### Docker 빌드
```bash
# 이미지 빌드
docker build -t learning-service .

# 컨테이너 실행
docker run -p 8080:8080 learning-service
```

## 모니터링

### 애플리케이션 메트릭
- http://localhost:8080/actuator/metrics
- http://localhost:8080/actuator/health

### 데이터베이스 모니터링
```bash
# MySQL 접속
docker exec -it learning-service-mysql mysql -u learning_user -p learning_service_db
```

### Kafka 토픽 확인
```bash
# 토픽 리스트
docker exec -it learning-service-kafka kafka-topics --bootstrap-server localhost:9092 --list
```
