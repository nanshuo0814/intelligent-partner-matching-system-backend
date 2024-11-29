package icu.ydg.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * id通用请求
 *
 * @author 袁德光
 * @date 2024/03/31 12:12:29
 */
@Data
@ApiModel(value = "IdRequest", description = "id通用请求DTO")
public class IdRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "id", required = true)
    private Long id;

    private static final long serialVersionUID = 1L;
}