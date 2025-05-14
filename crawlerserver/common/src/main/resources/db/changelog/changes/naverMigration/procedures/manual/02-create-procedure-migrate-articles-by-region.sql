--liquibase formatted sql
--changeset jungwoo_shin:create-trigger-after-update-v1 runOnChange:true endDelimiter:// dbms:mariadb

DROP PROCEDURE IF EXISTS MigrateArticlesByRegion//

CREATE PROCEDURE MigrateArticlesByRegion(IN region_code BIGINT)
BEGIN
DECLARE done INT DEFAULT FALSE;
DECLARE article_id BIGINT;
DECLARE cur CURSOR FOR SELECT id FROM naver_raw_articles
WHERE cortar_no = region_code
AND migration_status = 'PENDING';
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO article_id;
        IF done THEN
            LEAVE read_loop;
        END IF;

        CALL MigrateNaverRawArticle(article_id);

        -- 과부하 방지를 위한 지연
        DO SLEEP(0.01);
    END LOOP;

    CLOSE cur;
END//