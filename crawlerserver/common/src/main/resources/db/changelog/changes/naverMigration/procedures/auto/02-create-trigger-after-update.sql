--liquibase formatted sql
--changeset jungwoo_shin:create-trigger-after-update-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP TRIGGER IF EXISTS after_naver_raw_article//

CREATE TRIGGER after_naver_raw_article_update
AFTER UPDATE ON naver_raw_articles
FOR EACH ROW
    BEGIN
    -- 펜딩으로 변경시 트리거
    IF NEW.migration_status = 'PENDING' AND (OLD.migration_status != 'PENDING' OR OLD.migration_status IS NULL) THEN
    CALL MigrateNaverRawArticle(NEW.id);
    END IF;
END//
