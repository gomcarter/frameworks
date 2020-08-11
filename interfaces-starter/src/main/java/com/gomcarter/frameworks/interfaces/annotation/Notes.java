package com.gomcarter.frameworks.interfaces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gomcarter 2019-12-02 09:23:09
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER,
        ElementType.TYPE_PARAMETER, ElementType.TYPE_USE, ElementType.TYPE,
        ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE,
        ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Notes {

    /**
     * content
     *
     * @return content
     */
    String value() default "";

    /**
     * null or not
     *
     * @return true then not null
     */
    boolean notNull() default false;

    /**
     * 1，固定值 —— value，生成固定的value值，可以是数字，字符串，日期字符串，json字符串等，如：100，2020-09-10 20:20:00，{foo:'bar',number:1}， [1，2，3]
     * <br>
     * <br>
     * 2，限定值 —— 从限定的几个值中选一个或者几个，写法：{element1,element2,element3,...,elementN} —— 从elementN中任意选几length个元素出来作为mock值，多个值逗号隔开
     * <br>
     * 举例：
     * <br>
     * <blockquote>
     * {@code @Notes(mock="{1,2,3}") —— 1，2，3中任意选一个值；}
     * </blockquote>
     * <br>
     * <blockquote>
     * {@code @Notes(mock="{aa,bb,cc}", length = 2) —— aa，bb, cc中任意选两个值；}
     * </blockquote>
     * <br>
     * 3，范围值 —— 仅支持数字、日期类型，从一个范围内随机生成一个值，写法：(min, max)  —— min表示最小值，max表示最大值
     * <br>
     * 举例：
     * <blockquote>
     * {@code @Notes(mock="(1,100)") —— 从1到100，随机生成1个数字 }
     * </blockquote>
     * <blockquote>
     * {@code @Notes(mock="(100,999)", length=10) —— 从100到999，随机生成10个数字；}
     * </blockquote>
     * <br>
     * 4，完全随机 —— 完全随机或者按照类中字段规则生成，写法：*
     * <br>
     * 举例：
     * <br>
     * <blockquote>
     * {@code @Notes(mock="*", length=10) —— 生成10个随机值，如果是数字则随机数字，日期就随机日期，字符串就生成长度为10的字符串}
     * </blockquote>
     * 如果是给某个类标记，则表示生成N个这个类，但里面的具体值按照类中各自字段的规则生成；
     * <br>
     * 如果给某个简单类标记，则完全按照这个类的类型来随机生成，如果是数字则随机数字，如果是日期就随机日期
     */
    String mock() default "";

    /**
     * mock数据的长度，仅对数组，列表，集合等数据有效
     */
    int length() default 1;
}
