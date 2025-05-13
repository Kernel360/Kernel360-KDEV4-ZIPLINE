--liquibase formatted sql
--changeset jungwoo_shin:create-regions-table-v1 runOnChange:true dbms:mariadb

CREATE TABLE IF NOT EXISTS regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cortar_no BIGINT NOT NULL UNIQUE,
    level INT NOT NULL,
    cortar_name VARCHAR(50) NOT NULL,
    center_lat DECIMAL(10, 7),
    center_lon DECIMAL(10, 7),
    parent_cortar_no BIGINT,
    FOREIGN KEY (parent_cortar_no) REFERENCES regions(cortar_no)
);