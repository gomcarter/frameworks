package com.gomcarter.frameworks.memory.tool;

import java.util.HashMap;
import java.util.Map;

public class MemoryCacheManager {
    private static class Data<T> {
        private long ttl;
        private T data;

        public Data(T data, long ttl) {
            this.data = data;
            this.ttl = System.currentTimeMillis() + ttl;
        }
    }

    private static Map<String, Data> DATA_CACHE_MAP = new HashMap<>();

    public static <T> void set(String key, T data) {
        set(key, data, 999999999L);
    }

    public static <T> void set(String key, T data, long ttl) {
        DATA_CACHE_MAP.put(key, new Data<>(data, ttl));
    }

    public static <T> T get(String key) {
        Data<T> data = DATA_CACHE_MAP.get(key);
        // 过期时间 ttl > 大于当前时间
        if (data != null && data.ttl > System.currentTimeMillis()) {
            return data.data;
        }
        return null;
    }

    public static boolean lock(String key, int ttl) {
        Object data = get(key);
        if (data != null) {
            return false;
        }
        set(key, true, ttl);
        return true;
    }

    public static void del(String key) {
        DATA_CACHE_MAP.remove(key);
    }
}
