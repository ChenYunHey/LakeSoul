package org.apache.flink.lakesoul.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecoredFieldsStatus {

    String fieldName;
    String[] fieldNames;
    HashMap<String,Object> map;


    public RecoredFieldsStatus(String fieldName, String[] fieldNames,HashMap<String,Object> map){
        this.fieldName = fieldName;
        this.fieldNames= fieldNames;
        this.map = map;
    }

}
