package com.rongyan.appstore.item;

import java.io.Serializable;

/**
 * 返回对象公共类
 */

public class Result implements Serializable{

    private boolean success;//根据这个字段来判断调用是否成功

    private String code;

    private String message;//失败时会放错误信息

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
