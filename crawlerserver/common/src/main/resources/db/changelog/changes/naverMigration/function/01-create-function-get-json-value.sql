--liquibase formatted sql
--changeset jungwoo_shin:create-get-json-value-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP FUNCTION IF EXISTS GetJsonValue//

CREATE FUNCTION GetJsonValue(json_data TEXT, json_path VARCHAR(255))
RETURNS TEXT DETERMINISTIC
BEGIN
DECLARE result TEXT;
-- 문자열 값에서 따옴표를 제거하기 위해 JSON_UNQUOTE 사용
SET result = JSON_UNQUOTE(JSON_EXTRACT(json_data, json_path));
    -- 존재하지 않는 경로에 대해 NULL대신 빈문자열 반환
    RETURN COALESCE(result, '');
END//