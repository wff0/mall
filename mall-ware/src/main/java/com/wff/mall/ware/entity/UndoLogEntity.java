package com.wff.mall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 12:16:16
 */
@Data
@TableName("undo_log")
public class UndoLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private Long id;
    /**
     *
     */
    private Long branchId;
    /**
     *
     */
    private String xid;
    /**
     *
     */
    private String context;
    /**
     *
     */
    private byte[] rollbackInfo;
    /**
     *
     */
    private Integer logStatus;
    /**
     *
     */
    private Date logCreated;
    /**
     *
     */
    private Date logModified;
    /**
     *
     */
    private String ext;

}
