package com.gomcarter.frameworks.base.pager;


import java.io.Serializable;

/**
 * @author gomcarter 2017年12月2日 08:10:35
 */
public interface Pageable extends Serializable {

    /**
     * 翻页到 pageNo 页
     *
     * @param pageNo pageNo
     * @return this
     */
    Pageable turnPage(int pageNo);

    /**
     * limit #{startNum} #{pageCount}
     *
     * @return 获取翻页起始项（不是起始页！！！）
     */
    int getStartNum();

    /**
     * limit #{startNum} #{pageCount}
     *
     * @return 获取每页大小
     */
    int getPageCount();

    /**
     * order by ${orderColumn} ${orderType}
     *
     * @return 获取排序字段名； 多个排序用 逗号 隔开
     */
    String getOrderColumn();

    /**
     * order by ${orderColumn} ${orderType}
     *
     * @return 获取排序类型，一般为desc，或者as； 多个排序用 逗号 隔开
     */
    String getOrderType();
}
