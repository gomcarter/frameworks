package com.gomcarter.frameworks.mybatis.annotation;

/**
 * 匹配策略, 不支持 IN, NOTIN, OR, AND, INSQL, NOTINSQL
 *
 * @author gomcarter
 */
public enum MatchStrategy {

    /**
     * &lt;if test="columnProperty != null"&gt;column=#{columnProperty}&lt;/if&gt;
     */
    NOT_NULL,

    /**
     * &lt;if test="columnProperty != null"&gt;column=#{columnProperty}&lt;/if&gt;
     * &lt;if test="columnProperty == null"&gt;column is null&lt;/if&gt;
     */
    IGNORED
}
