# 🗄️ DDL 실행 가이드

## 📋 **개요**
이 문서는 `schema.sql` 파일을 사용하여 Learning Service 데이터베이스를 생성하는 방법을 설명합니다.

## 🚀 **실행 방법**

### **방법 1: MySQL 명령줄에서 직접 실행**
```bash
# MySQL 접속
mysql -u root -p

# 스키마 파일 실행
source /path/to/your/project/demo/src/main/resources/schema.sql;
```

### **방법 2: MySQL Workbench에서 실행**
1. MySQL Workbench 실행
2. `File` → `Open SQL Script`
3. `schema.sql` 파일 선택
4. `Execute` 버튼 클릭

### **방법 3: 터미널에서 한 번에 실행**
```bash
mysql -u root -p < demo/src/main/resources/schema.sql
```

## 🔧 **사전 요구사항**

### **1. MySQL 서버 실행 확인**
```bash
# MySQL 서비스 상태 확인
brew services list | grep mysql

# MySQL 서비스 시작 (필요시)
brew services start mysql
```

### **2. MySQL 접속 테스트**
```bash
mysql -u root -p
# 비밀번호 입력: 1234
```

### **3. 데이터베이스 목록 확인**
```sql
SHOW DATABASES;
```

## 📊 **생성되는 테이블 구조**

### **핵심 테이블**
1. **`user_profiles`** - 사용자 프로필 (UserService)
2. **`user_interests`** - 사용자 관심사 (1:N)
3. **`user_selected_minor_categories`** - 선택된 소분류 (1:N)
4. **`learning_sessions`** - 학습 세션 (핵심)
5. **`question_answers`** - 문제 답변 (1:N)

### **뷰 (View)**
1. **`user_learning_stats`** - 사용자별 학습 통계
2. **`session_question_details`** - 세션별 문제 상세

## 🎯 **테스트 데이터**

### **자동 생성되는 테스트 사용자**
- **user001**: 비즈니스 영어 학습자 (IT 도메인)
- **user002**: 학업 영어 학습자 (컴퓨터공학)
- **user003**: 여행 영어 학습자 (여행사)

### **테스트 데이터 확인**
```sql
USE learning_service;

-- 사용자 프로필 확인
SELECT * FROM user_profiles;

-- 사용자 관심사 확인
SELECT * FROM user_interests;

-- 선택된 소분류 확인
SELECT * FROM user_selected_minor_categories;
```

## ⚠️ **주의사항**

### **1. 기존 데이터베이스**
- `learning_service` 데이터베이스가 이미 존재하면 오류 발생
- 필요시 기존 데이터베이스 삭제 후 재생성

### **2. 권한 문제**
- MySQL root 계정으로 접속 필요
- 애플리케이션용 별도 사용자 생성 권장

### **3. 문자셋**
- `utf8mb4` 사용으로 이모지 지원
- 한글 데이터 정상 저장

## 🔍 **문제 해결**

### **1. "Access denied" 오류**
```sql
-- root 권한으로 접속
mysql -u root -p

-- 또는 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

### **2. "Database already exists" 오류**
```sql
-- 기존 데이터베이스 삭제
DROP DATABASE IF EXISTS learning_service;

-- 스키마 재실행
source /path/to/schema.sql;
```

### **3. "Table already exists" 오류**
```sql
-- 테이블 삭제 후 재생성
DROP TABLE IF EXISTS question_answers;
DROP TABLE IF EXISTS learning_sessions;
-- ... 기타 테이블들

-- 스키마 재실행
source /path/to/schema.sql;
```

## 📈 **성능 최적화**

### **생성된 인덱스**
- **단일 인덱스**: 자주 조회되는 컬럼별
- **복합 인덱스**: 함께 조회되는 컬럼 조합
- **외래키 인덱스**: 자동 생성

### **추가 인덱스 권장사항**
```sql
-- 사용자별 세션 조회 최적화
CREATE INDEX idx_learning_sessions_user_date 
ON learning_sessions(user_id, started_at DESC);

-- 문제 답변 통계 조회 최적화
CREATE INDEX idx_question_answers_category_correct 
ON question_answers(major_category, minor_category, is_correct);
```

## 🎉 **완료 확인**

### **1. 테이블 생성 확인**
```sql
USE learning_service;
SHOW TABLES;
```

### **2. 테스트 데이터 확인**
```sql
-- 사용자 수 확인
SELECT COUNT(*) FROM user_profiles;

-- 세션 테이블 구조 확인
DESCRIBE learning_sessions;

-- 문제 답변 테이블 구조 확인
DESCRIBE question_answers;
```

### **3. 뷰 생성 확인**
```sql
SHOW FULL TABLES WHERE Table_type = 'VIEW';
```

## 🚀 **다음 단계**

DDL 실행 완료 후:
1. **Spring Boot 애플리케이션 실행**
2. **JPA 자동 테이블 생성 확인**
3. **테스트 시나리오 실행**

---

**🎯 DDL 실행이 완료되면 애플리케이션이 정상적으로 실행될 것입니다!**
