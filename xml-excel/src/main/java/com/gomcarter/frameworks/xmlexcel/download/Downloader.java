//package com.gomcarter.frameworks.xmlexcel.download;
//
//import com.google.common.collect.Lists;
//import java.io.File;
//import java.io.OutputStream;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
//import java.util.function.Consumer;
//import javax.servlet.http.HttpServletRequest;
//import org.apache.commons.io.FileUtils;
//import org.apache.shiro.crypto.hash.Md5Hash;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class Downloader {
//    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private Map<String, Downloadable> downloaderMap;
//    private String appName;
//    private String downloadDomain;
//    private TgRedisTool redisTool;
//    private StorageType storageType;
//    private final ExecutorService executor;
//    private String rootDir;
//    public static final String DOWNLOAD_TITLES = "downloadTitles";
//    private static String DOWNLOAD_URL_PREFIX = "download_url_prefix_";
//
//    public Downloader() {
//        this.storageType = StorageType.nfs;
//        this.executor = new ThreadPoolExecutor(5, 50, 5L, TimeUnit.MINUTES, new ArrayBlockingQueue(500), Executors.defaultThreadFactory(), new CallerRunsPolicy());
//    }
//
//    public String getRootDir() {
//        if (this.rootDir == null) {
//            switch(EnvConstants.getType()) {
//            case dev:
//            case test:
//            case pre:
//            case online:
//                this.rootDir = "/download/" + this.appName + "/";
//                break;
//            default:
//                this.rootDir = "C://download/" + this.appName + "/";
//            }
//        }
//
//        return this.rootDir;
//    }
//
//    public String getSavePath() {
//        return this.getRootDir() + JPDateUtils.toString(new Date(), "yyyy-MM-dd") + "/";
//    }
//
//    public TaskState check(String key) throws Exception {
//        Boolean has = this._hasProcessingTask(key);
//        if (!has) {
//            String url = this._getDownloadUrl(key);
//            return JPStringUtils.isNotBlank(url) ? new TaskState(State.finish, key, url) : new TaskState(State.nothing, key, (String)null);
//        } else {
//            return new TaskState(State.running, key, (String)null);
//        }
//    }
//
//    public TaskState generate(String key, String filename, Consumer<OutputStream> task) {
//        boolean has = this._hasProcessingTask(key);
//        if (!has) {
//            String savePath = this.getSavePath() + filename;
//            this.doGenerate(key, savePath, task);
//        }
//
//        return new TaskState(State.running, key, (String)null);
//    }
//
//    private void doGenerate(final String key, final String savePath, final Consumer<OutputStream> task) {
//        try {
//            boolean success = this.redisTool.lock(key, State.running.name(), 600L);
//            if (!success) {
//                this.logger.info("key:[" + key + "]发生并发！");
//                return;
//            }
//        } catch (Exception var5) {
//            this.logger.error("key:[" + key + "]调用redis失败！", var5);
//            return;
//        }
//
//        this._deleteDownloadUrl(key);
//        this.executor.submit(new Runnable() {
//            public void run() {
//                try {
//                    task.accept(FileUtils.openOutputStream(new File(savePath)));
//                    Downloader.this._setDownloadUrl(key, savePath);
//                } catch (Exception var10) {
//                    Downloader.this.logger.error("key:[" + key + "]跑任务失败了", var10);
//                } finally {
//                    try {
//                        Downloader.this.redisTool.unlock(key);
//                    } catch (Exception var9) {
//                        Downloader.this.logger.error("key:[" + key + "] 解锁删除失败了", var9);
//                    }
//
//                }
//
//            }
//        });
//    }
//
//    public TaskState generate(Map<String, Object> params, List<DownloaderTitles> titles, String type, String userId, Object... extraData) throws Exception {
//        String key = this._generateKey(type, userId, params);
//        boolean has = this._hasProcessingTask(key);
//        if (!has) {
//            Downloadable downloadable = (Downloadable)this.downloaderMap.get(type);
//            AssertUtils.notNull(downloadable, "未知下载类型 ：【" + type + "】");
//            String filePath = this.getSavePath() + _generateUniqueFileName(downloadable.getFileName());
//            Map<String, Object> cloneParams = new HashMap();
//            if (params != null) {
//                Iterator var11 = params.keySet().iterator();
//
//                while(var11.hasNext()) {
//                    String pKey = (String)var11.next();
//                    if (!JPStringUtils.equals("downloadTitles", pKey)) {
//                        cloneParams.put(pKey, params.get(pKey));
//                    }
//                }
//            }
//
//            this._runTask(key, downloadable, cloneParams, titles, filePath, new Callback() {
//                public void doCmd(Object... o) {
//                    if ((Boolean)o[0]) {
//                    }
//
//                }
//            }, extraData);
//        }
//
//        return new TaskState(State.running, key, (String)null);
//    }
//
//    public static void main(String[] args) {
//        System.out.println(_generateUniqueFileName("lkasflk.xls"));
//        System.out.println(_generateUniqueFileName("纠结啊师傅.xls"));
//        System.out.println(_generateUniqueFileName("啊设立法律身份xls"));
//        System.out.println(_generateUniqueFileName("据啊司法所xls.xls.xls"));
//        System.out.println(_generateUniqueFileName(".xls"));
//        System.out.println(_generateUniqueFileName(".xls.xls"));
//        System.out.println(_generateUniqueFileName(".xls.xl"));
//        System.out.println(_generateUniqueFileName(".xlsxls"));
//    }
//
//    private static String _generateUniqueFileName(String fileName) {
//        if (JPStringUtils.isBlank(fileName)) {
//            return UUID.randomUUID().toString() + ".xls";
//        } else {
//            String suffix = JPStringUtils.getFileSuffix(fileName);
//            if (JPStringUtils.equals(suffix, fileName)) {
//                fileName = fileName + "-" + UUID.randomUUID().toString() + ".xls";
//            } else {
//                fileName = fileName.substring(0, Math.max(fileName.length() - suffix.length() - 1, 0)) + "-" + UUID.randomUUID().toString() + "." + suffix;
//            }
//
//            return fileName;
//        }
//    }
//
//    public TaskState generate(Map<String, Object> params, String type, String userId, Object... extraData) throws Exception {
//        return this.generate(params, JsonMapper.buildNonNullMapper().fromJsonToList(String.valueOf(params.get("downloadTitles")), DownloaderTitles.class), type, userId, extraData);
//    }
//
//    public TaskState generate(HttpServletRequest request, String type, String userId, Object... extraData) throws Exception {
//        return this.generate(ServletUtils.getParametersStartingWith(request, (String)null), type, userId, extraData);
//    }
//
//    private void _runTask(final String key, final Downloadable downloader, final Map<String, Object> params, final List<DownloaderTitles> titles, final String savePath, final Callback callback, final Object... extraData) {
//        try {
//            boolean success = this.redisTool.lock(key, State.running.name(), 600L);
//            if (!success) {
//                this.logger.info("key:[" + key + "]发生并发！");
//                return;
//            }
//        } catch (Exception var9) {
//            this.logger.error("key:[" + key + "]调用redis失败！", var9);
//            return;
//        }
//
//        this._deleteDownloadUrl(key);
//        this.executor.submit(new Runnable() {
//            public void run() {
//                try {
//                    DownloaderUtils.generateFile(downloader, params, titles, savePath, extraData);
//                    Downloader.this._setDownloadUrl(key, savePath);
//                    if (callback != null) {
//                        callback.doCmd(new Object[]{Boolean.TRUE});
//                    }
//                } catch (Exception var10) {
//                    Downloader.this.logger.error("key:[" + key + "]跑任务失败了，参数：" + params, var10);
//                    if (callback != null) {
//                        callback.doCmd(new Object[]{false});
//                    }
//                } finally {
//                    try {
//                        Downloader.this.redisTool.unlock(key);
//                    } catch (Exception var9) {
//                        Downloader.this.logger.error("key:[" + key + "] 解锁删除失败了", var9);
//                    }
//
//                }
//
//            }
//        });
//    }
//
//    private void _setDownloadUrl(String key, String fileName) {
//        this.redisTool.setValue(DOWNLOAD_URL_PREFIX + key, this.storageType.getDownloadUrl(this, fileName), 1L, TimeUnit.DAYS);
//    }
//
//    private String _getDownloadUrl(String key) throws Exception {
//        return this.redisTool.getValue(DOWNLOAD_URL_PREFIX + key);
//    }
//
//    private void _deleteDownloadUrl(String key) {
//        this.redisTool.delValue(DOWNLOAD_URL_PREFIX + key);
//    }
//
//    private String _getTaskState(String key) {
//        return this.redisTool.getValue(key);
//    }
//
//    private boolean _hasProcessingTask(String key) {
//        String state = this._getTaskState(key);
//        return JPStringUtils.isNotBlank(state);
//    }
//
//    private String _generateKey(String type, String userId, Map<String, Object> params) {
//        List<String> paramKeyList = Lists.newArrayList();
//        if (params != null) {
//            Iterator var5 = params.keySet().iterator();
//
//            while(var5.hasNext()) {
//                String key = (String)var5.next();
//                paramKeyList.add(key + String.valueOf(params.get(key)));
//            }
//        }
//
//        Collections.sort(paramKeyList);
//        StringBuilder source = new StringBuilder();
//        if (paramKeyList.size() > 0) {
//            Iterator var9 = paramKeyList.iterator();
//
//            while(var9.hasNext()) {
//                String key = (String)var9.next();
//                source.append(key);
//            }
//        }
//
//        return this.appName + "_" + type + "_" + (new Md5Hash(userId + "_" + source.toString())).toHex();
//    }
//
//    public void setDownloaderMap(Map<String, Downloadable> downloaderMap) {
//        this.downloaderMap = downloaderMap;
//    }
//
//    public void setAppName(String appName) {
//        this.appName = appName;
//    }
//
//    public void setRedisTool(TgRedisTool redisTool) {
//        this.redisTool = redisTool;
//    }
//
//    public void setStorageType(String storageType) {
//    }
//
//    public String getAppName() {
//        return this.appName;
//    }
//
//    public String getDownloadDomain() {
//        return this.downloadDomain;
//    }
//
//    public void setDownloadDomain(String downloadDomain) {
//        this.downloadDomain = downloadDomain;
//    }
//}
