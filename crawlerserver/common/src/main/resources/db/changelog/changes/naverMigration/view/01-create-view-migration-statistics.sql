--liquibase formatted sql
--changeset jungwoo_shin:create-view-migration-statistics-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP VIEW IF EXISTS migration_statistics//

CREATE OR REPLACE VIEW migration_statistics AS
SELECT
    COUNT(*) as total_count,
    SUM(CASE WHEN migration_status = 'PENDING' THEN 1 ELSE 0 END) as pending_count,
    SUM(CASE WHEN migration_status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_count,
    SUM(CASE WHEN migration_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count
FROM naver_raw_articles//