package com.gomcarter.frameworks.mybatis.annotation;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import org.springframework.beans.BeanUtils;

import java.util.Collection;

/**
 * 前提标记的字段本身不能 null，为 null 则跳过这个条件
 */
public enum MatchType {
    /**
     * where name = name
     */
    EQ {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).eq(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" = #{").append(javaFieldName).append("}")
                    .append("</if>");
        }
    },
    /**
     * where name &lt;&gt; name
     */
    NE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).ne(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &lt;&gt; #{").append(javaFieldName).append("}")
                    .append("</if>");
        }
    },
    /**
     * where name LIKE "%name%"
     */
    LIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).like(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" LIKE concat(\"%\", #{").append(javaFieldName).append("}, \"%\")")
                    .append("</if>");
        }
    },
    /**
     * where name NOT LIKE "%name%"
     */
    NOTLIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).notLike(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" NOT LIKE concat(\"%\", #{").append(javaFieldName).append("}, \"%\")")
                    .append("</if>");
        }
    },
    /**
     * where name LIKE "name%"
     */
    RIGHTLIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).likeRight(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" LIKE concat(#{").append(javaFieldName).append("}, \"%\")")
                    .append("</if>");
        }
    },
    /**
     * where name LIKE "%name"
     */
    LEFTLIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).likeLeft(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" LIKE concat(\"%\", #{").append(javaFieldName).append("})")
                    .append("</if>");
        }
    },
    /**
     * where name &gt; name
     */
    GT {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).gt(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &gt; #{").append(javaFieldName).append("}")
                    .append("</if>");
        }
    },
    /**
     * where name &gt;= name
     */
    GE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).ge(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &gt;= #{").append(javaFieldName).append("}")
                    .append("</if>");
        }
    },
    /**
     * where name &lt; name
     */
    LT {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).lt(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &lt; #{").append(javaFieldName).append("}")
                    .append("</if>");
        }
    },
    /**
     * where name &lt;= name
     */
    LE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).le(fieldName, value);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &lt;= #{").append(javaFieldName).append("}")
                    .append("</if>");
        }
    },
    /**
     * where name is null
     */
    NULL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).isNull(fieldName);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" IS NULL")
                    .append("</if>");
        }
    },
    /**
     * where name is not null
     */
    NOTNULL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).isNotNull(fieldName);
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" IS NOT NULL")
                    .append("</if>");
        }
    },
    /**
     * where name in (...) : name 字段必须是 iterable 或者 array，且模板类必须是基础类型
     * <p>
     * 特别的：而且如果字段本身是字段命名为  nameList，nameSet 那么不需要打此标签，默认就是 in
     */
    IN {
        @Override
        public <T> void wrap(Wrapper<T> w, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) w);
            Class kls = value.getClass();
            if (Iterable.class.isAssignableFrom(kls)) {
                wrapper.in(fieldName, (Collection<?>) value);
            } else {
                wrapper.in(fieldName, value);
            }
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (BeanUtils.isSimpleValueType(fieldClass) || fieldClass == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 in 了
                sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                        .append(" AND ").append(databaseFieldName).append(" IN (#{").append(javaFieldName).append("})")
                        .append("</if>");
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".size > 0\" >")
                        .append("\n AND ").append(databaseFieldName)
                        .append(" IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            } else {
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".length > 0\" >")
                        .append("\n AND ").append(databaseFieldName)
                        .append(" IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            }
        }
    },
    /**
     * where name not in (...) :nameNOTIN 必须是 iterable 或者 array，且模板类必须是基础类型；
     */
    NOTIN {
        @Override
        public <T> void wrap(Wrapper<T> w, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) w);
            Class kls = value.getClass();
            if (Iterable.class.isAssignableFrom(kls)) {
                wrapper.notIn(fieldName, (Collection<?>) value);
            } else {
                wrapper.notIn(fieldName, value);
            }
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (BeanUtils.isSimpleValueType(fieldClass) || fieldClass == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 in 了
                sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                        .append(" AND ").append(databaseFieldName).append(" NOT IN (#{").append(javaFieldName).append("})")
                        .append("</if>");
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".size > 0\" >")
                        .append("\n AND ").append(databaseFieldName)
                        .append(" NOT IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            } else {
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".length > 0\" >")
                        .append("\n AND ").append(databaseFieldName)
                        .append(" NOT IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            }
        }
    },
    /**
     * where name in (...): nameINSQL是一个 sql 语句： select id from table where id &lt; 3
     * <p>
     * 尽量不要使用这个，有 sql注入 的风险
     */
    INSQL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).inSql(fieldName, value + "");
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" IN (#{").append(javaFieldName).append("})")
                    .append("</if>");
        }
    },
    /**
     * where name not in (...): nameINSQL是一个 sql 语句： select name from table where id &lt; 3
     * <p>
     * 尽量不要使用这个，有 sql注入 的风险
     */
    NOTINSQL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).notInSql(fieldName, value + "");
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" NOT IN (#{").append(javaFieldName).append("})")
                    .append("</if>");
        }
    },
    /**
     * 自定义可以不标注，默认就是 and
     * 当此字段是一个自定义类： where (XX = XX AND YY = YY OR ZZ = ZZ) and other ———— XX,YY,ZZ 是此字段类里面的字段
     */
    AND {
        @Override
        public <T> void wrap(Wrapper<T> wr, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) wr);

            Class kls = value.getClass();

            if (BeanUtils.isSimpleValueType(kls) || kls == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 or 了
                wrapper.and(w -> ((AbstractWrapper) w).eq(fieldName, value));
            } else if (kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
                // 如果是数组或者是Iterable
                wrapper.and(w -> IN.wrap(((AbstractWrapper) w), fieldName, value));
            } else {
                // 否则去 and 这个类对应下面的所有字段的匹配结果
                wrapper.and(w -> MapperUtils.buildWrapper(((AbstractWrapper) w), value));
            }
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (BeanUtils.isSimpleValueType(fieldClass) || fieldClass == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 and 了
                EQ.sql(sql, mainTable, databaseFieldName, javaFieldName, fieldClass);
            } else if (fieldClass.isArray() || Collection.class.isAssignableFrom(fieldClass)) {
                // 如果是数组或者是Iterable
                IN.sql(sql, mainTable, databaseFieldName, javaFieldName, fieldClass);
            } else {
                // 否则去 and 这个类对应下面的所有字段的匹配结果
                sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                        .append(" AND ( 1 = 1");

                MapperUtils.buildSql(sql, mainTable, javaFieldName, fieldClass);

                sql.append(")</if>");
            }
        }
    },
    /**
     * 当此字段是基础类型： where name = name or other
     * 当此字段是一个自定义类：where (XX = XX AND YY == YY OR ZZ = ZZ) or other ———— XX,YY,ZZ 是此字段类里面的字段
     */
    OR {
        @Override
        public <T> void wrap(Wrapper<T> wr, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) wr);

            Class kls = value.getClass();

            if (BeanUtils.isSimpleValueType(kls) || kls == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 or 了
                wrapper.or(w -> ((AbstractWrapper) w).eq(fieldName, value));
            } else if (kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
                // 如果是数组或者是Iterable
                wrapper.or(w -> IN.wrap(((AbstractWrapper) w), fieldName, value));
            } else {
                // 否则去 or 这个类对应下面的所有字段的匹配结果
                wrapper.or(w -> MapperUtils.buildWrapper(((AbstractWrapper) w), value));
            }
        }

        @Override
        public void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (BeanUtils.isSimpleValueType(fieldClass) || fieldClass == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 or 了
                sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                        .append(" OR ").append(databaseFieldName).append(" = #{").append(javaFieldName).append("}")
                        .append("</if>");
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                // 如果是数组或者是Iterable
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".size > 0\" >")
                        .append("\n OR ").append(databaseFieldName)
                        .append(" IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            } else if (fieldClass.isArray()) {
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".length > 0\" >")
                        .append("\n OR ").append(databaseFieldName)
                        .append(" IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            } else {
                // 否则去 and 这个类对应下面的所有字段的匹配结果
                sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                        .append("\n OR ( 1 = 1");

                MapperUtils.buildSql(sql, mainTable, javaFieldName, fieldClass);

                sql.append(")\n</if>");
            }
        }
    };

    public abstract <T> void wrap(Wrapper<T> wrapper, String fieldName, Object value);

    public static MatchType getDefaultType(Condition condition, Class kls) {
        MatchType type;
        // 基本类型，默认使用 EQ
        if (BeanUtils.isSimpleValueType(kls) || kls == Object.class) {
            type = EQ;
        } else if (kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
            // 数组或者Iterable默认使用 in
            type = IN;
        } else {
            // 复杂类默认使用 and
            type = AND;
        }

        // 如果字段标记了@Condition，那么取出匹配类型，如果类型是 EQ 那么还是使用getDefaultType的值
        if (condition != null && condition.type() != MatchType.EQ) {
            type = condition.type();
        }

        return type;
    }

    public abstract void sql(StringBuilder sql, String mainTable, String databaseFieldName, String javaFieldName, Class fieldClass);
}
