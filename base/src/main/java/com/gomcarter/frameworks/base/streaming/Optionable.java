package com.gomcarter.frameworks.base.streaming;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author gomcarter
 */
public class Optionable<T> {

    private Optional<T> that;

    public static <T> Optionable<T> of(T o) {
        return new Optionable<>(Optional.ofNullable(o));
    }


    private Optionable(Optional<T> t) {
        this.that = t;
    }

    public <R> R map(Function<T, R> mapper) {
        return this.that.map(mapper).orElse(null);
    }
}
