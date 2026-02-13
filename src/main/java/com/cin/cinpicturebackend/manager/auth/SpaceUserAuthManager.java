package com.cin.cinpicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.cin.cinpicturebackend.exception.ErrorCode;
import com.cin.cinpicturebackend.exception.ThrowUtils;
import com.cin.cinpicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.cin.cinpicturebackend.manager.auth.model.SpaceUserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpaceUserAuthManager {

    public static  final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        ThrowUtils.throwIf(spaceUserRole == null, ErrorCode.PARAMS_ERROR," 空间角色不能为空");
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> r.getKey().equals(spaceUserRole))
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return  role.getPermissions();
    }
}
