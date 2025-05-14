--liquibase formatted sql
--changeset jungwoo_shin:create-procedure-retry-failed-migrations-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP PROCEDURE IF EXISTS RetryFailedMigrations//

CREATE PROCEDURE RetryFailedMigrations()
BEGIN
-- 실패한 함수들 펜딩으로 변경
UPDATE naver_raw_articles
SET migration_status = 'PENDING',
migration_error = NULL,
migrated_at = NULL
WHERE migration_status = 'FAILED';
-- 재실행
    CALL MigrateAllPendingArticles();
END//