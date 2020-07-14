package com.gomcarter.frameworks.base.json;

public abstract class JsonObject {

    protected Integer code = 0;
    protected String message = "";

    public JsonObject() {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return System.currentTimeMillis();
    }
}
