package com.itheima.reggie.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用的返回结果类
 * @param <T>
 */
public class R<T> {
    private Integer code; //响应码：1成功，其他失败
    private String msg; //响应信息
    private T data; //数据

    private Map map = new HashMap();

    public static <T> R<T> success(T object){
        R<T> r = new R<>();
        r.data = object;
        r.code = 1;
        return r;
    }
    public static <T> R<T> error(String msg){
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }
    public R<T> add(String key, Object value){
        this.map.put(key, value);
        return this;
    }
}
