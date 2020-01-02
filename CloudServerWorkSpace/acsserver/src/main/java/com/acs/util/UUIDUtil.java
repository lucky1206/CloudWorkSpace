package com.acs.util;

import java.util.UUID;

public class UUIDUtil {
    public UUIDUtil() {
    }

    /**
     * 自动生成32位的UUid，对应数据库的主键id进行插入用。
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println("ss[" + i + "]=====" + getUUID());
        }
    }
}
