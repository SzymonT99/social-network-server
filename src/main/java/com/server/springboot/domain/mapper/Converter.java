package com.server.springboot.domain.mapper;

@FunctionalInterface
public interface Converter<T, F> {
    T convert(F from);
}
