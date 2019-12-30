package com.gomcarter.frameworks.base.controller;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyEditorSupport;


/**
 * 解决js注入
 */
public class StringEscapeEditor extends PropertyEditorSupport {
    public StringEscapeEditor() {
        super();
    }

    @Override
    public void setAsText(String text) {
        if (text == null) {
            setValue(null);
        } else {
            setValue(StringUtils.replaceEach(text, new String[]{"<", ">"}, new String[]{"&lt;", "&gt;"}));
        }
    }
}
