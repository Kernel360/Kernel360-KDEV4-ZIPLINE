--liquibase formatted sql
--changeset jungwoo_shin:create-function-safe-to-long-v1 runOnChange:true-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP FUNCTION IF EXISTS SafeToLong//

CREATE FUNCTION SafeToLong(str VARCHAR(255))
RETURNS BIGINT DETERMINISTIC
BEGIN
DECLARE result BIGINT DEFAULT 0;
    IF str REGEXP '^[0-9]+$' THEN
        SET result = CAST(str AS SIGNED);
    END IF;
    RETURN result;
END//