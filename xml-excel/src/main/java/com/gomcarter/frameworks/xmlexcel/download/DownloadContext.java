package com.gomcarter.frameworks.xmlexcel.download;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gaopeng 2021/2/18
 */
public class DownloadContext {

    private Map<String, Object> context = new HashMap<>();

    private boolean finished = false;

    public void set(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public void setFinished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }
}
