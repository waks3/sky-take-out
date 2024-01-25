package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * AOP,自定义切面类，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     * 这个通知只不过就是给那些公共字段赋值而已
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint)
    {
        log.info("开始进行公共字段自动填充...");
        //获取到当前被拦截的方法上的数据库操作类型，比如说INSERT和UPDATE两个方法它们需要进行的操作是不同的
        MethodSignature signature=(MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill=signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType=autoFill.value();//获得数据库操作类型

        //获取到当前被拦截的方法的参数——实体对象
        Object[] args= joinPoint.getArgs();//这里获取的实体对象可能会多个，就是方法的形参可能会有多个
        if(args==null || args.length==0) return;//进行特殊判断，如果为空那么不用进行下面的操作了
        Object entity=args[0];//这是我们做的一个约定，目标实体对象都会默认放在最前面那个

        //准备赋值的数据
        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType==OperationType.INSERT)
        {
            //为4个公共字段赋值
            try{
                //这些其实都可以看成是在定义一个方法，将方法中需要用到的参数属性给交代清楚，然后后续再惊醒调用
                Method setCreateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                //用这个拉屎举个例子就是，我们需要更改CreateTime的值，所以我们调用set函数，器赋值的类型为LocalDateTime……
                Method setCreateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                //通过反射为对象属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            //为2个公共字段赋值
            try{
                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
