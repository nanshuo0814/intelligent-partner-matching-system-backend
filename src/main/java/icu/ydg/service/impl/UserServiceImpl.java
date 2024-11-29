package icu.ydg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.PageConstant;
import icu.ydg.constant.RedisKeyConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.constant.VerifyParamRegexConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.mapper.UserMapper;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.user.*;
import icu.ydg.model.enums.sort.UserSortFieldEnums;
import icu.ydg.model.enums.user.UserRoleEnums;
import icu.ydg.model.vo.user.UserLoginVO;
import icu.ydg.model.vo.user.UserVO;
import icu.ydg.service.TagService;
import icu.ydg.service.UserService;
import icu.ydg.utils.*;
import icu.ydg.utils.redis.RedisUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static icu.ydg.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author 袁德光
 * @date 2023/12/23 16:30:17
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 超级管理员电子邮件
     */
    @Value("${superAdmin.email}")
    private String superAdminEmail;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserMapper userMapper;
    @Resource
    @Lazy
    private TagService tagService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册 Request
     * @return long
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 获取参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 判断参数是否合法
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于3位");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于6位");
        }
        // 确认密码校验
        if (checkPassword != null && !checkPassword.equals(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.eq(User::getUserAccount, userAccount);
            long userAccountId = this.baseMapper.selectCount(qw);
            if (userAccountId > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已被注册,请重新输入一个");
            }
            // MD5加密
            String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
            // 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            // 用户名和账号初始值一样
            user.setUserName(userAccount);
            user.setUserPassword(encryptPassword);
            // 设置默认的简介（UserConstant.DEFAULT_USER_PROFILE）
            user.setUserProfile(UserConstant.DEFAULT_USER_PROFILE);
            // 设置默认的头像（UserConstant.DEFAULT_USER_AVATAR）
            user.setUserAvatar(UserConstant.DEFAULT_USER_AVATAR);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，系统内部错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录Request
     * @return {@code UserLoginVO}
     */
    @Override
    public UserLoginVO userLogin(HttpServletRequest request, UserLoginRequest userLoginRequest) {
        // 获取参数
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 判断参数是否合法
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于3位");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于6位");
        }
        // 查询用户信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号或密码错误");
        }
        if (user.getUserRole().equals(UserConstant.BAN_ROLE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用,请联系管理员解封");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
        if (!encryptPassword.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        // 记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 缓存用户信息
        redisUtils.set(RedisKeyConstant.USER_LOGIN_STATE_CACHE + user.getId(), user);
        // 返回用户登录信息
        return this.getLoginUserVO(user);
    }

    /**
     * 获取登录用户vo
     *
     * @param user 用户
     * @return {@code UserLoginVO}
     */
    @Override
    public UserLoginVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return {@code User}
     */
    @Override


    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录,获取用户信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请登录！");
        }
        // 尝试从缓存redis中通过用户id获取用户信息
        User cachedUser = this.getUserCacheById(user.getId());
        if (cachedUser == null) {
            // 缓存中不存在，从数据库查询
            cachedUser = this.getById(user.getId());
            if (cachedUser != null) {
                // 将用户信息放入缓存
                this.saveUserToCache(cachedUser, request);
            } else {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请登录！");
            }
        }
        // 返回用户信息
        return cachedUser;
    }

    /**
     * 按id获取用户缓存
     *
     * @param userId 用户id
     * @return {@code User}
     */
    @Override
    public User getUserCacheById(Long userId) {
        String cacheKey = RedisKeyConstant.USER_LOGIN_STATE_CACHE + userId;
        return (User) redisUtils.get(cacheKey);
    }

    /**
     * 将用户保存到缓存
     *
     * @param user 用户
     */
    @Override
    public void saveUserToCache(User user, HttpServletRequest request) {
        String cacheKey = RedisKeyConstant.USER_LOGIN_STATE_CACHE + user.getId();
        redisUtils.set(cacheKey, user, UserConstant.USER_CACHE_TIME_OUT, TimeUnit.HOURS);
        // 将用户信息更新写入session
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return {@code Boolean}
     */
    @Override
    public long userLogout(HttpServletRequest request) {
        // 删除缓存
        long id = this.getLoginUser(request).getId();
        redisUtils.del(RedisKeyConstant.USER_LOGIN_STATE_CACHE + id);
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return id;
    }


    /**
     * 验证邮箱是否存在
     */
    @Override
    public boolean validateEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserEmail, email);
        long count = this.baseMapper.selectCount(queryWrapper);
        return count > 0;
    }

    /**
     * 用户密码自行更新
     *
     * @param request                   请求
     * @param userPasswordUpdateRequest 用户密码重置Request
     * @return {@code Boolean}
     */
    @Override
    public long userPasswordUpdateByMyself(HttpServletRequest request, UserPasswordUpdateRequest userPasswordUpdateRequest) {
        // 获取参数
        String oldPassword = userPasswordUpdateRequest.getOldPassword();
        String userPassword = userPasswordUpdateRequest.getNewPassword();
        String checkPassword = userPasswordUpdateRequest.getCheckPassword();
        // 参数判断
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于6位");
        }
        // 判断两次密码是否一致
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次新密码输入不一致");
        }
        // 获取当前用户
        User loginUser = this.getById(this.getLoginUser(request).getId());
        // 判断旧密码是否正确
        String encryptOldPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + oldPassword).getBytes());
        String trueOldPassword = loginUser.getUserPassword();
        if (!encryptOldPassword.equals(trueOldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确旧密码");
        }
        // 修改密码
        String newEncryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
        // 判断新密码与旧密码是否相等
        if (newEncryptPassword.equals(trueOldPassword)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新密码与旧密码不能一样！");
        }
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(newEncryptPassword);
        ThrowUtils.throwIf(!this.updateById(user), ErrorCode.OPERATION_ERROR, "修改密码失败");
        // 更新用户密码
        return loginUser.getId();
    }

    /**
     * 验证电子邮件代码
     *
     * @param email        电子邮件
     * @param emailCaptcha 电子邮件验证码
     * @return boolean
     */
    @Override
    public boolean validateEmailCode(String email, String emailCaptcha) {
        Object trueEmailCaptcha = redisUtils.get(RedisKeyConstant.EMAIL_CAPTCHA_KEY + ":" + email);
        if (ObjectUtils.isEmpty(trueEmailCaptcha)) {
            log.info("验证码已过期或邮箱填写有误：{}", trueEmailCaptcha);
            return false;
        }
        if (!emailCaptcha.equals(trueEmailCaptcha.toString())) {
            log.info("邮箱验证码错误：{}}", trueEmailCaptcha);
            return false;
        }
        return true;
    }

    /**
     * 通过电子邮件重置用户密码
     *
     * @param userPwdUpdateByEmailRequest 用户密码重置请求
     * @return {@code 布尔值}
     * @param请求
     */
    @Override
    public long userPasswordUpdateByEmail(HttpServletRequest request, UserPwdUpdateByEmailRequest userPwdUpdateByEmailRequest) {
        // 获取参数
        String userAccount = userPwdUpdateByEmailRequest.getUserAccount();
        String userPassword = userPwdUpdateByEmailRequest.getUserPassword();
        String checkPassword = userPwdUpdateByEmailRequest.getCheckPassword();
        String email = userPwdUpdateByEmailRequest.getUserEmail();
        String emailCaptcha = userPwdUpdateByEmailRequest.getEmailCaptcha();
        // 校验两次密码是否一致
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次新密码输入不一致");
        }
        // 验证账号是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserEmail, email);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该邮箱未绑定账号");
        }
        // 验证用户是否被禁用
        if (user.getUserRole().equals(UserConstant.BAN_ROLE)) {
            throw new BusinessException(ErrorCode.BAN_ERROR, "该用户已被禁用,请联系管理员解封");
        }
        // 验证邮箱
        // ThrowUtils.throwIf(user.getUserEmail() == null, ErrorCode.NOT_LOGIN_ERROR, "该用户未绑定邮箱");
        // if (!user.getUserEmail().equals(email)) {
        //     throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不正确");
        // }
        // 验证邮箱验证码
        boolean flag = validateEmailCode(email, emailCaptcha);
        if (!flag) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱验证码错误");
        }
        // 修改密码
        synchronized (userAccount.intern()) {
            // 加密密码
            String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
            // 更新用户密码
            user.setUserPassword(encryptPassword);
            ThrowUtils.throwIf(!this.updateById(user), ErrorCode.OPERATION_ERROR, "修改密码失败");
            return user.getId();
        }
    }

    /**
     * 删除用户
     *
     * @param id        id
     * @param loginUser 登录用户
     * @return int
     */
    @Override
    public long deleteUser(Long id, User loginUser) {
        // 是否为超级管理员（邮箱）
        User deleteUser = userMapper.selectById(id);
        ThrowUtils.throwIf(deleteUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        String userEmail = loginUser.getUserEmail();
        boolean isLoginSuperAdmin = false;
        if (userEmail != null) {
            isLoginSuperAdmin = userEmail.equals(superAdminEmail);
        }
        // 自己不能删除自己，包含了下面的超级管理员删除自己
        if (id.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "自己不能删除自己");
        }
        // 管理员之间也不能删除
        if (!isLoginSuperAdmin && loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE) && deleteUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "管理员不能删除管理员");
        }
        ThrowUtils.throwIf(userMapper.deleteById(id) < 1, ErrorCode.SYSTEM_ERROR, "未知错误，请联系超级管理员！");
        return id;
    }

    /**
     * 添加用户
     *
     * @param userAddRequest 用户添加Request
     * @return {@code Long}
     */
    @Override
    public long addUser(UserAddRequest userAddRequest, User loginUser) {
        String userName = userAddRequest.getUserName();
        String userAccount = userAddRequest.getUserAccount();
        Integer userGender = userAddRequest.getUserGender();
        String userRole = userAddRequest.getUserRole();

        if (StringUtils.isEmpty(userAccount)) {
            // es: ydg13857331
            userAddRequest.setUserAccount(UserConstant.DEFAULT_USER_ACCOUNT + new Random().nextInt(100000000));
        }
        // 判断参数（不是必须的）,设置默认值
        if (StringUtils.isEmpty(userName)) {
            // 设置默认的用户名（UserConstant.DEFAULT_USER_NAME+当时的时间戳）user+时间戳
            userAddRequest.setUserName(UserConstant.DEFAULT_USER_NAME + System.currentTimeMillis());
        }
        if (userGender == null || userGender < 0 || userGender > 2) {
            // 设置默认的性别（UserConstant.DEFAULT_USER_GENDER）
            userAddRequest.setUserGender(UserConstant.DEFAULT_USER_GENDER);
        }
        if (StringUtils.isEmpty(userRole)) {
            // 设置默认的角色（UserConstant.DEFAULT_ROLE）
            userAddRequest.setUserRole(UserConstant.USER_ROLE);
        }
        // 超级管理员权限，根据邮箱来确定唯一的一个超级管理员权限（只能有一个），可以操作普通的用户和普通管理员
        if (userRole != null && userRole.equals(UserConstant.ADMIN_ROLE) && !loginUser.getUserEmail().equals(superAdminEmail)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "你无权限修改该用户权限");
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 设置默认的密码
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_USER_PASSWORD).getBytes());
        user.setUserPassword(encryptPassword);
        // 设置默认的简介（UserConstant.DEFAULT_USER_PROFILE）
        user.setUserProfile(UserConstant.DEFAULT_USER_PROFILE);
        // 设置默认的头像（UserConstant.DEFAULT_USER_AVATAR）
        user.setUserAvatar(UserConstant.DEFAULT_USER_AVATAR);
        // 校验用户账号是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User oldUser = this.baseMapper.selectOne(queryWrapper);
        if (oldUser != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户账号已存在,请换一个");
        }
        // 保存用户
        boolean result = this.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "未知错误,添加用户失败");
        return user.getId();
    }

    /**
     * 获取用户脱敏 vo
     *
     * @param user 用户
     * @return {@code UserVO}
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取用户脱敏 vo list
     *
     * @param userList 用户列表
     * @return {@code List<UserVO>}
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 用户密码由admin重置
     *
     * @param userId 用户id
     * @return {@code Long}
     */
    @Override
    public long userPasswordResetByAdmin(Long userId) {
        ThrowUtils.throwIf(userId < 1, ErrorCode.PARAMS_ERROR, "用户id不合法");
        // 根据id获取用户
        User user = this.getById(userId);
        // 用户存在
        if (user != null) {
            String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + UserConstant.DEFAULT_USER_PASSWORD).getBytes());
            user.setUserPassword(encryptPassword);
            ThrowUtils.throwIf(!this.updateById(user), ErrorCode.OPERATION_ERROR, "重置密码失败");
            return userId;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在!");
    }

    /**
     * 更新用户信息
     * 更新用户
     *
     * @param userUpdateRequest 用户更新请求
     * @param request           请求
     * @return {@code Integer}
     */
    @Override
    public long updateUserInfo(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        // 获取要修改update更新的用户ID
        long updateUserId = userUpdateRequest.getId();
        // 判断用户id是否合法
        if (updateUserId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不合法");
        }
        // 获取当前的登录用户信息
        User loginUser = this.getLoginUser(request);
        // 如果不是管理员，只允许更新当前（自己的）信息
        boolean isAdmin = loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE);
        if (!isAdmin && updateUserId != loginUser.getId()) {
            // 即不是管理员也不是当前登录的用户，无权限修改update用户信息，抛异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "当前你无权限修改该用户信息！");
        }
        // 通过用户id来查询要update的用户信息，更新前原始用户数据
        User oldUser = userMapper.selectById(updateUserId);
        if (oldUser == null) {
            // 用户不存在，抛异常
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在或已删除！");
        }
        if (!ObjectUtils.isEmpty(userUpdateRequest.getUserAccount())) {
            // 账号唯一性
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.eq(User::getUserAccount, userUpdateRequest.getUserAccount());
            User account = userMapper.selectOne(qw);
            if (account != null && account.getId() != updateUserId) {
                String userAccount = account.getUserAccount();
                if (userUpdateRequest.getUserAccount().equals(userAccount)) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已存在，请换一个");
                }
            }
        }
        // new一个空用户对象
        User updateUser = new User();
        // 判断传进来的各个参数是否为空，不为空则更新
        if (!ObjectUtils.isEmpty(userUpdateRequest.getUserEmail())) {
            ThrowUtils.throwIf(!RegexUtils.matches(VerifyParamRegexConstant.EMAIL, userUpdateRequest.getUserEmail()), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        if (!ObjectUtils.isEmpty(userUpdateRequest.getUserPhone())) {
            ThrowUtils.throwIf(!RegexUtils.matches(VerifyParamRegexConstant.PHONE, userUpdateRequest.getUserPhone()), ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        }
        updateUser.setId(oldUser.getId());
        updateUser.setUserAge(!ObjectUtils.isEmpty(userUpdateRequest.getUserAge()) ? userUpdateRequest.getUserAge() : oldUser.getUserAge());
        updateUser.setUserAccount(!ObjectUtils.isEmpty(userUpdateRequest.getUserAccount()) ? userUpdateRequest.getUserAccount() : oldUser.getUserAccount());
        updateUser.setUserName(!ObjectUtils.isEmpty(userUpdateRequest.getUserName()) ? userUpdateRequest.getUserName() : oldUser.getUserName());
        updateUser.setUserAvatar(!ObjectUtils.isEmpty(userUpdateRequest.getUserAvatar()) ? userUpdateRequest.getUserAvatar() : oldUser.getUserAvatar());
        updateUser.setUserEmail(!ObjectUtils.isEmpty(userUpdateRequest.getUserEmail()) ? userUpdateRequest.getUserEmail() : oldUser.getUserEmail());
        updateUser.setUserPhone(!ObjectUtils.isEmpty(userUpdateRequest.getUserPhone()) ? userUpdateRequest.getUserPhone() : oldUser.getUserPhone());
        updateUser.setUserGender(!ObjectUtils.isEmpty(userUpdateRequest.getUserGender()) ? userUpdateRequest.getUserGender() : oldUser.getUserGender());
        updateUser.setUserProfile(!ObjectUtils.isEmpty(userUpdateRequest.getUserProfile()) ? userUpdateRequest.getUserProfile() : oldUser.getUserProfile());
        // 标签校验
        String tags = userUpdateRequest.getTags();
        // 转为json
        if (tags != null && !tags.isEmpty()) {
            // 将 tags 转换为 List<String>
            List<String> tagsJson = JsonUtils.jsonToList(tags, String.class);
            // 从数据库查询这些标签是否存在
            // List<String> existingTags = tagService.findExistingTags(tagsJson);
            // 找到不存在的标签
            // assert tagsJson != null;
            // List<String> nonExistingTags = tagsJson.stream()
            // .filter(tag -> !existingTags.contains(tag))
            // .collect(Collectors.toList());
            // 如果有不存在的标签则抛出异常
            // if (!nonExistingTags.isEmpty()) {
            // throw new RuntimeException("以下标签不存在: " + nonExistingTags);
            // }
        }
        updateUser.setTags(!ObjectUtils.isEmpty(tags) ? userUpdateRequest.getTags() : oldUser.getTags());
        // 用户角色
        String newRole = userUpdateRequest.getUserRole();
        // 老角色
        String oldRole = oldUser.getUserRole();
        String oldUserEmail = oldUser.getUserEmail();
        // 是否为超级管理员（邮箱）
        if (oldRole != null && oldUserEmail != null && newRole != null) {
            boolean isLoginSuperAdmin = loginUser.getUserEmail().equals(superAdminEmail);
            // 超级管理员除了他本身都可以修改其他用户角色（包括管理员）
            if (isLoginSuperAdmin && oldUserEmail.equals(superAdminEmail) && !newRole.equals(UserConstant.ADMIN_ROLE)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "超级管理员不能修改自己的权限");
            }
            // 旧角色不是管理员且操作用户为管理员
            if (oldRole.equals(UserConstant.ADMIN_ROLE) && isAdmin && !isLoginSuperAdmin) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改该用户权限");
            }
        }
        updateUser.setUserRole(!ObjectUtils.isEmpty(userUpdateRequest.getUserRole()) ? newRole : oldRole);
        // 返回更新结果
        ThrowUtils.throwIf(userMapper.updateById(updateUser) < 1, ErrorCode.SYSTEM_ERROR, "修改用户信息失败!");
        // 更新一下 缓存
        User user = userMapper.selectById(updateUserId);
        redisUtils.set(RedisKeyConstant.USER_LOGIN_STATE_CACHE + updateUser.getId(), user);
        return updateUserId;
    }

    /**
     * 是否为admin
     *
     * @param request 请求
     * @return boolean
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    /**
     * 是否为admin
     *
     * @param user 用户
     * @return boolean
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnums.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    @Override
    public List<UserVO> findUsersByTags(List<String> tags) {
        // todo 如果想要更加全面的校验可以校验判断标签是否存在
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 遍历 tags 构建条件
        for (String tag : tags) {
            queryWrapper.or().like(User::getTags, tag);
        }
        // 执行查询
        List<User> users = userMapper.selectList(queryWrapper);
        List<UserVO> userVOList = new ArrayList<>();
        // 使用 BeanUtils.copyProperties 或自定义转换将 User 对象转为 UserVO 对象
        for (User user : users) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO); // 将 User 对象的属性复制到 UserVO 对象
            userVOList.add(userVO); // 将转换后的 UserVO 添加到返回列表
        }
        return userVOList;
    }

    /**
     * 匹配用户
     *
     * @param num       num
     * @param loginUser 登录用户
     * @return {@link List }<{@link User }>
     */
    @Override
    public List<UserVO> matchUsers(long num, User loginUser) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //优化:只查需要的数据，减少查询时间
        queryWrapper.select(User::getId, User::getTags);
        //过滤掉标签为空的用户
        queryWrapper.isNotNull(User::getTags);
        //最终得到和当前用户进行比较的所有用户
        List<User> userList = this.list(queryWrapper);
        //获取当前登录用户的标签---数据库中的是json格式
        String tags = loginUser.getTags();
        List<String> tagList = JsonUtils.jsonToList(tags, String.class);
        // 用户列表的下标 => 用户---key可以重复的数据结构
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度，越小越相似
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己---过滤掉自己
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagList = JsonUtils.jsonToList(userTags, String.class);
            // 计算分数
            assert tagList != null;
            assert userTagList != null;
            long distance = AlgorithmUtils.getListEditDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表---没有用户的具体信息
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        //查出来的具体用户信息列表没有按照之前的输出顺序排列
        userQueryWrapper.in(User::getId, userIdList);
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<UserVO>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getUserVO)
                .collect(Collectors.groupingBy(UserVO::getId));
        //最后遍历原来的用户id获得顺序的用户信息
        List<UserVO> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 推荐用户
     *
     * @param num num
     * @return {@link List }<{@link UserVO }>
     */
    @Override
    public List<UserVO> recommendUsers(long num) {
        String key = RedisKeyConstant.RECOMMEND_USER;
        // 查询缓存
        Object oldKey = redisUtils.get(key);
        if (oldKey != null) {
            return JsonUtils.jsonToList(oldKey.toString(), UserVO.class);
        }
        // 若已过期缓存
        List<UserVO> randomUser = this.getRandomUser(8);
        redisUtils.set(key, JsonUtils.objToJson(randomUser), 60 * 60 * 24);
        return randomUser;
    }


    /**
     * 获取随机用户
     *
     * @param number 编号
     * @return {@link List }<{@link UserVO }>
     */
    @Override
    public List<UserVO> getRandomUser(long number) {
        List<User> randomUser = userMapper.getRandomUser(number);
        List<UserVO> userVOList = randomUser.stream().map((item) -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(item, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return userVOList;
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询Request
     * @return {@code LambdaQueryWrapper<User>}
     */
    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 获取参数
        Long id = userQueryRequest.getId();
        ThrowUtils.throwIf(id != null && id < 1, ErrorCode.PARAMS_ERROR, "用户id不合法");
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        Integer gender = userQueryRequest.getUserGender();
        String email = userQueryRequest.getUserEmail();
        String phone = userQueryRequest.getUserPhone();
        List<String> userTags = userQueryRequest.getUserTags();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(id != null, User::getId, id)
                .eq(gender != null, User::getUserGender, gender)
                .eq(StringUtils.isNotBlank(userAccount), User::getUserAccount, userAccount)
                .eq(StringUtils.isNotBlank(userRole), User::getUserRole, userRole)
                .eq(StringUtils.isNotBlank(email), User::getUserEmail, email)
                .eq(StringUtils.isNotBlank(phone), User::getUserPhone, phone)
                .like(StringUtils.isNotBlank(userProfile), User::getUserProfile, userProfile)
                .like(StringUtils.isNotBlank(userName), User::getUserName, userName);
        if (userTags != null && !userTags.isEmpty()) {
            // 将 userTags 转换为小写，确保查询大小写不敏感
            List<String> lowerCaseTags = userTags.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            // 在转换为 JSON 时也进行小写处理
            String jsonTags = JsonUtils.objToJson(lowerCaseTags);

            // JSON_CONTAINS 检查结合正则表达式匹配
            lambdaQueryWrapper.apply("JSON_CONTAINS(LOWER(tags), LOWER({0}))", jsonTags);
        }
        lambdaQueryWrapper.orderBy(SqlUtils.validSortField(sortField),
                        sortOrder.equals(PageConstant.SORT_ORDER_ASC), isSortField(sortField));
        return lambdaQueryWrapper;
    }

    /**
     * 是否为排序字段
     *
     * @param sortField 排序字段
     * @return {@code SFunction<User, ?>}
     */
    private SFunction<User, ?> isSortField(String sortField) {
        // 如果排序字段为空，则默认为更新时间
        if (StringUtils.isBlank(sortField)) {
            sortField = UserSortFieldEnums.UPDATE_TIME.name();
        }
        if (SqlUtils.validSortField(sortField)) {
            return UserSortFieldEnums.fromString(sortField)
                    .map(UserSortFieldEnums::getFieldGetter)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "错误的排序字段"));
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段无效");
        }
    }

    public List<UserGrowthRequest> getUserGrowth() {
        // 查询数据库，按月统计用户增长数据
        return userMapper.getUserGrowthData();
    }

}