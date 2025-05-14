--liquibase formatted sql
--changeset jungwoo_shin:create-function-safe-to-double-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP FUNCTION IF EXISTS SafeToDouble//

CREATE FUNCTION SafeToDouble(str VARCHAR(255))
RETURNS DOUBLE DETERMINISTIC
BEGIN
DECLARE result DOUBLE DEFAULT NULL;
IF str REGEXP '^[0-9]+(.[0-9]+)?$' THEN
SET result = CAST(str AS DOUBLE);
END IF;
    RETURN result;
END//