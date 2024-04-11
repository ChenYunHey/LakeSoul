package org.apache.flink.lakesoul.entry;

import com.ververica.cdc.common.annotation.Experimental;
import com.ververica.cdc.common.annotation.Internal;
import com.ververica.cdc.common.annotation.PublicEvolving;
import com.ververica.cdc.connectors.base.config.SourceConfig;
import com.ververica.cdc.connectors.base.source.IncrementalSource;
import com.ververica.cdc.connectors.base.source.meta.split.SourceRecords;
import com.ververica.cdc.connectors.base.source.meta.split.SourceSplitState;
import com.ververica.cdc.connectors.base.source.metrics.SourceReaderMetrics;
import com.ververica.cdc.connectors.mongodb.source.MongoDBSource;
import com.ververica.cdc.connectors.mongodb.source.MongoDBSourceBuilder;
import com.ververica.cdc.connectors.mongodb.source.config.MongoDBSourceConfig;
import com.ververica.cdc.connectors.mongodb.source.config.MongoDBSourceConfigFactory;
import com.ververica.cdc.connectors.mongodb.source.dialect.MongoDBDialect;
import com.ververica.cdc.connectors.mongodb.source.offset.ChangeStreamOffsetFactory;
import com.ververica.cdc.connectors.mongodb.source.reader.MongoDBRecordEmitter;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.connector.base.source.reader.RecordEmitter;

@Experimental
@Internal
class MongoDBSourceWithSchema<T> extends IncrementalSource<T, MongoDBSourceConfig> {
    private static final long serialVersionUID = 1L;
    MongoDBSourceWithSchema(MongoDBSourceConfigFactory configFactory, DebeziumDeserializationSchema<T> deserializationSchema) {
        super(configFactory, deserializationSchema, new ChangeStreamOffsetFactory(), new MongoDBDialect());
    }
    @PublicEvolving
    public static <T> MongoDBSourceBuilder<T> builder() {
        return new MongoDBSourceBuilder();
    }

    protected RecordEmitter<SourceRecords, T, SourceSplitState> createRecordEmitter(SourceConfig sourceConfig, SourceReaderMetrics sourceReaderMetrics) {
        System.out.println("llllll");
        return new MongoDBRecordEmitter(this.deserializationSchema, sourceReaderMetrics, this.offsetFactory);
    }
}
//class MysqlSourceWithSchema<T> extends MongoDBSource<T> {
//    public MysqlSourceWithSchema(MongoDBSourceConfigFactory configFactory, DebeziumDeserializationSchema<T> deserializationSchema) {
//        super(configFactory, deserializationSchema);
//    }
//}