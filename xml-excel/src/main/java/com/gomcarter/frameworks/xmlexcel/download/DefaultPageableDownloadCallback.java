package com.gomcarter.frameworks.xmlexcel.download;

import com.gomcarter.frameworks.base.pager.DefaultPager;
import com.gomcarter.frameworks.base.pager.Pageable;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author gaopeng 2021/3/23
 */
public class DefaultPageableDownloadCallback<T> implements DownloadCallback<T> {

    private final Pageable pageable;

    private final BiFunction<Pageable, DownloadContext, List<T>> dataFunc;

    public DefaultPageableDownloadCallback(BiFunction<Pageable, DownloadContext, List<T>> dataFunc) {
        this(null, null, dataFunc);
    }

    public DefaultPageableDownloadCallback(int page, int rows,
                                           BiFunction<Pageable, DownloadContext, List<T>> dataFunc) {
        this(page, rows, null, null, dataFunc);
    }

    public DefaultPageableDownloadCallback(String orderColumn, String orderType,
                                           BiFunction<Pageable, DownloadContext, List<T>> dataFunc) {
        this(1, 10, orderColumn, orderType, dataFunc);
    }

    public DefaultPageableDownloadCallback(int page, int rows,
                                           String orderColumn, String orderType,
                                           BiFunction<Pageable, DownloadContext, List<T>> dataFunc) {
        this.pageable = new DefaultPager(page, rows, orderColumn, orderType);
        this.dataFunc = dataFunc;
    }

    @Override
    public List<T> execute(DownloadContext context) {
        Integer page = (Integer) context.get("page");
        if (page == null) {
            page = pageable.getStartNum();
        } else {
            page = page + 1;
        }
        context.set("page", page);

        pageable.turnPage(page);

        List<T> data = dataFunc.apply(pageable, context);
        if (CollectionUtils.size(data) < pageable.getPageCount()) {
            context.setFinished();
        }

        return data;
    }
}
