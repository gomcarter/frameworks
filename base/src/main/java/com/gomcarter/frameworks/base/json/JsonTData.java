package com.gomcarter.frameworks.base.json;

public class JsonTData<T> extends JsonObject {

    private T data;

    public JsonTData() {

    }

    public JsonTData(T data) {
        this.code = 0;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public JsonTData setData(T data) {
        this.data = data;
        return this;
    }
}
