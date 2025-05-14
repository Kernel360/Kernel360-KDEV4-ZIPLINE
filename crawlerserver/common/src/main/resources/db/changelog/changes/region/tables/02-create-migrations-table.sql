--liquibase formatted sql
--changeset jungwoo_shin:create-migrations-table-v1 runOnChange:true dbms:mariadb

CREATE TABLE IF NOT EXISTS migrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cortar_no BIGINT NOT NULL UNIQUE,
    naver_status VARCHAR(20),
    naver_last_migrated_at DATETIME,
    error_log TEXT
);