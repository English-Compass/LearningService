#!/bin/bash

# Kafka 토픽 생성 스크립트
# Learning Service용 토픽들을 체계적으로 생성

echo "🚀 Learning Service Kafka 토픽 생성을 시작합니다..."

# Kafka 서비스가 준비될 때까지 대기
echo "⏳ Kafka 서비스 준비 대기 중..."
until kafka-topics --bootstrap-server kafka:9092 --list; do
    echo "Kafka 서비스가 아직 준비되지 않았습니다. 10초 후 재시도..."
    sleep 10
done

echo "✅ Kafka 서비스가 준비되었습니다!"

# ===== 원시 이벤트 토픽 (Raw Events) =====
echo "📝 원시 이벤트 토픽 생성 중..."

# 학습 세션 시작 이벤트
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-session-started \
    --partitions 3 --replication-factor 1 \
    --config retention.ms=604800000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 학습 세션 완료 이벤트
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-session-completed \
    --partitions 3 --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 문제 답변 이벤트
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-question-answered \
    --partitions 6 --replication-factor 1 \
    --config retention.ms=604800000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# ===== 집계 결과 토픽 (Aggregated Results) =====
echo "📊 집계 결과 토픽 생성 중..."

# 사용자 학습 패턴 집계
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-user-patterns \
    --partitions 3 --replication-factor 1 \
    --config retention.ms=7776000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 문제별 성과 집계
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-question-performance \
    --partitions 3 --replication-factor 1 \
    --config retention.ms=7776000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 카테고리별 성과 집계
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-category-performance \
    --partitions 2 --replication-factor 1 \
    --config retention.ms=7776000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# ===== 패턴 분석 토픽 (Pattern Analysis) =====
echo "🔍 패턴 분석 토픽 생성 중..."

# 정답률 패턴 분석
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-accuracy-patterns \
    --partitions 2 --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 오답 패턴 분석
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-wrong-answer-patterns \
    --partitions 2 --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 난이도 패턴 분석
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-difficulty-patterns \
    --partitions 2 --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# ===== 추천 및 최적화 토픽 (Recommendations) =====
echo "💡 추천 및 최적화 토픽 생성 중..."

# 개인화된 학습 추천
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-personalized-recommendations \
    --partitions 3 --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 복습 일정 계획
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-review-schedule \
    --partitions 3 --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# ===== 시스템 모니터링 토픽 (System Monitoring) =====
echo "📡 시스템 모니터링 토픽 생성 중..."

# 시스템 상태 이벤트
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-system-health \
    --partitions 1 --replication-factor 1 \
    --config retention.ms=604800000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# 에러 및 예외 이벤트
kafka-topics --bootstrap-server kafka:9092 \
    --create --topic learning-error-events \
    --partitions 2 --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete \
    --config compression.type=lz4

# ===== 토픽 생성 완료 확인 =====
echo "✅ 모든 토픽 생성이 완료되었습니다!"
echo "📋 생성된 토픽 목록:"

kafka-topics --bootstrap-server kafka:9092 --list | grep "learning-"

echo ""
echo "🎯 토픽별 상세 정보:"

# 토픽별 상세 정보 출력
for topic in learning-session-started learning-session-completed learning-question-answered \
             learning-user-patterns learning-question-performance learning-category-performance \
             learning-accuracy-patterns learning-wrong-answer-patterns learning-difficulty-patterns \
             learning-personalized-recommendations learning-review-schedule \
             learning-system-health learning-error-events; do
    
    echo "📊 $topic:"
    kafka-topics --bootstrap-server kafka:9092 --describe --topic "$topic" | head -3
    echo ""
done

echo "🚀 Learning Service Kafka 토픽 설정이 완료되었습니다!"

