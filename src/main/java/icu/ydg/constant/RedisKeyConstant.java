package icu.ydg.constant;

/**
 * redis key 常量
 *
 * @author 袁德光
 * @date 2024/01/03
 */
public interface RedisKeyConstant {

    /**
     * 邮箱验证码key
     */
    String EMAIL_CAPTCHA_KEY = "email_captcha_";

    /**
     * 用于邮箱重置密码的验证码凭证
     */
    String VOUCHER = "voucher:";

    /**
     * 用户登录状态缓存
     */
    String USER_LOGIN_STATE_CACHE = "user_login_cache:";

    String RECOMMEND_USER = "recommend_users:";
    String MESSAGE_POST_PRAISE_NUM_KEY = "message_post_praise_num:";
    String MESSAGE_POST_COMMENT_NUM_KEY = "message_post_comment_num:";
    String MESSAGE_FRIEND_APPLY_NUM_KEY = "message_friend_apply_num:";
    String MESSAGE_FANS_NUM_KEY = "message_fans_num:";
    String CACHE_CHAT_PRIVATE = "chat_private:";
    String CACHE_CHAT_HALL = "chat_hall:";
}
