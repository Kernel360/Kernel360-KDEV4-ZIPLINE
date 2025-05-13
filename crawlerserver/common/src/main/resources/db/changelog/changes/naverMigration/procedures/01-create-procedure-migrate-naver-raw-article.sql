--liquibase formatted sql
--changeset jungwoo_shin:create-procedure-migrate-naver-raw-article-v1 runOnChange:true endDelimiter://
DROP PROCEDURE IF EXISTS MigrateNaverRawArticle//

CREATE PROCEDURE MigrateNaverRawArticle(IN article_id BIGINT)
BEGIN
    DECLARE raw_data TEXT;
    DECLARE article_no VARCHAR(255);
    DECLARE region_code VARCHAR(255);
    DECLARE trad_tp_nm VARCHAR(50);
    DECLARE category_val VARCHAR(20);
    DECLARE price_val BIGINT DEFAULT 0;
    DECLARE deposit_val BIGINT DEFAULT 0;
    DECLARE monthly_rent_val BIGINT DEFAULT 0;
    DECLARE error_msg TEXT;
    DECLARE cortar_no_val BIGINT;

    -- 매물이 존재여부 확인
    DECLARE article_exists INT DEFAULT 0;

    -- 예외처리
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_msg = MESSAGE_TEXT;
        -- 에러로그 기록
        UPDATE naver_raw_articles
        SET migration_status = 'FAILED',
            migration_error = error_msg,
            migrated_at = NOW()
        WHERE id = article_id;
    END;

    -- 로우 데이터 셋
    SELECT raw_data, article_id, cortar_no INTO raw_data, article_no, cortar_no_val
    FROM naver_raw_articles
    WHERE id = article_id;

    -- 지역 코드 셋
    SET region_code = CAST(cortar_no_val AS CHAR);

    -- 추출타입 셋 카테고리 셋
    SET trad_tp_nm = GetJsonValue(raw_data, '$.tradTpNm');

    CASE trad_tp_nm
        WHEN '매매' THEN
            SET category_val = 'SALE';
            SET price_val = SafeToLong(GetJsonValue(raw_data, '$.prc'));
            SET deposit_val = NULL;
            SET monthly_rent_val = NULL;
        WHEN '전세' THEN
            SET category_val = 'DEPOSIT';
            SET price_val = NULL;
            SET deposit_val = SafeToLong(GetJsonValue(raw_data, '$.prc'));
            SET monthly_rent_val = NULL;
        WHEN '월세' THEN
            SET category_val = 'MONTHLY';
            SET price_val = NULL;
            SET deposit_val = SafeToLong(GetJsonValue(raw_data, '$.prc'));
            SET monthly_rent_val = SafeToLong(GetJsonValue(raw_data, '$.rentPrc'));
        ELSE
            SET category_val = 'SALE';
            SET price_val = 0;
            SET deposit_val = NULL;
            SET monthly_rent_val = NULL;
    END CASE;

    --이미 존재하는지 체크
    SELECT COUNT(*) INTO article_exists
    FROM property_articles
    WHERE article_id = article_no;

    IF article_exists > 0 THEN
        -- 기존기록 업데이트
        UPDATE property_articles
        SET region_code = region_code,
            category = category_val,
            building_name = GetJsonValue(raw_data, '$.atclNm'),
            description = GetJsonValue(raw_data, '$.atclFetrDesc'),
            building_type = GetJsonValue(raw_data, '$.rletTpNm'),
            price = price_val,
            deposit = deposit_val,
            monthly_rent = monthly_rent_val,
            longitude = SafeToDouble(GetJsonValue(raw_data, '$.lng')),
            latitude = SafeToDouble(GetJsonValue(raw_data, '$.lat')),
            supply_area = SafeToDouble(GetJsonValue(raw_data, '$.spc1')),
            exclusive_area = SafeToDouble(GetJsonValue(raw_data, '$.spc2')),
            platform = 'NAVER',
            updated_at = NOW()
        WHERE article_id = article_no;
    ELSE
        -- 새기록 삽입
        INSERT INTO property_articles (
            article_id,
            region_code,
            category,
            building_name,
            description,
            building_type,
            price,
            deposit,
            monthly_rent,
            longitude,
            latitude,
            supply_area,
            exclusive_area,
            platform,
            created_at,
            updated_at
        ) VALUES (
            article_no,
            region_code,
            category_val,
            GetJsonValue(raw_data, '$.atclNm'),
            GetJsonValue(raw_data, '$.atclFetrDesc'),
            GetJsonValue(raw_data, '$.rletTpNm'),
            price_val,
            deposit_val,
            monthly_rent_val,
            SafeToDouble(GetJsonValue(raw_data, '$.lng')),
            SafeToDouble(GetJsonValue(raw_data, '$.lat')),
            SafeToDouble(GetJsonValue(raw_data, '$.spc1')),
            SafeToDouble(GetJsonValue(raw_data, '$.spc2')),
            'NAVER',
            NOW(),
            NOW()
        );
    END IF;

    -- 완료로 표시
    UPDATE naver_raw_articles
    SET migration_status = 'COMPLETED',
        migration_error = NULL,
        migrated_at = NOW()
    WHERE id = article_id;
END//
