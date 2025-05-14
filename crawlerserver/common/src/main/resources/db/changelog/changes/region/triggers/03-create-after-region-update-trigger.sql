--liquibase formatted sql
--changeset jungwoo_shin:create-after-region-update-trigger-v1 runOnChange="true" endDelimiter:// dbms:mariadb

DROP TRIGGER IF EXISTS after_region_update//

CREATE TRIGGER after_region_update
AFTER UPDATE ON regions
FOR EACH ROW
BEGIN
  IF NEW.level = 3 THEN
    INSERT INTO migrations (cortar_no, naver_status, zigbang_status)
    VALUES (NEW.cortar_no, 'NEW', 'NEW')
    ON DUPLICATE KEY UPDATE
      naver_status = 'NEW',
      zigbang_status = 'NEW',
      naver_last_migrated_at = NULL,
      zigbang_last_migrated_at = NULL;
  ELSEIF OLD.level = 3 AND NEW.level != 3 THEN
    DELETE FROM migrations WHERE cortar_no = OLD.cortar_no;
  END IF;

  IF NEW.level = 3 THEN
    INSERT INTO crawls (cortar_no, naver_status, zigbang_status)
    VALUES (NEW.cortar_no, 'NEW', 'NEW')
    ON DUPLICATE KEY UPDATE
      naver_status = 'NEW',
      zigbang_status = 'NEW',
      naver_last_crawled_at = NULL,
      zigbang_last_crawled_at = NULL;
  ELSEIF OLD.level = 3 AND NEW.level != 3 THEN
    DELETE FROM crawls WHERE cortar_no = OLD.cortar_no;
  END IF;
END//