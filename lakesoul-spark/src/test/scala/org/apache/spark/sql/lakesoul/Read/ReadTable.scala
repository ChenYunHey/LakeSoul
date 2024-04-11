package org.apache.spark.sql.lakesoul.Read

import com.dmetasoul.lakesoul.tables.LakeSoulTable
import org.apache.spark.sql.SparkSession

object ReadTable {
  def main(args: Array[String]): Unit = {

    import org.apache.spark.sql._
    val spark = SparkSession.builder.master("local")
      .config("spark.sql.extensions", "com.dmetasoul.lakesoul.sql.LakeSoulSparkSessionExtension")
      // 使用 SQL 功能还需要增加以下两个配置项
      .config("spark.sql.catalog.lakesoul", "org.apache.spark.sql.lakesoul.catalog.LakeSoulCatalog")
      .config("spark.sql.defaultCatalog", "lakesoul")
      .getOrCreate()
    import spark.implicits._
    val tablePath = "/home/cyh/data/lakesoul/mongo/cdc/c10"
    val df1 = spark.read.format("lakesoul").load(tablePath)
    //df1.printSchema()
    //LakeSoulTable.forPath(tablePath).compaction()
    //LakeSoulTable.forPath(tablePath).dropTable()
    df1.show(30)
//    println(df1.count())
  }

}
