package com.gomcarter.frameworks.base.json;

public class JsonError extends JsonObject {

    private Object data;

    public JsonError() {
    }

    public JsonError(String msg) {
        this(msg, -1);
    }

    public JsonError(ErrorCode errorCode) {
    	this(errorCode.getMsg(), errorCode.getCode());
    }

    public JsonError(String msg, Integer code) {
        if (code == null) {
            code = -1;
        }
        this.code = code;
        this.message = msg;
    }

    public Object getData() {
        return data;
    }

    public JsonError setData(Object data) {
        this.data = data;
        return this;
    }
}
