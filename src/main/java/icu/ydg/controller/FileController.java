package icu.ydg.controller;

import cn.hutool.core.io.FileUtil;
import icu.ydg.annotation.Verify;
import icu.ydg.common.ApiResponse;
import icu.ydg.common.ApiResult;
import icu.ydg.common.ErrorCode;
import icu.ydg.constant.FileConstant;
import icu.ydg.constant.UserConstant;
import icu.ydg.exception.BusinessException;
import icu.ydg.manager.UpyunManager;
import icu.ydg.model.domain.User;
import icu.ydg.model.dto.file.UploadFileRequest;
import icu.ydg.model.enums.file.FileUploadTypeEnums;
import icu.ydg.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 文件接口
 *
 * @author 袁德光
 * @date 2024/01/26
 */
//@Api(tags = "文件模块接口")
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    /**
     * 图片文件类型
     */
    public static final List<String> IMAGE_TYPE = Arrays.asList("jpeg", "jpg", "svg", "png", "webp", "icon", "gif");

    /**
     * 文件最大大小(1M)
     */
    private static final long MAX_SIZE = 1024 * 1024L;

    @Resource
    private UserService userService;
    @Resource
    private UpyunManager upyunManager;

    /**
     * 上传文件
     * 文件上传
     *
     * @param multipartFile     多部分文件
     * @param request           请求
     * @param uploadFileRequest 上传文件Request
     * @return {@code ApiResponse<String>}
     */
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    @Verify(checkAuth = UserConstant.USER_ROLE)
    public ApiResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                          UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String type = uploadFileRequest.getType();
        FileUploadTypeEnums fileUploadTypeEnums = FileUploadTypeEnums.getEnumByValue(type);
        if (fileUploadTypeEnums == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件的参数类型错误");
        }
        validFile(multipartFile, fileUploadTypeEnums);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String currentTime = String.valueOf(System.currentTimeMillis());
        String filename = multipartFile.getOriginalFilename() + "-" + currentTime;
        String filepath = String.format("%s/%s/%s", fileUploadTypeEnums.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            Response response = upyunManager.uploadFile(filepath, file);
            log.info("文件上传成功，response = {}", response);
            // 返回可访问地址
            return ApiResult.success(FileConstant.UPYUN_HOST_ADDRESS + filepath, "文件上传成功！");
        } catch (Exception e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败,请联系管理员");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("文件删除失败, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param fileUploadBizEnum 业务类型
     * @param multipartFile     多部分文件
     */
    private void validFile(MultipartFile multipartFile, FileUploadTypeEnums fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 针对type是用户头像
        if (FileUploadTypeEnums.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > MAX_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!IMAGE_TYPE.contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        // todo 可以添加其他校验条件
    }
}
