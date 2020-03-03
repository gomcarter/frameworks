package com.gomcarter.frameworks.mybatis.datasource;


/**
 * <pre>
 * 读/写动态数据库 决策者
 * 根据DataSourceType是write/read 来决定是使用读/写数据库
 * 通过ThreadLocal绑定实现选择功能
 * </pre>
 *
 * @author Zhang Kaitao
 */
public class ReadWriteDataSourceDecision {

    public enum DataSourceType {
        /**
         * write to be the write database
         */
        write,
        /**
         * read to be the read database
         */
        read
    }


    private static final ThreadLocal<DataSourceType> DATASOURCE_TYPE_HOLDER = new ThreadLocal<>();

    public static void markWrite() {
        DATASOURCE_TYPE_HOLDER.set(DataSourceType.write);
    }

    public static void markRead() {
        DATASOURCE_TYPE_HOLDER.set(DataSourceType.read);
    }

    public static boolean unmarked() {
        return DATASOURCE_TYPE_HOLDER.get() == null;
    }

    public static void reset() {
        DATASOURCE_TYPE_HOLDER.remove();
    }

    public static boolean isChoiceNone() {
        return null == DATASOURCE_TYPE_HOLDER.get();
    }

    public static boolean isChoiceWrite() {
        return DataSourceType.write == DATASOURCE_TYPE_HOLDER.get();
    }

    public static boolean isChoiceRead() {
        return DataSourceType.read == DATASOURCE_TYPE_HOLDER.get();
    }

}
