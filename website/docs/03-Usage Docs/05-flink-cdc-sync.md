# Synchronization of Entire Database into LakeSoul

<!--
SPDX-FileCopyrightText: 2023 LakeSoul Contributors

SPDX-License-Identifier: Apache-2.0
-->

Since version 2.1.0, LakeSoul has introduced Flink CDC Sink, capable of supporting Table API and SQL (single-table) as well as Stream API (multiple tables within a database). Currently, the supported upstream data sources are MySQL (5.6-8.0), Oracle (11, 12, 19, 21), and Postgresql (10-14). The unified entry for data ingestion into the lake is JdbcCdc.
## Main features

In the Stream API, the main functions of LakeSoul Sink are:

* Support real-time CDC synchronization of thousands of tables (different schemas) in the same Flink job, and different tables will be automatically written to the corresponding table names of LakeSoul
* For MySQL and PostgreSQL, LakeSoul supports automatic synchronization of schema changes (DDL) to downstream reads, ensuring compatibility between old and new data. Currently, it supports column addition and deletion as well as increasing precision for numeric types.* Support automatic perception of new tables in the upstream database during operation, and automatic table creation in LakeSoul;
* However, for Oracle, only tables with unchanged schemas are supported for synchronization (adding or deleting columns is not allowed), and it does not support synchronization of new tables.
* Additionally, MySQL and PostgreSQL support automatic detection of newly created tables in the upstream database during runtime, enabling automatic table creation within LakeSoul.
* Provide Flink command line startup entry class, support specifying parameters such as database name, table name blacklist, parallelism, etc.;


## How to use the command line
### 1. Download LakeSoul Flink Jar
It can be downloaded from the LakeSoul Release page: https://github.com/lakesoul-io/LakeSoul/releases/download/vVAR::VERSION/lakesoul-flink-1.20-VAR::VERSION.jar.

The currently supported Flink version is 1.20.

### 2. Start the Flink job

#### 2.1 Add LakeSoul metadata database configuration
Add the following configuration to `$FLINK_HOME/conf/flink-conf.yaml`:
```yaml
containerized.master.env.LAKESOUL_PG_DRIVER: com.lakesoul.shaded.org.postgresql.Driver
containerized.master.env.LAKESOUL_PG_USERNAME: root
containerized.master.env.LAKESOUL_PG_PASSWORD: root
containerized.master.env.LAKESOUL_PG_URL: jdbc:postgresql://localhost:5432/test_lakesoul_meta?stringtype=unspecified
containerized.taskmanager.env.LAKESOUL_PG_DRIVER: com.lakesoul.shaded.org.postgresql.Driver
containerized.taskmanager.env.LAKESOUL_PG_USERNAME: root
containerized.taskmanager.env.LAKESOUL_PG_PASSWORD: root
containerized.taskmanager.env.LAKESOUL_PG_URL: jdbc:postgresql://localhost:5432/test_lakesoul_meta?stringtype=unspecified
```

Note that both the master and taskmanager environment variables need to be set.

:::tip
The connection information, username and password of the Postgres database need to be modified according to the actual deployment.
:::

:::caution
Note that if you use Session mode to start a job, that is, submit the job to Flink Standalone Cluster as a client, `flink run` as a client will not read the above configuration, so you need to configure the environment variables separately, namely:

```bash
export LAKESOUL_PG_DRIVER=com.lakesoul.shaded.org.postgresql.Driver
export LAKESOUL_PG_URL=jdbc:postgresql://localhost:5432/test_lakesoul_meta?stringtype=unspecified
export LAKESOUL_PG_USERNAME=root
export LAKESOUL_PG_PASSWORD=root
```
:::

Description of required parameters:

| Parameter | Meaning                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | Value Description                                                                                          |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| -c | The task runs the main function entry class                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | org.apache.flink.lakesoul.entry.MysqlCdc                                                                   |
| Main package | Task running jar                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | lakesoul-flink-1.20-VAR::VERSION.jar                                                                             |
| --source_db.type | source database type                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | mysql postgres oracle                                                                                      |
| --source_db.host | The address of the source database,When mongodb enters the lake, you need to bring the port.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |                                                                                                            |
| --source_db.port | source database port，This parameter is not required when mongodb enters the lake.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |                                                                                                            |
| --source_db.user | source database username                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |                                                                                                            |
| --source_db.password | Password for source database                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |                                                                                                            |
| --source.parallelism | The parallelism of a single table read task affects the data reading speed. The larger the value, the greater the pressure on source database.                                                                                                                                                                                                                                                                                                                                                                                                                                                             | The parallelism can be adjusted according to the write QPS of source database                              |
| --sink.parallelism | The parallelism of the single-table write task, which is also the number of primary key shards in the LakeSoul table. Affects the landing speed of data entering the lake. The larger the value, the greater the number of small files, which affects the subsequent read performance; the smaller the value, the greater the pressure on the write task, and the greater the possibility of data skew. It can be adjusted according to the data volume of the largest table. It is generally recommended that a degree of parallelism (primary key sharding) manage no more than 10 million rows of data. |
| --warehouse_path | Data storage path prefix (cluster prefix is ​​required for hdfs)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | LakeSoul will write the corresponding table data to the warehouse_path/database_name/table_name/ directory |
| --flink.savepoint | Flink savepoint path (cluster prefix is ​​required for hdfs)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |                                                                                                            |
| --flink.checkpoint | Flink checkpoint path (cluster prefix is ​​required for hdfs)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |                                                                                                            |

Other Flink parameters, such as job manager, task manager CPU, memory, slots, etc., also need to be set according to the specific situation.

Optional parameter description:

| Parameter | Meaning Description | Parameter Filling Format |
|--------------------------------------|----------------------|-----------------------------------------|
| --job.checkpoint_mode | Data synchronization mode, the default is EXACTLY_ONCE | --job.checkpoint_mode AT_LEAST_ONCE |
| --job.checkpoint_interval | Checkpoint storage interval, in ms, default is 10 minutes | --job.checkpoint_interval 1200000 |

For MySQL, the following additional parameters need to be configured

| Parameter                 | Required | Meaning Description    | Parameter Filling Format                                                                  |
|----------------------|----------|------------------------|-------------------------------------------------------------------------------------------|
| --source_db.exclude_tables| optional | A list of data table names that do not need to be synchronized, separated by commas, the default is empty | --source_db.exclude_tables test_1,test_2                                                  |
| --server_time_zone=Asia/Shanghai | optional | MySQL server time zone, Flink side defaults to "Asia/Shanghai" | Refer to [JDK ZoneID 文档](https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html) |



Synchronous mysql job example
For Mysql configuration, please refer to https://ververica.github.io/flink-cdc-connectors/release-2.4/content/connectors/mysql-cdc.html

```bash
./bin/flink run -c org.apache.flink.lakesoul.entry.JdbcCDC \
    lakesoul-flink-1.20-VAR::VERSION.jar \
    --source_db.db_name "testDB" \
    --source_db.user "root" \
    --source.parallelism 1 \
    --source_db.db_type "mysql" \
    --source_db.password "123456" \
    --source_db.host "172.17.0.2" \
    --source_db.port 3306 \
    --sink.parallelism 1 \
    --server_time_zone=Asia/Shanghai
    --warehouse_path s3://bucket/lakesoul/flink/data \
    --flink.checkpoint s3://bucket/lakesoul/flink/checkpoints \
    --flink.savepoint s3://bucket/lakesoul/flink/savepoints
```
For Oracle, the following additional parameters need to be configured

| Parameter                        | Required | Meaning Description              | Parameter Filling Format                                                                  |
|----------------------------------|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| --source_db.schemaList           | require  | The list schema of oracle database                                                                                                                     | --source_db.schemaList schema1,schema2                                                    |
| --source_db.schema_tables        | require  | table name，Use commas to separate multiple tables                                                                                                      | --source_db.schema_tables schema.table1,schema.table2                                     |
| --server_time_zone=Asia/Shanghai | optional | Oracle server time zone, Flink side defaults to "Asia/Shanghai"                                                                                        | Refer to [JDK ZoneID 文档](https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html) |
| --source_db.splitSize            | optional | The split size (number of rows) of table snapshot, captured tables are split into multiple splits when read the snapshot of table.the default is 1024. | --source_db.splitSize 10000                                                               |

Synchronous oracle job example
For oracle configuration,please refer to https://ververica.github.io/flink-cdc-connectors/release-2.4/content/connectors/oracle-cdc.html
```bash
./bin/flink run -c org.apache.flink.lakesoul.entry.JdbcCDC \
    lakesoul-flink-1.20-VAR::VERSION.jar \
    --source_db.db_type oracle \
    --source_db.db_name "testDB" \
    --source_db.user "FLINKUSER" \
    --source.parallelism 1 \
    --sink.parallelism 1 \
    --source_db.password "flinkpw" \
    --source_db.host "172.17.0.2" \
    --source_db.port 1521 \
    --source_db.splitSize 10000 \
    --source_db.schemaList "FLINKUSER" \
    --source_db.schema_tables "FLINKUSER.T1" \
    --job.checkpoint_interval 1000 \
    --server_time_zone=Asia/Shanghai
    --warehouse_path s3://bucket/lakesoul/flink/data \
    --flink.checkpoint s3://bucket/lakesoul/flink/checkpoints \
    --flink.savepoint s3://bucket/lakesoul/flink/savepoints
```

For Postgresql, the following additional parameters need to be configured

| Parameter                 | Required | Meaning Description                                                                                                                                                                                                  | Parameter Filling Format                                                |
|---------------------------|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------|
| --source_db.schemaList    | require  | the schema list of postgres                                                                                                                                                                                          | --source_db.schemaList schema1,schema2          |
| --source_db.schema_tables | require  | table name，Use commas to separate multiple tables                                                                                                                                                                    | --source_db.schema_tables schema.table1,schema.table2 |
| --source_db.splitSize     | optional | The split size (number of rows) of table snapshot, captured tables are split into multiple splits when read the snapshot of table.the default is 1024.                                                               | --source_db.splitSize 10000                     |
| --pluginName              | optional | The name of the Postgres logical decoding plug-in installed on the server. Supported values are decoderbufs, wal2json, wal2json_rds, wal2json_streaming, wal2json_rds_streaming and pgoutput. default is decoderbufs | --pluginName    pgoutput                        |
| --source_db.slot_name     | require  | postgres slot name                                                                                                                                                                                                   | --source_db.slot_name flink                     |

For Postgresql configuration,please refer to  https://ververica.github.io/flink-cdc-connectors/release-2.4/content/connectors/postgres-cdc.html
```bash
./bin/flink run -c org.apache.flink.lakesoul.entry.JdbcCDC \
    lakesoul-flink-1.20-VAR::VERSION.jar \
    --source_db.db_name "postgres" \
    --source_db.user "postgres" \
    --source.parallelism 1 \
    --source_db.schemaList "public" \
    --source_db.db_type "postgres" \
    --source_db.password "123456" \
    --source_db.host "172.17.0.2" \
    --source_db.port 5433 \
    --source_db.splitSize 2 \
    --source_db.schema_tables "public.t1" \
    --source_db.slot_name flink \
    --pluginName "pgoutput" \
    --sink.parallelism 1 \
    --job.checkpoint_interval 1000 \
    --warehouse_path s3://bucket/lakesoul/flink/data \
    --flink.checkpoint s3://bucket/lakesoul/flink/checkpoints \
    --flink.savepoint s3://bucket/lakesoul/flink/savepoints
```

For mongoDB, the following additinal parameters need to be configured

| Parameter   | Required | Meaning Description   | Parameter Filling Format |
|-------------|----------|-----------------------|----------------------|
| --batchSize | optional | Get data size for each batch| --batchSize 1024     |

```bash
./bin/flink run -c org.apache.flink.lakesoul.entry.JdbcCDC \
    lakesoul-flink-2.4.0-flink-1.20-SNAPSHOT.jar \
    --source_db.db_name "cdc" \
    --source_db.user "flinkuser" \
    --source.parallelism 1 \
    --mongodb_database cdc \
    --server_time_zone Asia/Shanghai \
    --source_db.db_type "mongodb" \
    --source_db.password "flinkpw" \
    --source_db.host "172.18.0.2:27017" \
    --source_db.schema_tables "cdc.bincoll" \
    --sink.parallelism 1 \
    --job.checkpoint_interval 1000 \
    --flink.checkpoint "file:/tmp/data/lakesoul/227/" \
    --warehouse_path "file:/home/cyh/data/lakesoul/mongo"
```
## LakeSoul Flink CDC Sink job execution process

In the initialization phase after the LakeSoul Flink job starts, it will first read all the tables in the configured MySQL DB (excluding tables that do not need to be synchronized). For each table, first determine whether it exists in LakeSoul. If it does not exist, a LakeSoul table is automatically created, and its schema is consistent with the corresponding table in MySQL.

After initialization, the CDC Stream of all tables will be read and written to the corresponding LakeSoul tables in Upsert mode.

If a DDL Schema change occurs to a MySQL table during synchronization, the change will also be applied to the corresponding LakeSoul table.

## LakeSoul Flink CDC Sink Exactly Once Semantic Guarantee
LakeSoul Flink CDC Sink automatically saves the relevant state during job running, and can restore and rewrite the state when a Flink job fails, so data will not be lost.

When LakeSoul writes, write idempotency is guaranteed in two parts:
1. When the stage file is Commit, it is consistent with the Flink File Sink. The atomicity of the file system rename operation is used to ensure that the staging file is written to the final path. Because rename is atomic, no duplicate writes or misses occur after a Failover.
2. When the LakeSoul metadata is submitted, the file path will be recorded first, and when the snapshot is updated, the file will be marked as submitted through a transaction. After Failover, by judging whether a file has been submitted, the idempotency of submission can be guaranteed.

In summary, LakeSoul Flink CDC Sink ensures that data is not lost through state recovery, and ensures that data is not duplicated by committing with idempotency, which achieves an exactly once semantic guarantee.

## Type mapping relationship between MySQL and LakeSoul

Since MySQL, Spark, Parquet and other data types are not exactly the same, LakeSoul makes the following mapping relationships during synchronization (types that are not in the table are currently not supported):

|MySQL Type | Spark Type |
|-|-|
|BOOLEAN, BOOL|org.apache.spark.sql.types.DataTypes.BooleanType|
|BIT(1)|org.apache.spark.sql.types.DataTypes.BooleanType|
|BIT(>1)|org.apache.spark.sql.types.DataTypes.BinaryType|
|TINYINT|org.apache.spark.sql.types.DataTypes.IntegerType|
|SMALLINT[(M)]|org.apache.spark.sql.types.DataTypes.IntegerType|
|MEDIUMINT[(M)]|org.apache.spark.sql.types.DataTypes.IntegerType|
|INT, INTEGER[(M)]|org.apache.spark.sql.types.DataTypes.IntegerType|
|BIGINT[(M)]|org.apache.spark.sql.types.DataTypes.LongType|
|REAL[(M,D)]|org.apache.spark.sql.types.DataTypes.FloatType|
|FLOAT[(M,D)]|org.apache.spark.sql.types.DataTypes.DoubleType|
|DOUBLE[(M,D)]|org.apache.spark.sql.types.DataTypes.DoubleType|
|CHAR(M)]|org.apache.spark.sql.types.DataTypes.StringType|
|VARCHAR(M)]|org.apache.spark.sql.types.DataTypes.StringType|
|BINARY(M)]|org.apache.spark.sql.types.DataTypes.BinaryType|
|VARBINARY(M)]|org.apache.spark.sql.types.DataTypes.BinaryType|
|TINYBLOB|org.apache.spark.sql.types.DataTypes.BinaryType|
|TINYTEXT|org.apache.spark.sql.types.DataTypes.StringType|
|BLOB|org.apache.spark.sql.types.DataTypes.BinaryType|
|TEXT|org.apache.spark.sql.types.DataTypes.StringType|
|MEDIUMBLOB|org.apache.spark.sql.types.DataTypes.BinaryType|
|MEDIUMTEXT|org.apache.spark.sql.types.DataTypes.StringType|
|LONGBLOB|org.apache.spark.sql.types.DataTypes.BinaryType|
|LONGTEXT|org.apache.spark.sql.types.DataTypes.StringType|
|JSON|org.apache.spark.sql.types.DataTypes.StringType|
|ENUM|org.apache.spark.sql.types.DataTypes.StringType|
|SET|org.apache.spark.sql.types.DataTypes.StringType|
|YEAR[(2\|4)]|org.apache.spark.sql.types.DataTypes.IntegerType|
|TIMESTAMP[(M)]|org.apache.spark.sql.types.DataTypes.TimestampType|
|DATE|org.apache.spark.sql.types.DataTypes.DateType|
|TIME[(M)]|org.apache.spark.sql.types.DataTypes.LongType|
|DATETIME[(M)]|org.apache.spark.sql.types.DataTypes.TimestampType|
|NUMERIC[(M[,D])],DECIMAL[(M[,D])]|_if decimal.handling.mode=precise_ <br />&emsp;org.apache.spark.sql.types.DecimalType(M,D) <br />if _decimal.handling.mode=string_<br />   &emsp;                 org.apache.spark.sql.types.DataTypes.StringType<br />_if decimal.handling.mode=doulbe_ <br />&emsp;org.apache.spark.sql.types.DataTypes.DoubleType|
|GEOMETRY, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON, GEOMETRYCOLLECTION, POINT|Not currently supported|

Types in Spark, type names in Spark SQL, you can find the corresponding relationship in the [Spark Data Types](https://spark.apache.org/docs/latest/sql-ref-datatypes.html) document.

## Data type matching
Type mapping relationship between Postgres and LakeSoul

| SMALLINT INT2 SMALLSERIALSERIAL2                                                      | org.apache.spark.sql.types.DataTypes.IntegerType                                                                                                                                                                                                                                           | 
|---------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| INTEGER   <br/>SERIAL                                                                 | org.apache.spark.sql.types.DataTypes.IntegerType                                                                                                                                                                                                                                           |
| BIGINT   <br/>BIGSERIAL                                                               | org.apache.spark.sql.types.DataTypes.LongType                                                                                                                                                                                                                                              |
| REAL   <br/>FLOAT4  <br/>FLOAT8   <br/>DOUBLE  <br/>PRECISION                         | org.apache.spark.sql.types.DataTypes.DoubleType                                                                                                                                                                                                                                            |
| NUMERIC(p, s)  <br/>DECIMAL(p, s)                                                     | if decimal.handling.mode=precise  <br/>    org.apache.spark.sql.types.DecimalType(M,D)  <br/> if decimal.handling.mode=string   <br/>    org.apache.spark.sql.types.DataTypes.StringType   <br/> if decimal.handling.mode=doulbe   <br/>   org.apache.spark.sql.types.DataTypes.DoubleType |
| BOOLEAN                                                                               | org.apache.spark.sql.types.DataTypes.BooleanType                                                                                                                                                                                                                                           |
| DATE                                                                                  | org.apache.spark.sql.types.DataTypes.DateType                                                                                                                                                                                                                                              |
| TIME [(p)] [WITHOUT TIMEZONE]                                                         | org.apache.spark.sql.types.DataTypes.LongType                                                                                                                                                                                                                                              |
| TIMESTAMP [(p)] [WITHOUT TIMEZONE]                                                    | org.apache.spark.sql.types.DataTypes.TimestampType                                                                                                                                                                                                                                         |
| CHAR(n)  <br/> CHARACTER(n)  <br/>  VARCHAR(n)  <br/> CHARACTER VARYING(n)  <br/>TEXT |  org.apache.spark.sql.types.DataTypes.StringType                                                                                                                                                                                                                                                                                          |
| BYTEA                                                                                 |org.apache.spark.sql.types.DataTypes.BinaryType                                                                                                                                                                                                                                                                                                                                           |

Type mapping relationship between Oracle and LakeSoul

| SMALLINT INT2 SMALLSERIALSERIAL2                                                                                                    | org.apache.spark.sql.types.DataTypes.IntegerType                                                                                                                                                                          | 
|-------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NUMBER(p, s )                                                                                                                       | if decimal.handling.mode=precise  <br/>  org.apache.spark.sql.types.DecimalType(M,D)  <br/>if decimal.handling.mode=string  <br/>  org.apache.spark.sql.types.DataTypes.StringType  <br/>if decimal.handling.mode=doulbe  <br/> org.apache.spark.sql.types.DataTypes.DoubleType |
| FLOAT  <br/> BINARY_FLOAT                                                                                                           | org.apache.spark.sql.types.DataTypes.DoubleType                                                                                                                                                                                                                                      |
| DOUBLE PRECISION  <br/> BINARY_DOUBLE                                                                                               | org.apache.spark.sql.types.DataTypes.DoubleType                                                                                                                                                                                                                                                                                     |
| NUMBER(1)                                                                                                                           | org.apache.spark.sql.types.DataTypes.BooleanType                                                                                                                                                                                                                                                                                                                                    |
| DATE                                                                                                                                |  org.apache.spark.sql.types.DataTypes.DateType                                                                                                                                                                                                                                                                                                                                                                                   |
| TIMESTAMP [(p)] WITH TIME ZONE                                                                                                      |  org.apache.spark.sql.types.DataTypes.TimestampType                                                                                                                                                                                                                                                                                                                                                                                                                                |
| CHAR(n)  <br/>NCHAR(n)  <br/>NVARCHAR2(n)  <br/>VARCHAR(n)  <br/>VARCHAR2(n)  <br/>CLOB  <br/>NCLOB  <br/>XMLType  <br/>SYS.XMLTYPE | org.apache.spark.sql.types.DataTypes.StringType                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| BLOB                                                                                                                                |  org.apache.spark.sql.types.DataTypes.BinaryType                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

Type mapping relationship between Oracle and Lakesoul

| STRING      | org.apache.spark.sql.types.DataTypes.StringTypes | 
|-------------|----------------------------------------|
| DOUBLE      | org.apache.spark.sql.types.DataTypes.DoubleType |
| INTEGER     | org.apache.spark.sql.types.DataTypes.Integer |
| BOOLEAN     | org.apache.spark.sql.types.DataTypes.DoubleType |
| DATE        | org.apache.spark.sql.types.DataTypes.DateType |
| ARRAYS      | org.apache.spark.sql.types.ArrayType   |
| BINARY DATA | org.apache.spark.sql.types.DataTypes.BinaryTYpe |
| LONG        | org.apache.spark.sql.types.DataTypes.LongType |
| STRUCT      | org.apache.spark.sql.types.StructField |
| DECIMAL     |  org.apache.spark.sql.types.DecimalType(M,D)                                      |


## Precautions
1. A table in MySQL must have a primary key, and tables without a primary key are currently not supported;
2. The DDL change currently supports adding a column at the end, or deleting a column in the middle; the default value of a new column currently only supports `null`, and LakeSoul will automatically add a `null` value to the column when reading old data; the deleted column , LakeSoul will automatically filter this column when reading;
3. The TIME type in MySQL corresponds to the LongType type in LakeSoul, as there is no TIME data type in Spark and Debezium resolves the TIME type to the current value in microseconds from 00:00:00. Therefore, this is consistent with Debezium;
4. The TIMESTAMP and DATETIME types in MySQL are stored as UTC TIME ZOME values in LakeSoul to avoid time zone resolution issues; When reading, you just need to specify the time zone and it can be parsed according to the specified time zone. So it is necessary to correctly fill in the server_time_zone parameter when starting the FLINK CDC task.
5. Postgres needs to set wal_level = logical
6. In order to obtain complete Update event information in Postgres, you need to execute: alter table tablename replica identity full.
7. Oracle needs to enable incremental logging for synchronized tables: ALTER TABLE inventory.customers ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;
8. When transferring tables from Oracle to lakesoul, avoid using schema.* to transfer multiple tables.