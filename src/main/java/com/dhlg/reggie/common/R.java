package com.dhlg.reggie.common;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回结果类
 *
 * 需要响应前端的数据
 *
 */
@Data
public class R<T>{

    private Integer code;//编码:1成功 0和其他数表示失败

    private String msg;//错误信息

    private T data;//数据

    private Map map = new HashMap();//动态数据


    /**
     * @param object
     * @return
     * @param <T>
     *
     * 登录成功方法
     *
     */
    public static <T> R<T> success(T object){
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }


    /**
     *
     * 登录失败方法
     *
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> R<T> error(String msg){
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }


    public R<T> add(String key,Object value){
        this.map.put(key,value);
        return this;
    }





}
