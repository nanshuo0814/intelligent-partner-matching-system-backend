package icu.ydg.exception;


import icu.ydg.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常
 *
 * @author 袁德光
 * @date 2024/07/26
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 业务异常（参数异常40000）
     *
     * @param message 信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.PARAMS_ERROR.getCode();
    }

}
