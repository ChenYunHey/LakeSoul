package com.dmetasoul.lakesoul

import org.apache.spark.sql.arrow.DataTypeCastUtils.{CAN_CAST, IS_EQUAL, checkDataTypeEqualOrCanCast}
import org.apache.spark.sql.types._

object TestStructCast {
  def main(args: Array[String]): Unit = {

  }
//  def checkSchemaEqualOrCanCast(source: StructType, target: StructType, partitionKeyList: java.util.List[String], primaryKeyList: java.util.List[String]): (String, Boolean, StructType) = {
//    var mergeStructType = source
//    var isEqual = source.fields.length == target.fields.length
//    var schemaChanged = false
//    for (targetField <- target.fields) {
//      val fieldIndex = source.getFieldIndex(targetField.name)
//      if (fieldIndex.isDefined) {
//        val sourceField = source.fields(fieldIndex.get)
//        val equalOrCanCast = checkDataTypeEqualOrCanCast(sourceField.dataType, targetField.dataType)
//        if (equalOrCanCast != CAN_CAST && equalOrCanCast != IS_EQUAL) return (equalOrCanCast, true, mergeStructType)
//        if (equalOrCanCast != IS_EQUAL) {
//          schemaChanged = true
//          mergeStructType.fields(fieldIndex.get) = targetField
//          if (partitionKeyList.contains(targetField.name))
//            return (s"Datatype Change of Partition Column $targetField is forbidden", schemaChanged, mergeStructType)
//          if (primaryKeyList.contains(targetField.name))
//            return (s"Datatype Change of Primary Key Column $targetField is forbidden", schemaChanged, mergeStructType)
//          isEqual = false
//        }
//      } else {
//        mergeStructType = mergeStructType.add(targetField)
//        schemaChanged = true
//      }
//    }
//    if (isEqual) (IS_EQUAL, schemaChanged, mergeStructType) else (CAN_CAST, schemaChanged, mergeStructType)
//  }

}
