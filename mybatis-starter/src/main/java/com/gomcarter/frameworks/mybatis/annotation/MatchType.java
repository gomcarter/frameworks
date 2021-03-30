package com.gomcarter.frameworks.mybatis.annotation;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.gomcarter.frameworks.config.mapper.JsonMapper;
import com.gomcarter.frameworks.mybatis.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.Collection;

/**
 * 前提标记的字段本身不能 null，为 null 则跳过这个条件
 */
public enum MatchType {
    /**
     * where table_column = #{condition}
     */
    EQ {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).eq(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" = ").append(condition.fixedValue());
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" = #{").append(javaFieldName).append("}")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" = ").append(fixed ? fieldValue : JsonMapper.buildNonNullMapper().toJson(fieldValue));
        }
    },
    /**
     * where table_column &lt;&gt; #{condition}
     */
    NE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).ne(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" &lt;&gt; ").append(condition.fixedValue());
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NOT NULL")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &lt;&gt; #{").append(javaFieldName).append("}")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" != ").append(fixed ? fieldValue : JsonMapper.buildNonNullMapper().toJson(fieldValue));
        }
    },
    /**
     * where table_column LIKE concat('%', #{condition}, '%')
     */
    LIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).like(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" LIKE \"%").append(condition.fixedValue()).append("%\"");
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" LIKE concat(\"%\", #{").append(javaFieldName).append("}, \"%\")")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" LIKE \"%").append(fieldValue).append("%\"");
        }
    },
    /**
     * where table_column NOT LIKE concat('%', #{condition}, '%')
     */
    NOTLIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).notLike(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" NOT LIKE \"%").append(condition.fixedValue()).append("%\"");
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NOT NULL")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" NOT LIKE concat(\"%\", #{").append(javaFieldName).append("}, \"%\")")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" NOT LIKE \"%").append(fieldValue).append("%\"");
        }
    },
    /**
     * where table_column LIKE concat(#{condition}, '%')
     */
    RIGHTLIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).likeRight(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" LIKE \"").append(condition.fixedValue()).append("%\"");
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" LIKE concat(#{").append(javaFieldName).append("}, \"%\")")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" LIKE \"").append(fieldValue).append("%\"");
        }
    },
    /**
     * where table_column LIKE concat('%', #{condition})
     */
    LEFTLIKE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).likeLeft(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" LIKE \"%").append(condition.fixedValue()).append("\"");
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" LIKE concat(\"%\", #{").append(javaFieldName).append("})")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" LIKE \"%").append(fieldValue).append("\"");
        }
    },
    /**
     * where table_column &gt; #{condition}
     */
    GT {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).gt(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" &gt; ").append(condition.fixedValue());
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &gt; #{").append(javaFieldName).append("}")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" > ").append(fieldValue);
        }
    },
    /**
     * where table_column &gt;= #{condition}
     */
    GE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).ge(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" &gt;= ").append(condition.fixedValue());
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &gt;= #{").append(javaFieldName).append("}")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" >= ").append(fieldValue);
        }
    },
    /**
     * where table_column &lt; #{condition}
     */
    LT {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).lt(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" &lt; ").append(condition.fixedValue());
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &lt; #{").append(javaFieldName).append("}")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" < ").append(fieldValue);
        }
    },
    /**
     * where table_column &lt;= #{condition}
     */
    LE {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).le(fieldName, value);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" &lt;= ").append(condition.fixedValue());
                    return;
                } else if (condition.strategy() == MatchStrategy.IGNORED) {
                    sql.append("\n<if test=\"").append(javaFieldName).append(" == null\">")
                            .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                            .append("</if>");
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" &lt;= #{").append(javaFieldName).append("}")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" <= ").append(fieldValue);
        }
    },
    /**
     * where table_column is null
     */
    NULL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).isNull(fieldName);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue()) || condition.strategy() == MatchStrategy.IGNORED) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" IS NULL ");
                    return;
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" IS NULL ")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" IS NULL ");
        }
    },
    /**
     * where table_column is not null
     */
    NOTNULL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).isNotNull(fieldName);
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null) {
                if (StringUtils.isNotBlank(condition.fixedValue()) || condition.strategy() == MatchStrategy.IGNORED) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" IS NOT NULL ");
                    return;
                }
            }

            sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                    .append(" AND ").append(databaseFieldName).append(" IS NOT NULL")
                    .append("</if>");
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" IS NOT NULL ");
        }
    },
    /**
     * where table_column in (#{condition}) —— condition 字段必须是 iterable 或者 array，且模板类必须是基础类型
     */
    IN {
        @Override
        public <T> void wrap(Wrapper<T> w, Condition condition, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) w);
            Class kls = value.getClass();
            if (Iterable.class.isAssignableFrom(kls)) {
                wrapper.in(fieldName, (Collection<?>) value);
            } else {
                wrapper.in(fieldName, value);
            }
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                // 设置固定值
                sql.append(" AND ").append(databaseFieldName).append(" IN (").append(condition.fixedValue()).append(")");
            } else if (BeanUtils.isSimpleValueType(fieldClass) || fieldClass == Object.class) {
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
            } else if (fieldClass.isArray()) {
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".length > 0\" >")
                        .append("\n AND ").append(databaseFieldName)
                        .append(" IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            }
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" IN (");

            Class fieldClass = fieldValue.getClass();
            if (Collection.class.isAssignableFrom(fieldClass) || fieldClass.isArray()) {
                String v = JsonMapper.buildNonNullMapper().toJson(fieldValue);
                sql.append(v.substring(0, v.length() - 1).substring(1));
            } else {
                sql.append(fixed ? fieldValue : JsonMapper.buildNonNullMapper().toJson(fieldValue));
            }

            sql.append(")");
        }
    },
    /**
     * where table_column not in (#{condition}) —— #{condition} 必须是 iterable 或者 array，且模板类必须是基础类型
     */
    NOTIN {
        @Override
        public <T> void wrap(Wrapper<T> w, Condition condition, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) w);
            Class kls = value.getClass();
            if (Iterable.class.isAssignableFrom(kls)) {
                wrapper.notIn(fieldName, (Collection<?>) value);
            } else {
                wrapper.notIn(fieldName, value);
            }
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                // 设置固定值
                sql.append(" AND ").append(databaseFieldName).append(" NOT IN (").append(condition.fixedValue()).append(")");
            } else if (BeanUtils.isSimpleValueType(fieldClass) || fieldClass == Object.class) {
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
            } else if (fieldClass.isArray()) {
                sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".length > 0\" >")
                        .append("\n AND ").append(databaseFieldName)
                        .append(" NOT IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                        .append("#{_item_}")
                        .append("</foreach>\n")
                        .append("</if>");
            }
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            sql.append(" AND ").append(fieldName).append(" NOT IN (");

            Class fieldClass = fieldValue.getClass();
            if (Collection.class.isAssignableFrom(fieldClass) || fieldClass.isArray()) {
                String v = JsonMapper.buildNonNullMapper().toJson(fieldValue);
                sql.append(v.substring(0, v.length() - 1).substring(1));
            } else {
                sql.append(fixed ? fieldValue : JsonMapper.buildNonNullMapper().toJson(fieldValue));
            }

            sql.append(")");
        }
    },
    /**
     * where table_column in (#{condition}) —— condition是一个 sqlTemplate 语句， 如： select id from table where id &lt; 3
     * <p>
     * 尽量不要使用这个，有 sql注入 的风险
     */
    INSQL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).inSql(fieldName, value + "");
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                // 设置固定值
                sql.append(" AND ").append(databaseFieldName).append(" IN (").append(condition.fixedValue()).append(")");
            } else {
                sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                        .append(" AND ").append(databaseFieldName).append(" IN (#{").append(javaFieldName).append("})")
                        .append("</if>");
            }
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            throw new RuntimeException("not support inSql mode");
        }
    },
    /**
     * where table_column not in (#{condition}) —— condition是一个 sqlTemplate 语句： select name from table where id &lt; 3
     * <p>
     * 尽量不要使用这个，有 sql注入 的风险
     */
    NOTINSQL {
        @Override
        public <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value) {
            ((AbstractWrapper) wrapper).notInSql(fieldName, value + "");
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                // 设置固定值
                sql.append(" AND ").append(databaseFieldName).append(" NOT IN (").append(condition.fixedValue()).append(")");
            } else {
                sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                        .append(" AND ").append(databaseFieldName).append(" NOT IN (#{").append(javaFieldName).append("})")
                        .append("</if>");
            }
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            throw new RuntimeException("not support notInSql mode");
        }
    },
    /**
     * 自定义可以不标注，默认就是 and
     * 当此字段是一个自定义类： where (XX = XX AND YY = YY OR ZZ = ZZ) and other ———— XX,YY,ZZ 是此字段类里面的字段
     */
    AND {
        @Override
        public <T> void wrap(Wrapper<T> wr, Condition condition, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) wr);

            Class kls = value.getClass();

            if (BeanUtils.isSimpleValueType(kls) || kls == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 or 了
                wrapper.and(w -> ((AbstractWrapper) w).eq(fieldName, value));
            } else if (kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
                // 如果是数组或者是Iterable
                wrapper.and(w -> IN.wrap(((AbstractWrapper) w), condition, fieldName, value));
            } else {
                // 否则去 and 这个类对应下面的所有字段的匹配结果
                wrapper.and(w -> MapperUtils.buildWrapper(((AbstractWrapper) w), value));
            }
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (BeanUtils.isSimpleValueType(fieldClass)) {
                // 判断 kls 是否是基本类型，如果是则直接 and 了
                EQ.sqlTemplate(sql, condition, databaseFieldName, javaFieldName, fieldClass);
            } else if (fieldClass.isArray() || Collection.class.isAssignableFrom(fieldClass)) {
                // 如果是数组或者是Iterable
                IN.sqlTemplate(sql, condition, databaseFieldName, javaFieldName, fieldClass);
            } else {
                if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" AND ").append(databaseFieldName).append(" = ").append(condition.fixedValue());
                } else {
                    // 否则去 and 这个类对应下面的所有字段的匹配结果
                    sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                            .append(" AND ( 1 = 1");

                    MapperUtils.buildWhereSql(sql, condition, javaFieldName, fieldClass);

                    sql.append(")</if>");
                }
            }
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            Class fieldClass = fieldValue.getClass();
            if (BeanUtils.isSimpleValueType(fieldClass)) {
                // 判断 kls 是否是基本类型，如果是则直接 and 了
                EQ.whereSqlForExists(sql, fieldName, fieldValue, fixed);
            } else if (fieldClass.isArray() || Collection.class.isAssignableFrom(fieldClass)) {
                // 如果是数组或者是Iterable
                IN.whereSqlForExists(sql, fieldName, fieldValue, fixed);
            } else {
                // 否则去 and 这个类对应下面的所有字段的匹配结果
                sql.append(" AND (");

                MapperUtils.buildExistsSql(sql, fieldValue);

                sql.append(")");
            }
        }
    },
    /**
     * 当此字段是基础类型： where table_column = #{condition} or other
     * 当此字段是一个自定义类：where (XX = XX AND YY == YY OR ZZ = ZZ) or other ———— XX,YY,ZZ 是此字段类里面的字段
     */
    OR {
        @Override
        public <T> void wrap(Wrapper<T> wr, Condition condition, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) wr);

            Class kls = value.getClass();

            if (BeanUtils.isSimpleValueType(kls) || kls == Object.class) {
                // 判断 kls 是否是基本类型，如果是则直接 or 了
                wrapper.or(w -> ((AbstractWrapper) w).eq(fieldName, value));
            } else if (kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
                // 如果是数组或者是Iterable
                wrapper.or(w -> IN.wrap(((AbstractWrapper) w), condition, fieldName, value));
            } else {
                // 否则去 or 这个类对应下面的所有字段的匹配结果
                wrapper.or(w -> MapperUtils.buildWrapper(((AbstractWrapper) w), value));
            }
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            if (BeanUtils.isSimpleValueType(fieldClass)) {
                if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" OR ").append(databaseFieldName).append(" = ").append(condition.fixedValue());
                } else {
                    // 判断 kls 是否是基本类型，如果是则直接 or 了
                    sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                            .append(" OR ").append(databaseFieldName).append(" = #{").append(javaFieldName).append("}")
                            .append("</if>");
                }
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" OR ").append(databaseFieldName).append(" IN (").append(condition.fixedValue()).append(")");
                } else {
                    // 如果是数组或者是Iterable
                    sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".size > 0\" >")
                            .append("\n OR ").append(databaseFieldName)
                            .append(" IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                            .append("#{_item_}")
                            .append("</foreach>\n")
                            .append("</if>");
                }
            } else if (fieldClass.isArray()) {
                if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" OR ").append(databaseFieldName).append(" IN (").append(condition.fixedValue()).append(")");
                } else {
                    sql.append("<if test=\"").append(javaFieldName).append(" != null and ").append(javaFieldName).append(".length > 0\" >")
                            .append("\n OR ").append(databaseFieldName)
                            .append(" IN <foreach collection=\"").append(javaFieldName).append("\"  item=\"_item_\" open=\"(\" close=\")\" separator=\",\" >")
                            .append("#{_item_}")
                            .append("</foreach>\n")
                            .append("</if>");
                }
            } else {
                if (condition != null && StringUtils.isNotBlank(condition.fixedValue())) {
                    // 设置固定值
                    sql.append(" OR ").append(databaseFieldName).append(" = ").append(condition.fixedValue());
                } else {
                    // 否则去 and 这个类对应下面的所有字段的匹配结果
                    sql.append("<if test=\"").append(javaFieldName).append(" != null\">")
                            .append("\n OR ( 1 = 1");

                    MapperUtils.buildWhereSql(sql, condition, javaFieldName, fieldClass);

                    sql.append(")\n</if>");
                }
            }
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            Class fieldClass = fieldValue.getClass();
            if (BeanUtils.isSimpleValueType(fieldClass)) {
                // 设置固定值
                sql.append(" OR ").append(fieldName).append(" = ").append(fixed ? fieldValue : JsonMapper.buildNonNullMapper().toJson(fieldValue));
            } else if (Collection.class.isAssignableFrom(fieldClass) || fieldClass.isArray()) {
                String v = JsonMapper.buildNonNullMapper().toJson(fieldValue);
                sql.append(" OR ").append(fieldName).append(" IN (").append(v.substring(0, v.length() - 1).substring(1)).append(")");
            } else {
                // 否则去 and 这个类对应下面的所有字段的匹配结果
                sql.append(" OR (");

                MapperUtils.buildExistsSql(sql, fieldValue);

                sql.append(")");
            }
        }
    },
    EXISTS {
        @Override
        public <T> void wrap(Wrapper<T> wr, Condition condition, String fieldName, Object value) {
            AbstractWrapper wrapper = ((AbstractWrapper) wr);
            Class kls = value.getClass();
            if (BeanUtils.isSimpleValueType(kls) || kls == Object.class || kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
                // 如果是数组或者是Iterable
                throw new RuntimeException("EXISTS 不支持类型为：" + kls + "的字段：" + fieldName + "，仅支持普通自定义类。");
            }
            StringBuilder whereSql = new StringBuilder();

            String[] relations = condition.relation();
            if (relations.length % 2 != 0 || relations.length == 0) {
                throw new RuntimeException("EXISTS: fieldName：" + fieldName + "的字段：relation: 配置不正确。");
            }
            StringBuilder sql = new StringBuilder("select 1 from " + fieldName + " a where ");

            for(int i = 0; i < relations.length; i = i + 2) {
                sql.append("a.").append(relations[i]).append(" = ").append(relations[i + 1]);
            }
            MapperUtils.buildExistsSql(whereSql, value);
            if (whereSql.length() > 0) {
                sql.append(whereSql.toString());
            }
            wrapper.exists(sql.toString());
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            // do nothing
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            throw new RuntimeException("not support nesting of exists");
        }
    },
    NOTEXISTS {
        @Override
        public <T> void wrap(Wrapper<T> wr, Condition condition, String fieldName, Object value) {
            if (value != null) {
                Class kls = value.getClass();
                if (BeanUtils.isSimpleValueType(kls) || kls == Object.class || kls.isArray() || Iterable.class.isAssignableFrom(kls)) {
                    // 如果是数组或者是Iterable
                    throw new RuntimeException("NOT EXISTS 不支持类型为：" + kls + "的字段：" + fieldName + "，仅支持普通自定义类。");
                }
                String[] relations = condition.relation();
                if (relations.length % 2 != 0 || relations.length == 0) {
                    throw new RuntimeException("NOT EXISTS fieldName：" + fieldName + "的字段：relation: 配置不正确。");
                }
                StringBuilder sql = new StringBuilder("select 1 from " + fieldName + " a where ");
                for(int i = 0; i < relations.length; i = i + 2) {
                    sql.append("a.").append(relations[i]).append(" = ").append(relations[i + 1]);
                }

                StringBuilder whereSql = new StringBuilder();
                MapperUtils.buildExistsSql(whereSql, value);
                if (whereSql.length() > 0) {
                    sql.append(" where 1 = 1 ").append(whereSql.toString());
                }

                ((AbstractWrapper) wr).notExists(sql.toString());
            }
        }

        @Override
        public void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass) {
            // do nothing
        }

        @Override
        public void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed) {
            throw new RuntimeException("not support nesting of not exists");
        }
    };

    public abstract <T> void wrap(Wrapper<T> wrapper, Condition condition, String fieldName, Object value);

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

    public abstract void sqlTemplate(StringBuilder sql, Condition condition, String databaseFieldName, String javaFieldName, Class fieldClass);

    public abstract void whereSqlForExists(StringBuilder sql, String fieldName, Object fieldValue, boolean fixed);
}
