#Liquebase 프로시져 함수 유지보수 가이드

### 1. changeset과 id의 중요성

- `changeset` 태그에 있는 `author:id` 조합은 Liquibase에서 고유한 식별자로 작동합니다.
- 동일한 `author:id`를 가진 changeset은 한 번만 실행됩니다.
- 따라서 **함수를 수정할 때는 새로운 id를 사용**해야 합니다.

### 2. 유지보수를 위한 접근 방법

#### 방법 1: DROP-CREATE 방식 
```sql
--changeset jungwoo_shin:create-trigger-after-update-v1 endDelimiter:// dbms:mariadb
DROP FUNCTION IF EXISTS MyFunction//
CREATE FUNCTION MyFunction...

--changeset jungwoo_shin:create-trigger-after-update-v1 endDelimiter:// dbms:mariadb
DROP FUNCTION IF EXISTS MyFunction//
CREATE FUNCTION MyFunction... -- 수정된 버전
```
dbms:mariadb
#### 방법 2: runOnChange 속성 사용
```sql
--changeset jungwoo_shin:create-trigger-after-update-v1 runOnChange:true endDelimiter:// dbms:mariadb
DROP FUNCTION IF EXISTS MyFunction//
CREATE FUNCTION MyFunction...
```
- `runOnChange:true`를 사용하면 changeset의 내용이 변경되었을 때만 재실행됩니다.
- 이는 함수 내용이 변경되었을 때만 업데이트 되므로 더 효율적입니다.

#### 방법 3: runAlways 속성 사용(현재 사용중인 방법)
```sql
--changeset jungwoo_shin:create-function runAlways:true dbms:mariadb
DROP FUNCTION IF EXISTS MyFunction;
CREATE FUNCTION MyFunction...
```
- `runAlways:true`를 사용하면 매번 Liquibase가 실행될 때마다 해당 changeset이 실행됩니다.
- 개발 환경에서는 유용할 수 있지만, 프로덕션 환경에서는 주의해서 사용해야 합니다.

### 3. 버전 관리 전략

함수나 프로시저를 업데이트할 때는 다음과 같은 버전 관리 전략을 고려할 수 있습니다:

(현재 사용중인 방법)
1. **버전 번호를 id에 포함시키기**:
   ```sql
   --changeset jungwoo_shin:function-safe-to-double-v1 dbms:mariadb
   --changeset jungwoo_shin:function-safe-to-double-v2 dbms:mariadb
   ```

2. **날짜를 id에 포함시키기**:
   ```sql
   --changeset jungwoo_shin:function-safe-to-double-20250513 dbms:mariadb
   ```

### 4. 최종 권장 방식
```sql
--liquibase formatted sql
--changeset jungwoo_shin:function-safe-to-double-v1 dbms:mariadb runOnChange:true
DROP FUNCTION IF EXISTS SafeToDouble;

CREATE FUNCTION SafeToDouble(str VARCHAR(255))
RETURNS DOUBLE DETERMINISTIC
BEGIN
    -- 함수 내
용
END;
```

이렇게 설정하면:
1. 함수 변경 시 자동으로 업데이트됩니다
2. 변경이 없으면 실행되지 않습니다
3. 버전 히스토리가 명확히 추적됩니다