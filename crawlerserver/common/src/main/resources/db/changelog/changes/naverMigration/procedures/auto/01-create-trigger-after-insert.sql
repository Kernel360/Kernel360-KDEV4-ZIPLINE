--liquibase formatted sql
--changeset jungwoo_shin:create-trigger-after-insert-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP TRIGGER IF EXISTS after_naver_raw_article_insert//

CREATE TRIGGER after_naver_raw_article_insert
AFTER INSERT ON naver_raw_articles
FOR EACH ROW
BEGIN
-- PENDING 상태이거나 NULL인 경우에만 마이그레이션 호출
IF NEW.migration_status = 'PENDING' OR NEW.migration_status IS NULL THEN
CALL MigrateNaverRawArticle(NEW.id);
END IF;
END//
