package org.example.zjzaiagent.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 聊天记录实体类
 * @TableName logger
 */
@TableName(value ="logger")
@Data
public class Logger implements Serializable {
    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private String message;

    /**
     * 
     */
    private Date time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}