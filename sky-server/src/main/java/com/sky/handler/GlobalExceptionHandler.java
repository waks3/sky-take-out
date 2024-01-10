package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获sql异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        //Duplicate entry 'zhansan' for key 'employee.idx_username'
        //获取异常
        String message=ex.getMessage();
        //查询捕获的异常中是否出现了sql异常
        if(message.contains("Duplicate entry"))
        {
            //先将捕获的异常去除空格
            String[] split=message.split(" ");
            //获取异常中第三位的字符串，那个字符串即为用户输入的用户名
            String username=split[2];
            String msg=username+MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }
        else return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
