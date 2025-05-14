
SET SQL_SAFE_UPDATES = 0;
UPDATE ziplinedb.naver_raw_articles

SET migrated_at = NULL,
    migration_status = "PENDING",
    migration_error = NULL;
    
DELETE FROM `ziplinedb`.`property_articles`;

commit;