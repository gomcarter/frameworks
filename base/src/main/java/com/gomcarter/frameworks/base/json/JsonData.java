package com.gomcarter.frameworks.base.json;

public class JsonData extends JsonObject {

	private Object data;

	public JsonData() {
		this(null, null);
	}

	public JsonData(Object data) {
        this(data, null);
	}

	public JsonData(Object data, String msg) {
        this.data =  data;
        this.message = msg;
    }

	public Object getData() {
		return data;
	}

	public JsonData setData(Object data) {
		this.data = data;
		return this;
	}
}
