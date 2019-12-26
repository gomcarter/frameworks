package com.gomcarter.frameworks.mybatis.annotation;

import java.lang.annotation.*;

/**
 * select xxx from {main} {type} join {target} on {main}.{mainKey} = {target}.{targetKey}
 * <p>
 * 示例：
 * <p>
 * Joinable(main="a", target="b")
 * <p>
 * Joinable(main="b", target="c")
 * <p>
 * Joinable(main="d", target="e")
 * <p>
 * 为：
 * <p>
 * select * from a a inner join  b b on a.id = b.a_id
 * inner join c c on b.id = c.b_id
 * inner join d d on c.id = d.c_id
 *
 * @author gomcarter on 2019-11-09 22:53:32
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Repeatable(Joinables.class)
public @interface Joinable {

    /**
     * @return 主表
     */
    String main();

    /**
     * @return 被连接的表
     */
    String target();

    /**
     * @return join 类型
     */
    JoinType type() default JoinType.INNER;

    /**
     * @return 默认为 main 中的 id
     */
    String mainKey() default "id";

    /**
     * @return 默认为  ${main}_id
     */
    String targetKey() default "";
}
