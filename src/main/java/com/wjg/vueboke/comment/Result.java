package com.wjg.vueboke.comment;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result implements Serializable {

    // 0成功，500失败
    private int code;
    private String msg;
    private Object data;
    private String action;
    private Integer count;

    public static Result success() {
        return Result.success("操作成功", null);
    }

    public static Result success(Object data) {
        return Result.success("操作成功", data);
    }

    public static Result success(String msg, Object data) {
        Result result = new Result();
        result.code = 0;
        result.msg = msg;
        result.data = data;
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.code = 500;
        result.data = null;
        result.msg = msg;
        return result;
    }

    public Result action(String action){
        this.action = action;
        return this;
    }


}
