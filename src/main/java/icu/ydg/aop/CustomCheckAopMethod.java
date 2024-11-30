package icu.ydg.aop;

import icu.ydg.annotation.Verify;
import icu.ydg.common.ErrorCode;
import icu.ydg.exception.BusinessException;
import icu.ydg.model.domain.User;
import icu.ydg.model.enums.user.UserRoleEnums;
import icu.ydg.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 自定义检查拦截器 AOP
 * 该类用于拦截带有 @Verify 注解的方法，执行参数和身份验证的校验逻辑。
 * 支持对方法参数的非空、长度、正则表达式等进行校验，以及对身份的权限验证。
 *
 * @author 袁德光
 * @date 2023/12/30
 */
@Slf4j
@Component("CustomCheckAopMethod")
public class CustomCheckAopMethod {

    @Resource
    private UserService userService;

    /**
     * 拦截器方法，用于执行参数和身份验证检查。
     *
     * @param joinPoint 被拦截方法的信息。
     * @throws BusinessException 如果验证失败，抛出 BusinessException 异常。
     */
    public void interceptor(JoinPoint joinPoint) throws BusinessException {
        try {
            // 方法的目标对象
            Object[] arguments = joinPoint.getArgs();
            // 被拦截方法的名称
            // 获取方法签名
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            // 获取目标类的方法
            Method method = methodSignature.getMethod();
            // 检查是否存在 @Verify 注解并且需要进行参数验证
            Verify checkAnnotation = method.getAnnotation(Verify.class);
            // 获取身份验证角色
            String authRole = checkAnnotation != null ? checkAnnotation.checkAuth() : "";
            // 如果指定了身份验证角色
            if (StringUtils.isNotBlank(authRole)) {
                // 执行身份验证
                checkAuth(authRole);
            }
        } catch (BusinessException e) {
            // 处理 BusinessException，记录日志并重新抛出异常
            handleException(joinPoint, e);
        } catch (Exception e) {
            // 处理一般异常，创建 SYSTEM_ERROR BusinessException 并重新抛出异常
            handleException(joinPoint, new BusinessException(ErrorCode.SYSTEM_ERROR));
        } catch (Throwable e) {
            // 捕获其他 Throwable 并作为 RuntimeException 重新抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查权限身份
     *
     * @param checkAuth 身份(user/admin)
     * @throws BusinessException 业务异常
     */
    private void checkAuth(String checkAuth) throws BusinessException {
        // 获取当前请求的 HttpServletRequest
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 检查用户是否具有必要的权限
        if (StringUtils.isNotBlank(checkAuth)) {
            UserRoleEnums mustUserRoleEnums = UserRoleEnums.getEnumByValue(checkAuth);
            if (mustUserRoleEnums == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前无权限查看该内容！");
            }
            String userRole = loginUser.getUserRole();
            // 如果用户被封号，直接拒绝
            if (UserRoleEnums.BAN.equals(mustUserRoleEnums)) {
                throw new BusinessException(ErrorCode.BAN_ERROR, "该账号已被封号，请联系管理员解封！");
            }
            // 如果需要管理员权限，但用户不是管理员，拒绝
            if (UserRoleEnums.ADMIN.equals(mustUserRoleEnums)) {
                if (!checkAuth.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你当前无权限查看该内容！");
                }
            }
        }
    }

    /**
     * 处理异常的私有方法，记录异常信息并重新抛出 BusinessException。
     *
     * @param joinPoint 切面信息，用于获取方法签名和参数。
     * @param e         异常对象，捕获的 BusinessException。
     * @throws BusinessException 重新抛出异常。
     */
    private void handleException(JoinPoint joinPoint, BusinessException e) throws BusinessException {
        // 记录异常信息，包括方法签名、参数和异常堆栈
        log.error("拦截到异常 - 方法: {}, 参数: {}", joinPoint.getSignature().toShortString(), Arrays.toString(joinPoint.getArgs()), e);
        throw e;
    }

}