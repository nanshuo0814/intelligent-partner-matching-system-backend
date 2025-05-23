package icu.ydg.aop;

import icu.ydg.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 自定义 AOP 检查参数配置类
 *
 * @author 袁德光
 * @date 2024/03/22
 */
@Slf4j
@Aspect
@Configuration("CustomInterceptorAop")
public class CustomInterceptorAop {

    @Resource
    private CustomCheckAopMethod customCheckAopMethod;

    /**
     * 切入带有 @Verify 注解的方法。
     */
    @Pointcut("@annotation(icu.ydg.annotation.Verify)")
    public void checkPointcut() {
    }

    /**
     * 在带有 @Verify 注解的方法执行前，执行 CheckInterceptorAop 的拦截逻辑。
     *
     * @param joinPoint 切入点对象，包含方法的相关信息。
     * @throws BusinessException 拦截到业务异常时抛出 BusinessException。
     */
    @Before("checkPointcut()")
    public void doCheckInterceptor(JoinPoint joinPoint) throws BusinessException {
        customCheckAopMethod.interceptor(joinPoint);
    }

}