package com.gomcarter.frameworks.base.streaming;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author gomcarter
 */
public class Groupable<KEY, VAL> {
    private Map<KEY, VAL> map;

    public static <KEY, VAL> Groupable<KEY, VAL> of(Map<KEY, VAL> map) {
        return new Groupable<>(map);
    }

    private Groupable(Map<KEY, VAL> map) {
        if (map == null) {
            this.map = new HashMap<>();
        } else {
            this.map = map;
        }
    }

    public <OTHER> Groupable join(Groupable<KEY, OTHER> other) {
        Map<KEY, VAL> map = this.map;
        Map<KEY, OTHER> otherMap = other.map;
        return of(new HashMap<KEY, Pair<VAL, OTHER>>(this.map.size()) {{
            map.forEach((k, m) -> Optional.ofNullable(otherMap.get(k)).ifPresent(s -> put(k, Pair.of(m, s))));
        }});
    }

    public <OTHER> Groupable leftOuterJoin(Groupable<KEY, OTHER> other) {
        Map<KEY, VAL> map = this.map;
        Map<KEY, OTHER> otherMap = other.map;
        return of(new HashMap<KEY, Pair<VAL, OTHER>>(this.map.size()) {{
            map.forEach((k, m) -> put(k, Pair.of(m, otherMap.get(k))));
        }});
    }

    public <RESULT> Streamable<RESULT> map(BiFunction<KEY, VAL, RESULT> mapper) {
        return Streamable.valueOf(
                this.map.keySet()
                        .stream()
                        .map(s -> mapper.apply(s, map.get(s)))
        );
    }

    public Map<KEY, VAL> collect() {
        return map;
    }
}
