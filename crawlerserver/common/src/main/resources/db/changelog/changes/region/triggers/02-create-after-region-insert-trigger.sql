--liquibase formatted sql
--changeset jungwoo_shin:create-after-region-insert-trigger-v1 runOnChange="true" endDelimiter:// dbms:mariadb

DROP TRIGGER IF EXISTS after_region_insert//

CREATE TRIGGER after_region_insert
AFTER INSERT ON regions
FOR EACH ROW
BEGIN
    IF NEW.level = 3 THEN
        INSERT IGNORE INTO migrations (cortar_no, naver_status, zigbang_status)
        VALUES (NEW.cortar_no, 'NEW', 'NEW');

        INSERT IGNORE INTO crawls (cortar_no, naver_status, zigbang_status)
        VALUES (NEW.cortar_no, 'NEW', 'NEW');
    END IF;
END//