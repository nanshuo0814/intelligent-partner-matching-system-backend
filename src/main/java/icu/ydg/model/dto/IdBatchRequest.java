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
@ApiModel(value = "IdBatchRequest", description = "批量id")
public class IdBatchRequest implements Serializable {


    private List<Long> ids; // 批量删除用的ID集合

    private static final long serialVersionUID = 1L;
}