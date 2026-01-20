package com.cin.cinpicturebackend.model.dto.user;

import com.cin.cinpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
//@EqualsAndHashCode(callSuper = true) 的作用是：
// 在 Lombok 生成 equals() 和 hashCode() 时，
// **把父类 PageRequest 里的字段也纳入比较/计算**。
//需要严格区分“同一查询条件但不同分页”的请求对象。
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
