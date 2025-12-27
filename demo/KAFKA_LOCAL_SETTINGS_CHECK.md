# Kafka 로컬 설정 확인 가이드

## 현재 설정 상태

### ✅ application.properties
```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9094}
```
- 기본값: `localhost:9094` ✅
- 환경 변수 `KAFKA_BOOTSTRAP_SERVERS`가 설정되어 있으면 그 값을 사용

### ✅ KafkaTopicConfig
- `bootstrapServers` 값을 `@Value`로 주입받아 사용 ✅
- 로컬 실행 시 추가 설정 적용 ✅

## 문제 진단

로그에서 확인된 문제:
```
Connection to node -1 (localhost/127.0.0.1:9094) could not be established. 
Node may not be available.
```

### 가능한 원인

1. **Kafka 컨테이너가 9094 포트로 리스닝하지 않음**
   - 확인: `docker ps | grep kafka`로 포트 매핑 확인
   - 확인: `netstat -an | grep 9094` 또는 `lsof -i :9094`로 포트 리스닝 확인

2. **Kafka의 advertised listener 설정 문제**
   - Kafka가 `localhost:9094`를 advertised listener로 반환하지 않음
   - 컨테이너 설정에서 `PLAINTEXT_HOST://localhost:9094` 확인 필요

3. **방화벽 또는 네트워크 문제**
   - 로컬에서 9094 포트 접근이 차단되어 있을 수 있음

## 확인 방법

### 1. 환경 변수 확인
```bash
echo $KAFKA_BOOTSTRAP_SERVERS
# 출력이 없어야 함 (또는 localhost:9094)
```

### 2. Kafka 컨테이너 포트 확인
```bash
docker ps | grep kafka
# 포트 매핑이 9094:9092인지 확인
```

### 3. 포트 리스닝 확인
```bash
# macOS
lsof -i :9094

# 또는
netstat -an | grep 9094
```

### 4. Kafka 연결 테스트
```bash
# Kafka 컨테이너 내부에서 테스트
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# 호스트에서 직접 테스트 (kafka 클라이언트가 설치되어 있는 경우)
kafka-topics --bootstrap-server localhost:9094 --list
```

## 해결 방법

### 방법 1: Kafka 컨테이너 설정 확인
API Gateway의 docker-compose.yml에서:
```yaml
kafka:
  ports:
    - "9094:9092"  # 외부:내부 포트 매핑 확인
  environment:
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,PLAINTEXT_HOST://0.0.0.0:9092
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9094
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
```

### 방법 2: 애플리케이션 재시작
환경 변수를 확인한 후 애플리케이션을 재시작:
```bash
# 환경 변수 제거 (필요한 경우)
unset KAFKA_BOOTSTRAP_SERVERS

# 애플리케이션 재시작
```

## 현재 로컬 설정 요약

✅ **정상 설정된 항목:**
- `application.properties`: `localhost:9094` 설정
- `KafkaTopicConfig`: bootstrapServers 주입 및 사용
- 로컬 실행 모드 감지 및 추가 설정

⚠️ **확인 필요한 항목:**
- Kafka 컨테이너의 포트 매핑 (9094:9092)
- Kafka 컨테이너의 advertised listener 설정
- 실제 포트 리스닝 상태

