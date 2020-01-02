package com.acs.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataProcessControllerTest {
    private Logger logger = LogManager.getLogger(DataProcessControllerTest.class);

    @Test
    public void fileUploadHandler() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //测试类型转换
        //String nowDate = formatter.format(Long.parseLong(String.valueOf(new Date().getTime())));
        String nowDate = formatter.format(new Date().getTime());
        logger.info(nowDate);
    }
}