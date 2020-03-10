package com.gomcarter.frameworks.config.annotation;

import java.lang.annotation.*;

/**
 * 先在入口Application加上  {@link EnableValueAutoConfiguration}
 *
 * @author gomcarter on 2019-11-15 17:34:26
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurableValue {

    /**
     * 如果配置为：1  =》   表达式：  找到配置内容的keys （nacos 如：group.dataId）
     * <p>
     * 如果配置内容为： keyXXX=1   =》   表达式：  找到配置内容的keys.内容Key， （nacos：group.dataId.keyXXX）
     * <p>
     * nacos 示例：
     * <blockquote><pre>
     *     public class Foo {
     *         {@code @ConfigurableValue("CONFIG.ITEM.bar")}
     *          private SomeClass value;
     *     }
     * </pre></blockquote>
     * <blockquote><pre>
     *     public class Bar {
     *         {@code @ConfigurableValue("CONFIG.ITEM")}
     *          private SomeClass value;
     *     }
     * </pre></blockquote>
     *
     * @return the key
     */
    String value();

    /**
     * 是否自动更新，当配置中心内容发生变化，对应变量自动更新
     *
     * @return true the value will be refreshed automatic
     */
    boolean autoRefreshed() default false;

    /**
     * @return 默认值
     */
    String defaultValue() default "";
}
