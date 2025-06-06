// SPDX-FileCopyrightText: 2023 LakeSoul Contributors
//
// SPDX-License-Identifier: Apache-2.0

package org.apache.flink.lakesoul.sink.bucket;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.lakesoul.sink.committer.LakeSoulSinkCommitter;
import org.apache.flink.lakesoul.sink.committer.LakeSoulSinkGlobalCommitter;
import org.apache.flink.lakesoul.sink.state.LakeSoulMultiTableSinkCommittable;
import org.apache.flink.lakesoul.sink.state.LakeSoulMultiTableSinkGlobalCommittable;
import org.apache.flink.lakesoul.sink.writer.AbstractLakeSoulMultiTableSinkWriter;
import org.apache.flink.lakesoul.sink.state.LakeSoulWriterBucketState;

import java.io.IOException;
import java.io.Serializable;

/**
 * The base abstract class for the {@link BulkFormatBuilder}.
 */
public abstract class BucketsBuilder<IN, OUT, T extends BucketsBuilder<IN, OUT, T>>
        implements Serializable {

    protected static final long DEFAULT_BUCKET_CHECK_INTERVAL = 60L * 1000L;
    private static final long serialVersionUID = 2009336183670221500L;

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public abstract AbstractLakeSoulMultiTableSinkWriter<IN, OUT> createWriter(final Sink.InitContext context, int subTaskId) throws IOException;

    public abstract LakeSoulSinkCommitter createCommitter() throws IOException;

    public abstract SimpleVersionedSerializer<LakeSoulWriterBucketState> getWriterStateSerializer()
            throws IOException;

    public abstract SimpleVersionedSerializer<LakeSoulMultiTableSinkCommittable> getCommittableSerializer()
            throws IOException;

    public abstract LakeSoulSinkGlobalCommitter createGlobalCommitter();

    public abstract SimpleVersionedSerializer<LakeSoulMultiTableSinkGlobalCommittable> getGlobalCommittableSerializer()
            throws IOException;
}
