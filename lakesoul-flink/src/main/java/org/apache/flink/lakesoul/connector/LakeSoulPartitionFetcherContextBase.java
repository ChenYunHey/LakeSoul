// SPDX-FileCopyrightText: 2023 LakeSoul Contributors
//
// SPDX-License-Identifier: Apache-2.0

package org.apache.flink.lakesoul.connector;

import com.dmetasoul.lakesoul.meta.DBManager;
import com.dmetasoul.lakesoul.meta.DBUtil;
import com.dmetasoul.lakesoul.meta.DataFileInfo;
import com.dmetasoul.lakesoul.meta.DataOperation;
import com.dmetasoul.lakesoul.meta.entity.PartitionInfo;
import com.dmetasoul.lakesoul.meta.entity.TableInfo;
import org.apache.flink.connector.file.table.PartitionFetcher;
import org.apache.flink.lakesoul.tool.FlinkUtil;
import org.apache.flink.lakesoul.types.TableId;
import org.apache.flink.shaded.guava31.com.google.common.base.Splitter;
import org.apache.flink.table.utils.PartitionPathUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.dmetasoul.lakesoul.meta.DBConfig.LAKESOUL_RANGE_PARTITION_SPLITTER;


/**
 * Base class for table partition fetcher context.
 */
public abstract class LakeSoulPartitionFetcherContextBase<P> implements PartitionFetcher.Context<P> {

    private static final long serialVersionUID = -1154378756544412521L;
    protected final List<String> partitionKeys;
    protected final String partitionOrderKeys;
    protected TableId tableId;
    protected transient DBManager dbManager;

    public LakeSoulPartitionFetcherContextBase(TableId tableId, List<String> partitionKeys, String partitionOrderKeys) {
        this.tableId = tableId;
        this.partitionKeys = partitionKeys;
        this.partitionOrderKeys = partitionOrderKeys;
    }

    private static List<String> extractPartitionValues(String partitionName) {
        return PartitionPathUtils.extractPartitionValues(new org.apache.flink.core.fs.Path(partitionName));
    }

    public TableId getTableId() {
        return tableId;
    }

    /**
     * open the resources of the Context, this method should first call before call other
     * methods.
     */
    @Override
    public void open() throws Exception {
        dbManager = new DBManager();
    }

    private DataFileInfo[] getTargetDataFileInfo(TableInfo tableInfo) throws Exception {
        return FlinkUtil.getTargetDataFileInfo(tableInfo, null);
    }

    /**
     * Get list that contains partition with comparable object.
     *
     * <p>For 'create-time' and 'partition-time',the comparable object type is Long which
     * represents time in milliseconds, for 'partition-name', the comparable object type is
     * String which represents the partition names string.
     */
    @Override
    public List<ComparablePartitionValue> getComparablePartitionValueList() throws Exception {
        List<ComparablePartitionValue> partitionValueList = new ArrayList<>();
        TableInfo tableInfo =
                DataOperation.dbManager().getTableInfoByNameAndNamespace(tableId.table(), tableId.schema());
        List<String> partitionDescs = this.dbManager.getTableAllPartitionDesc(tableInfo.getTableId());
        for (String partitionDesc : partitionDescs) {
            partitionValueList.add(getComparablePartitionByName(partitionDesc));
        }
        return partitionValueList;
    }

    private ComparablePartitionValue<List<String>, String> getComparablePartitionByName(String partitionDesc) {
        return new ComparablePartitionValue<List<String>, String>() {
            private static final long serialVersionUID = 1L;
            // TODO: delete the option
            private static final long simpleLatestPartition = 1L;

            @Override
            public List<String> getPartitionValue() {
                return extractPartitionValues(partitionDesc.replaceAll(LAKESOUL_RANGE_PARTITION_SPLITTER, "/"));
            }

            @Override
            public String getComparator() {
                // order by partition-order-key name in alphabetical order
                // partition-order-key name format: pt_year=2020,pt_month=10,pt_day=14
                // TODO: update logic here
                if (simpleLatestPartition == 1) {
                    StringBuilder comparator = new StringBuilder();
                    // if partitionOrderKeys is null, default to use all partitionKeys to sort
                    Set<String> partitionOrderKeySet = partitionOrderKeys == null ? new HashSet<>(partitionKeys) :
                            new HashSet<>(
                                    Splitter.on(LAKESOUL_RANGE_PARTITION_SPLITTER).splitToList(partitionOrderKeys));
                    Map<String, String> parDescMap = DBUtil.parsePartitionDesc(partitionDesc);
                    // construct a comparator according to the order in which partitionOrderKeys appear in partitionKeys
                    partitionKeys.forEach(partitionKey -> {
                        if (partitionOrderKeySet.contains(partitionKey)) {
                            comparator.append(partitionKey + "=" + parDescMap.get(partitionKey) +
                                    LAKESOUL_RANGE_PARTITION_SPLITTER);
                        }
                    });
                    comparator.deleteCharAt(comparator.length() - 1);
                    return comparator.toString();
                } else {
                    return String.join(LAKESOUL_RANGE_PARTITION_SPLITTER, getPartitionValue());
                }
            }

        };
    }

    /**
     * close the resources of the Context, this method should call when the context do not need
     * any more.
     */
    @Override
    public void close() throws Exception {
    }
}
