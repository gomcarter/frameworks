package com.gomcarter.frameworks.interfaces.dto;


import com.gomcarter.frameworks.interfaces.annotation.Notes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gomcarter 2019-12-02 09:23:09
 */
public class ApiBean {
    @Notes("name of field")
    private String key;

    @Notes("@Notes(notNull=true/false)，means the field should be null or not null")
    private boolean notNull = false;

    @Notes("the interface has body params？")
    private boolean body = false;

    @Notes("this default value of this field")
    private Object defaults;

    @Notes("this mock value")
    private String mock;

    @Notes("this mock value")
    private Integer mockLength;

    @Notes("comment for this field @Notes(value='****') ")
    private String comment;

    @Notes("the data type of this field")
    private String type;

    @Notes("the class name of this field")
    private String className;

    @Notes("children，only for this field is a POJO or Iterable")
    private List<ApiBean> children;

    public String getKey() {
        return key;
    }

    public ApiBean setKey(String key) {
        this.key = key;
        return this;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public ApiBean setNotNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }

    public Object getDefaults() {
        return defaults;
    }

    public ApiBean setDefaults(Object defaults) {
        this.defaults = defaults;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public ApiBean setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getType() {
        return type;
    }

    public ApiBean setType(String type) {
        this.type = type;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public ApiBean setClassName(String className) {
        this.className = className;
        return this;
    }

    public List<ApiBean> getChildren() {
        return children;
    }

    public ApiBean setChildren(List<ApiBean> children) {
        this.children = children;
        return this;
    }

    public ApiBean addChild(ApiBean child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        return this;
    }

    public boolean isBody() {
        return body;
    }

    public ApiBean setBody(boolean body) {
        this.body = body;
        return this;
    }

    public String getMock() {
        return mock;
    }

    public ApiBean setMock(String mock) {
        this.mock = mock;
        return this;
    }

    public Integer getMockLength() {
        return mockLength == null ? 1 : mockLength;
    }

    public ApiBean setMockLength(Integer mockLength) {
        this.mockLength = mockLength;
        return this;
    }
}
