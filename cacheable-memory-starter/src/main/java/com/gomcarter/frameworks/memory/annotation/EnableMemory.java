package com.gomcarter.frameworks.memory.annotation;

import com.gomcarter.frameworks.memory.MemoryRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 临时方案，作为redis等缓存方案的初期方案
 * @author gomcarter on 2019-09-05 16:00:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MemoryRegistrar.class)
public @interface EnableMemory {
}
