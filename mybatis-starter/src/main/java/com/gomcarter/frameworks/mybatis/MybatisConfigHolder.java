package com.gomcarter.frameworks.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * @author gomcarter on 2019-11-09 23:31:48
 */
public class MybatisConfigHolder {
    /**
     * 数据库类型
     */
    public static DbType DB_TYPE = null;
    public static String DAO_XML_PATH = null;
    public static String[] DAO_BASE_PACKAGE = null;
    public static String TRANSACTION_POINTCUT_EXPRESSION = null;
    public static String[] TRANSACTION_REQUIRED_NAME_MAP = null;
}
