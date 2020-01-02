package com.satellite.utils;

import java.io.*;

/**
 * TLE两行根数预处理工具类
 * 补充tle最新版下载地址：https://www.space-track.org/basicspacedata/query/class/tle_latest/ORDINAL/1/EPOCH/%3Enow-30/orderby/NORAD_CAT_ID/format/3le
 */
public class TleUtil {
    public TleUtil() {
    }

    /**
     * 读取文件内容
     *
     * @param filePath
     * @return
     */
    private String readToString(String filePath) {
        String encoding = "UTF-8";
        File file = new File(filePath);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("当前操作系统不支持编码： " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 加载tle数据并入库
     * 注意：了解TLE数据格式说明
     */
    public String[] loadTle(String path) {
        if (!path.equalsIgnoreCase("")) {
              /* StringBuffer sb = new StringBuffer();
               sb.append(readToString(path));
               System.out.println(readToString(path));*/
            String tleStr = readToString(path);
            //格式化数据，采用逗号分隔：首行，第一行，第二行
            tleStr = tleStr.replaceAll("\r", ",").replaceAll("\n", "");
            //System.out.println(tleStr);
            return tleStr.split(",");
        }

        return null;
    }

    public static void main(String[] args) {
    }
}
