// SPDX-FileCopyrightText: 2023 LakeSoul Contributors
//
// SPDX-License-Identifier: Apache-2.0

package org.apache.flink.lakesoul.types;

import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.*;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.apache.flink.lakesoul.types.LakeSoulRecordConvert.hashMap;

public class ParseDocument {


    public static Struct convertBSONToStruct(String value) {
        Document bsonDocument = Document.parse(value);
        SchemaBuilder structSchemaBuilder = SchemaBuilder.struct();
        Struct struct = new Struct(buildSchema(bsonDocument, structSchemaBuilder));
        fillStructValues(bsonDocument, struct);
        return struct;
    }

    private static Schema buildSchema(Document bsonDocument, SchemaBuilder structSchemaBuilder) {
        HashMap<String, Object> tempHashMap = new HashMap<>(hashMap);
        for (Map.Entry<String, Object> entry : bsonDocument.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            tempHashMap.remove(fieldName);
            if (value == null ){
                hashMap.putIfAbsent(fieldName, null);
                if (hashMap.get(fieldName) != null){
                    Schema schemaFromMap = (Schema) hashMap.get(fieldName);
                    structSchemaBuilder.field(fieldName, schemaFromMap);
                }
            } else {
                if (value instanceof Document) {
                    SchemaBuilder nestedStructSchemaBuilder = SchemaBuilder.struct();
                    Schema schema = buildSchema((Document) value, nestedStructSchemaBuilder);
                    structSchemaBuilder.field(fieldName, schema);
                    hashMap.put(fieldName, schema);
                }  else if (value instanceof List) {
                    List<?> arrayList = (List<?>) value;
                    Schema arraySchema = getSchemaForArrayList(arrayList);
                    structSchemaBuilder.field(fieldName, arraySchema);
                    hashMap.put(fieldName, arraySchema);
                } else {
                    structSchemaBuilder.field(fieldName, getSchemaForValue(value));
                    hashMap.put(fieldName,getSchemaForValue(value));
                }
            }
        }
        tempHashMap.forEach((k, v) -> {
            if (v instanceof Schema) {
                structSchemaBuilder.field(k, (Schema) v);
            }
        });
        return structSchemaBuilder.build();
    }

    private static Schema getSchemaForArrayList(List<?> arrayList) {
        Schema elementSchema = null;
        if (!arrayList.isEmpty()) {
            Object firstElement = arrayList.get(0);
            elementSchema = getSchemaForValue(firstElement);
        }
        return SchemaBuilder.array(elementSchema).build();
    }

    private static void fillStructValues(Document bsonDocument, Struct struct) {
        if (bsonDocument.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : bsonDocument.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            if (value != null){
                if (value instanceof Document) {
                    Struct nestedStruct = new Struct(struct.schema().field(fieldName).schema());
                    fillStructValues((Document) value, nestedStruct);
                    struct.put(fieldName, nestedStruct);
                } else if (value instanceof List) {
                    List<?> arrayList = (List<?>) value;
                    struct.put(fieldName, arrayList);
                } else if (value instanceof Decimal128) {
                    BigDecimal decimalValue = new BigDecimal(value.toString());
                    struct.put(fieldName, decimalValue);
                } else if (value instanceof Binary) {
                    Binary binaryData = (Binary) value;
                    struct.put(fieldName,binaryData.getData());
                } else if (value instanceof BsonTimestamp) {
                    BsonTimestamp bsonTimestamp = (BsonTimestamp) value;
                    struct.put(fieldName,bsonTimestamp.getValue());
                } else {
                    struct.put(fieldName,value);
                }
            } else {
                if (hashMap.get(fieldName) != null){
                    struct.put(fieldName,null);
                }
            }
        }
    }

    private static Schema getSchemaForValue(Object value) {
        if (value instanceof String) {
            return Schema.OPTIONAL_STRING_SCHEMA;
        } else if (value instanceof Integer) {
            return Schema.OPTIONAL_INT32_SCHEMA;
        } else if (value instanceof Long) {
            return Schema.OPTIONAL_INT64_SCHEMA;
        } else if (value instanceof Double) {
            return Schema.OPTIONAL_FLOAT64_SCHEMA;
        } else if (value instanceof Boolean) {
            return Schema.OPTIONAL_BOOLEAN_SCHEMA;
        } else if (value instanceof Decimal128) {
            BigDecimal decimalValue = new BigDecimal(value.toString());
            return Decimal.builder(decimalValue.scale()).optional().build();
        } else if (value instanceof Byte) {
            return Schema.OPTIONAL_BYTES_SCHEMA;
        } else if (value instanceof Binary) {
            return Schema.OPTIONAL_BYTES_SCHEMA;
        } else if (value instanceof Date) {
            return Timestamp.builder().optional().build();
        } else if (value instanceof BsonTimestamp) {
            return Schema.OPTIONAL_INT64_SCHEMA;
        } else {
            return Schema.OPTIONAL_STRING_SCHEMA;
        }
    }
}
