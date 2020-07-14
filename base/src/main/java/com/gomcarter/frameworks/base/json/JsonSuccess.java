package com.gomcarter.frameworks.base.json;

public class JsonSuccess extends JsonObject {

	private Object data;

	public JsonSuccess() {
		this("");
	}

	public JsonSuccess(String msg) {
		this.code = 0;
		this.message = msg;
	}

	public Object getData() {
		return data;
	}

	public JsonSuccess setData(Object data) {
		this.data = data;
		return this;
	}
}
