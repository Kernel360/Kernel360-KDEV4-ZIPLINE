--liquebase formattedsql
--changeset jungwoo_shin:koreacortano-v1 runOnChange:true dbms:mariadb

INSERT INTO regions (cortar_no, level, cortar_name, center_lat, center_lon, parent_cortar_no)
VALUES (0,0,'대한민국',36.5,127.5,NULL);