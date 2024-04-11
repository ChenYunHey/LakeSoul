package org.apache.flink.lakesoul.test.ArrowTest;

import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Schema;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.SchemaBuilder;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Struct;

public class Main {
    public static void main(String[] args) {
        // 定义模式
        Schema schema = SchemaBuilder.struct()
                .field("field1", Schema.STRING_SCHEMA)
                .field("field2", Schema.OPTIONAL_STRING_SCHEMA)
                .field("field3", Schema.OPTIONAL_INT32_SCHEMA)
                .build();

        // 创建结构对象
        Struct struct = new Struct(schema);
        Schema schema1 = struct.schema();
        System.out.println(schema1);

        // 设置字段值，包括 null 值
        //struct.put("field1", "value1");
        struct.put("field2", null); // 设置为 null
        struct.put("field3", 3);
        if (struct.get("field1") == null){
            System.out.println("null");
        } // true

        // 输出结构
        System.out.println(struct);
    }
}
