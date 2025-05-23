package icu.ydg.model.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import icu.ydg.constant.UserConstant;
import icu.ydg.model.domain.User;
import icu.ydg.service.UserService;
import icu.ydg.utils.SpringBeanContextUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 通用字段自动处理程序（创建人和更新人）
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Component
public class CommonFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        UserService userService = SpringBeanContextUtils.getBeanByClass(UserService.class);
        Object flag = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        long createBy = 1L;
        long updateBy = 1L;
        if (flag != null) {
            User user = userService.getLoginUser(request);
            createBy = user.getId();
            updateBy = user.getId();
        }
        this.setFieldValByName("createBy", createBy, metaObject);
        this.setFieldValByName("updateBy", updateBy, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        UserService userService = SpringBeanContextUtils.getBeanByClass(UserService.class);
        Object flag = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        long updateBy = 1L;
        if (flag != null) {
            User user = userService.getLoginUser(request);
            updateBy = user.getId();
        }
        this.setFieldValByName("updateBy", updateBy, metaObject);
    }
}
