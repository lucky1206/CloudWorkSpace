package com.acs.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sort4JSONArrayUtil {
    public static JSONArray JsonArraySort(JSONArray jsonArr){
        if(jsonArr != null && jsonArr.size() > 0){
            List<JSONObject> list = new ArrayList<JSONObject>();
            JSONObject jsonObj = null;
            for (int i = 0; i < jsonArr.size(); i++) {
                jsonObj = (JSONObject)jsonArr.get(i);
                list.add(jsonObj);
            }
            Collections.sort(list,new MyComparator());
            jsonArr.clear();
            for (int i = 0; i < list.size(); i++) {
                jsonObj = list.get(i);
                jsonArr.add(jsonObj);
            }
        }

        return jsonArr;
    }
}
 class MyComparator implements Comparator<JSONObject> {

    @Override
    public int compare(JSONObject o1, JSONObject o2) {
        String key1 = o1.getString("reqIndex");
        String key2 = o2.getString("reqIndex");
        int int1 = Integer.parseInt(key1);
        int int2 = Integer.parseInt(key2);
        return int1-int2;
    }
}
