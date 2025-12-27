# Kafka 연결 문제 해결 가이드

## 🔍 문제 상황
- 로그에서 `UnknownHostException: kafka` 에러 발생
- 로컬에서 실행 중인데 `kafka:9092`로 연결 시도

## ✅ 해결 방법

### 1. 환경 변수 확인 및 제거

로컬에서 실행 시 환경 변수가 설정되어 있으면 제거:

```bash
# 환경 변수 확인
echo $KAFKA_BOOTSTRAP_SERVERS

# 환경 변수 제거 (현재 세션에서만)
unset KAFKA_BOOTSTRAP_SERVERS

# 또는 .zshrc, .bashrc 등에서 제거
```

### 2. Kafka 컨테이너 상태 확인

```bash
# Kafka 컨테이너가 실행 중인지 확인
docker ps | grep kafka

# Kafka 컨테이너 로그 확인
docker logs kafka

# Kafka 컨테이너 재시작 (필요한 경우)
docker restart kafka
```

### 3. Kafka 연결 테스트

```bash
# 로컬에서 Kafka 연결 테스트
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# 또는 호스트에서 직접 테스트
kafka-topics --bootstrap-server localhost:9094 --list
```

### 4. 애플리케이션 재시작

환경 변수를 제거한 후 애플리케이션을 재시작하면 `application.properties`의 기본값(`localhost:9094`)을 사용합니다.

## 📝 현재 설정

**application.properties**:
```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9094}
```

- 환경 변수가 없으면: `localhost:9094` 사용 ✅
- 환경 변수가 `kafka:9092`로 설정되어 있으면: `kafka:9092` 사용 ❌

## 🎯 컨슈머 그룹 등록

컨슈머 그룹은 **Kafka 서버에 연결되면 자동으로 등록**됩니다.

1. 애플리케이션이 정상적으로 Kafka에 연결되면
2. 컨슈머가 토픽을 구독하기 시작하면
3. Kafka가 자동으로 컨슈머 그룹을 생성하고 등록합니다

**컨슈머 그룹 확인 방법**:
```bash
# 컨슈머 그룹 목록 확인
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# 특정 컨슈머 그룹 상세 정보
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group learning-service-analysis-group
```

## ⚠️ 주의사항

- **Kafka 컨테이너 재실행은 필요 없을 수 있습니다**
- 문제는 연결 설정(환경 변수)일 가능성이 높습니다
- 컨테이너가 정상 실행 중이라면 재시작할 필요 없음

## 🔄 해결 순서

1. ✅ 환경 변수 `KAFKA_BOOTSTRAP_SERVERS` 확인 및 제거
2. ✅ Kafka 컨테이너가 정상 실행 중인지 확인
3. ✅ 애플리케이션 재시작
4. ✅ 로그에서 `localhost:9094`로 연결 시도하는지 확인
5. ✅ 컨슈머 그룹 자동 등록 확인

