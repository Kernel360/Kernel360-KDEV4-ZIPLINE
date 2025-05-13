# Liquibase에서의 테이블 관리 가이드

## 테이블 생성 및 변경 관리

### 1. 테이블 생성 방식

테이블 생성은 `DROP TABLE`을 사용하지 않고 `CREATE TABLE IF NOT EXISTS`를 사용하는 것이 안전합니다:

```sql
--changeset author:create-table-v1 runOnChange:true dbms:mariadb
CREATE TABLE IF NOT EXISTS table_name (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    /* 기타 컬럼 */
);
```

### 2. 테이블 수정 방식

테이블 구조를 변경할 때는 `ALTER TABLE` 명령을 사용합니다:

#### 2.1 컬럼 추가
```sql
--changeset author:add-column-v1 runOnChange:false dbms:mariadb
ALTER TABLE table_name 
ADD COLUMN IF NOT EXISTS new_column VARCHAR(100);
```

#### 2.2 컬럼 수정
```sql
--changeset author:modify-column-v1 runOnChange:false dbms:mariadb
ALTER TABLE table_name 
MODIFY COLUMN existing_column VARCHAR(200) NOT NULL;
```

#### 2.3 컬럼 삭제 (주의 필요)
```sql
--changeset author:drop-column-v1 runOnChange:false dbms:mariadb
ALTER TABLE table_name 
DROP COLUMN old_column;
```

### 3. 인덱스 관리

인덱스도 데이터 보존을 위해 안전하게 관리해야 합니다:

```sql
--changeset author:add-index-v1 runOnChange:false dbms:mariadb
CREATE INDEX IF NOT EXISTS idx_column_name ON table_name (column_name);
```

## 새로운 테이블 버전 관리 전략

### 전략 1: 작은 변경 사항은 ALTER TABLE 사용
- 컬럼 추가/수정/삭제와 같은 작은 변경사항은 `ALTER TABLE` 사용

### 전략 2: 대규모 변경은 임시 테이블 사용
대규모 구조 변경이 필요할 경우:

```sql
--changeset author:table-restructure-v1 runOnChange:false dbms:mariadb
-- 1. 임시 테이블 생성
CREATE TABLE table_name_new (
    /* 새로운 구조 */
);

-- 2. 데이터 복사
INSERT INTO table_name_new (/*컬럼들*/)
SELECT /*변환된 컬럼들*/ FROM table_name;

-- 3. 테이블 교체
RENAME TABLE table_name TO table_name_old, 
             table_name_new TO table_name;

-- 4. 이전 테이블 삭제 (백업 후)
-- DROP TABLE table_name_old;
```

## 데이터 마이그레이션 관리

### 데이터 초기 로드
```sql
--changeset author:data-load-v1 runOnChange:false dbms:mariadb
INSERT INTO table_name (col1, col2)
VALUES ('value1', 'value2'),
       ('value3', 'value4');
```

### 조건부 데이터 변경
```sql
--changeset author:update-data-v1 runOnChange:false dbms:mariadb
UPDATE table_name 
SET col1 = '새값' 
WHERE col2 = '조건값';
```

## Liquibase 속성 사용 팁

### runOnChange vs. runAlways

- **runOnChange:true** - 스크립트 내용이 변경된 경우에만 실행됨
    - 테이블 생성에 적합: `CREATE TABLE IF NOT EXISTS`

- **runAlways:false** (기본값) - 한 번만 실행됨
    - 데이터 변경에 적합: `ALTER TABLE`, `INSERT`, `UPDATE`

### context 속성 활용

특정 환경에서만 실행되도록 설정:

```sql
--changeset author:id context:dev,test
```

이렇게 하면 개발/테스트 환경에서만 실행되는 변경사항을 정의할 수 있습니다.

## 테이블 변경 모범 사례

1. **스키마 변경은 별도 파일로 관리**: 각 변경사항을 개별 SQL 파일로 관리
2. **의미 있는 changeset ID 사용**: `create-user-table`, `add-email-column-to-user` 등
3. **주석 활용**: 복잡한 변경의 이유를 주석으로 기록
4. **트랜잭션 고려**: 대규모 데이터 변경은 트랜잭션 관리 필요
5. **롤백 계획 수립**: 변경사항 실패 시 롤백 방법 미리 준비

이 가이드를 따르면 데이터 손실 없이 안전하게 테이블을 관리할 수 있습니다.