package com.itheima.reggie.common;

/**
 * 自定义业务异常，用于在删除时判断有无关联
 * @Author 星夏
 */
public class RelationException extends RuntimeException{

    public RelationException(String message){
        super(message);
    }

}
