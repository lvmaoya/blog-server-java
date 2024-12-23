package com.lvmaoya.blog.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class BeanCopyUtil {
    public static <O,T> T copyBean(O source, Class<T> clazz) {
        T o = null;
        try {
            o = clazz.newInstance();
            BeanUtils.copyProperties(source, o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return o;
    }

    public static <O,T> List<T> copyBeanList(List<O> list, Class<T> clazz) {
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }

}
