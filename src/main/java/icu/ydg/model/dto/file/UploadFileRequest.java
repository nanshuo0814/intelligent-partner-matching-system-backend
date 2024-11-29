package icu.ydg.model.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 上传文件请求
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Data
@ApiModel(value = "UploadFileRequest", description = "上传文件请求DTO")
public class UploadFileRequest implements Serializable {

    private static final long serialVersionUID = -2790684919067584112L;

    /**
     * 业务类型
     */
    @ApiModelProperty(value = "上传文件类型", required = true)
    private String type;

}
