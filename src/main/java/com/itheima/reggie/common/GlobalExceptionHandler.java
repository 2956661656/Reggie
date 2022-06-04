package com.itheima.reggie.common;

import com.itheima.reggie.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 * 拦截配置RestController、Controller注解的异常
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理异常
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.error(exception.getLocalizedMessage());
        String message = exception.getMessage();

        if (message.contains("Duplicate entry")){
            //违反唯一约束
            String[] split = message.split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("数据异常，操作失败");
    }

}
