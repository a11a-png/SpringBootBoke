package com.wjg.vueboke.po;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class SysToken {
    /**
     * 用户ID
     */
    private Integer id;

    /**
     * token
     */
    private String token;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}
