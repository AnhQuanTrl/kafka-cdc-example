package com.example;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.cache.Cache;
import org.apache.kafka.common.cache.LRUCache;
import org.apache.kafka.common.cache.SynchronizedCache;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import static org.apache.kafka.connect.transforms.util.Requirements.requireMap;
import static org.apache.kafka.connect.transforms.util.Requirements.requireStruct;

/**
 * Hello world!
 *
 */
public class ReplaceWithTombstone<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final Logger log = LoggerFactory.getLogger(ReplaceWithTombstone.class);


    private interface ConfigName {
        String DELETED_FIELD_NAME = "deleted.field.name";
    }

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(ConfigName.DELETED_FIELD_NAME, ConfigDef.Type.STRING, "__deleted", ConfigDef.Importance.HIGH,
                    "Field name that mark deletion");

                    private static final String PURPOSE = "Replace with tombstone if deleted";

    private String fieldName;

    @Override
    public void configure(Map<String, ?> props) {
        final SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
        fieldName = config.getString(ConfigName.DELETED_FIELD_NAME);
    }

    @Override
    public R apply(R record) {
        if (record.valueSchema() == null) {
            return applySchemaless(record);
        } else {
            return applyWithSchema(record);
        }
    }

    private R applySchemaless(R record) {
        final Map<String, Object> value = requireMap(record.value(), PURPOSE);
        final Object val = value.get(fieldName);
        if (val != null && (String)val == "true") {
            return record.newRecord(
                    record.topic(),
                    record.kafkaPartition(),
                    record.keySchema(),
                    record.key(),
                    null,
                    null,
                    record.timestamp());
        }
        return record;
    }

    private R applyWithSchema(R record) {
        final Struct value = requireStruct(record.value(), PURPOSE);
        final Field field = value.schema().field(fieldName);
        final Object origFieldValue = value.get(field);
        log.info("DELETED field class: " + origFieldValue.getClass());
        log.info("DELETED field: " + origFieldValue.toString());
            if (origFieldValue.equals("true")) {
                log.info("DELETED field: SUCCESS");
                return record.newRecord(
                        record.topic(),
                        record.kafkaPartition(),
                        record.keySchema(),
                        record.key(),
                        null,
                        null,
                        record.timestamp());
            }
        return record;
    }

    @Override
    public void close() {
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
