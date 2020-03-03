package com.gomcarter.frameworks.xmlexcel.utils;


import com.gomcarter.frameworks.base.common.CustomDateUtils;
import com.gomcarter.frameworks.xmlexcel.config.Header;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 生成csv文件
 *
 * @author gomcarter 2017年12月2日 08:10:35
 */
public class CsvUtils {

    private static final String BOM_TAG;
    private static final String SPLIT = "\t";

    static {
        BOM_TAG = new String(new byte[]{(byte) -17, (byte) -69, (byte) -65}, Charset.defaultCharset());
    }

    /**
     * @param savePath savePath
     * @param headers  headers
     * @param cells    cells
     * @throws IOException IOException
     */
    public static void appendCsv(String savePath, List<Header> headers, List<Map<String, Object>> cells) throws IOException {
        File file = new File(savePath);

        FileUtils.writeStringToFile(file, generateBody(headers, cells), Charset.defaultCharset(), true);
    }

    private static String generateBody(List<Header> headers, List<Map<String, Object>> dataList) {
        /*如果没有headers， 那么直接在excel里面写每行数据*/
        if (headers == null || headers.size() == 0) {
            return generateBodyWithoutHeaders(dataList);
        }
        /*存在headers，那么在每行数据里面找到对应header的内容 写入*/
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> data : dataList) {
            for (Header header : headers) {
                sb.append(formatData(data.get(header.getName()))).append(SPLIT);
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    private static String generateBodyWithoutHeaders(List<Map<String, Object>> dataList) {
        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> data : dataList) {
            sb.append(generateRow(data.values()));
        }
        return sb.toString();
    }

    private static String generateRow(Collection<?> elements) {
        StringBuilder sb = new StringBuilder();
        for (Object data : elements) {
            Object d = formatData(data);
            if (d != null) {
                sb.append(d.toString());
            }
            sb.append(SPLIT);
        }
        return sb.append("\r\n").toString();
    }

    private static Object formatData(Object data) {
        if (data != null) {
            if (data instanceof Date) {
                return CustomDateUtils.toString((Date) data);
            }
            if (data instanceof String) {
                return StringUtils.replaceEach((String) data, new String[]{"\t"}, new String[]{""});
            }
        }
        return data;
    }

    /**
     * @param savePath savePath
     * @param headers  headers
     * @throws IOException IOException
     */
    public static void createCsv(String savePath, List<Header> headers) throws IOException {
        File file = new File(savePath);
        StringBuilder sb = new StringBuilder();
        for (Header h : headers) {
            sb.append(h.getName()).append(SPLIT);
        }

        sb.append("\r\n");

        FileUtils.writeStringToFile(file, BOM_TAG + sb.toString(), Charset.defaultCharset());
    }
}
