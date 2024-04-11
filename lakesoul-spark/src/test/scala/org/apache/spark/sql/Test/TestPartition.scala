package org.apache.spark.sql.Test

object TestPartition {
  def main(args: Array[String]): Unit = {

    import org.apache.spark.sql._
    val spark = SparkSession.builder.master("local")
      .config("spark.sql.extensions", "com.dmetasoul.lakesoul.sql.LakeSoulSparkSessionExtension")
      // 使用 SQL 功能还需要增加以下两个配置项
      .config("spark.sql.catalog.lakesoul", "org.apache.spark.sql.lakesoul.catalog.LakeSoulCatalog")
      .config("spark.sql.defaultCatalog", "lakesoul")
      .getOrCreate()
    import spark.implicits._

    val df = Seq(("2021-01-01",1,"rice"),("2021-01-01",2,"bread")).toDF("date","id","name")
    val tablePath = "/tmp/table/name1"

    //create table
    //spark batch
    df.write
      .mode("append")
      .format("lakesoul")
      .option("rangePartitions","date")
      .option("hashPartitions","name")
      .option("hashBucketNum","2")
      .option("shortTableName","t471")
      .save(tablePath)
  }
}
