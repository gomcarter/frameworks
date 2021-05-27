package com.gomcarter.frameworks.xmlexcel.upload;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.SyncReadListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.gomcarter.frameworks.base.exception.CustomException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author gaopeng 2021/5/19
 */
public class CustomSyncReadListener extends SyncReadListener {
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException e = (ExcelDataConvertException) exception;

            String colName = "";
            List<String> names = e.getExcelContentProperty().getHead().getHeadNameList();
            if (CollectionUtils.isNotEmpty(names)) {
                colName = names.get(0);
            }
            String msg = String.format("第%d行第%d列[%s]格式不正确",
                    e.getRowIndex(),
                    e.getColumnIndex() + 1,
                    colName);

            throw new CustomException(msg);
        }

        throw exception;
    }
}
