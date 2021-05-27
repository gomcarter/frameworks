package com.gomcarter.frameworks.xmlexcel.download;

import java.util.List;

/**
 * @author gaopeng 2021/2/18
 */
public interface DownloadCallback<T> {

    List<T> execute(DownloadContext context);
}
