CREATE STREAM THIRD_PARTIES_RAW
WITH (KAFKA_TOPIC ='thirdparty.public.thirdparties',
      KEY_FORMAT  ='AVRO',
      VALUE_FORMAT='AVRO');
      

CREATE STREAM THIRD_PARTIES AS
    SELECT
        ROWKEY -> id AS id,
        name AS name,
        tenant_id AS tenant_id,
        __deleted
    FROM
        THIRD_PARTIES_RAW
    PARTITION BY ROWKEY -> id
    EMIT CHANGES;

CREATE STREAM TENANTS_RAW
WITH (KAFKA_TOPIC ='thirdparty.public.tenants',
      KEY_FORMAT  ='AVRO',
      VALUE_FORMAT='AVRO');

CREATE TABLE TBL_TENANTS AS 
    SELECT
        id,
        LATEST_BY_OFFSET(name) AS name
    FROM TENANTS_RAW
    GROUP BY id;

CREATE STREAM THIRD_PARTIES_WITH_TENANT AS
    SELECT
        tp.id AS id,
        tp.name AS name,
        tp.tenant_id AS tenant_id,
        tp.__DELETED AS __DELETED,
        t.name AS tenant_name
    FROM THIRD_PARTIES tp
    JOIN TBL_TENANTS t
    ON t.id = tp.tenant_id
    PARTITION BY tp.id;