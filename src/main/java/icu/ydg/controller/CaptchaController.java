package icu.ydg.controller;

import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.VerifyParamRegexConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.service.UserService;
import icu.ydg.utils.JsonUtils;
import icu.ydg.utils.RegexUtils;
import icu.ydg.utils.captcha.EmailCaptchaUtils;
import icu.ydg.utils.redis.RedisUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码接口
 *
 * @author 袁德光
 * @date 2024/01/05
 */
@Slf4j
//@Api(tags = "验证码模块接口")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserService userService;

    /**
     * 发送邮件验证码
     *
     * @param email 收件人的电子邮件地址
     * @return 包含操作结果的ApiResponse对象
     * @throws BusinessException 如果电子邮件格式不正确或者发送验证码的操作失败
     */
    @PostMapping("/sendEmailCaptcha")
    @ApiOperation(value = "发送邮件验证码")
    public ApiResponse<String> sendEmailCaptcha(@RequestParam String email) {
        // 参数正则表达式校验
        if (!RegexUtils.matches(VerifyParamRegexConstant.EMAIL, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        String key = RedisKeyConstant.EMAIL_CAPTCHA_KEY + ":" + email;
        // 查看redis是否有缓存验证码
        String captcha = (String) redisUtils.get(key);
        String result = "请勿重复发送验证码，请使用之前发送的验证码";
        // 如果没有缓存验证码
        if (captcha == null) {
            // 随机生成六位数验证码
            captcha = String.valueOf(new Random().nextInt(900000) + 100000);
            // 发送邮件
            result = EmailCaptchaUtils.getEmailCaptcha(email, captcha);
            // 存入redis中
            captcha = JsonUtils.objToJson(captcha);
            redisUtils.set(key, captcha, EmailCaptchaUtils.expireTime, TimeUnit.MINUTES);
        }
        log.info("{}的邮箱验证码为：{}", email, captcha);
        // 返回结果
        return ApiResult.success(null, result);
    }

}