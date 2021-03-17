package com.gomcarter.frameworks.mybatis.utils;

import com.gomcarter.frameworks.mybatis.annotation.Condition;
import com.gomcarter.frameworks.mybatis.annotation.MatchType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * @author gomcarter
 */
@Data
@Accessors(chain = true)
public class Demo {

    @Condition(fixedValue = "xxTable.id")
    private String id;

    private String name = "aaaa";

    @Condition(type = MatchType.LIKE)
    private String like = "asdlk";

    @Condition(type = MatchType.GE)
    private String ge = "10";

    @Condition(type = MatchType.IN)
    private List<Long> idList = Arrays.asList(1L, 2L, 3L, 4L, 5L);

    @Condition(type = MatchType.IN)
    private List<String> stringList = Arrays.asList("1L", "2L", "3L", "4L");

    @Condition(type = MatchType.IN)
    private Long[] idArrays = new Long[]{2L, 3L, 4L, 5L};
}
