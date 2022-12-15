package xyz.xcye.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.xcye.admin.po.Permission;
import xyz.xcye.admin.pojo.PermissionPojo;
import xyz.xcye.core.enums.RegexEnum;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.exception.permission.PermissionException;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.lambda.AssertUtils;
import xyz.xcye.data.entity.Condition;
import xyz.xcye.data.entity.PageData;
import xyz.xcye.data.util.PageUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author qsyyke
 * @date Created in 2022/5/4 21:34
 */

@Service
public class PermissionService {

    @Autowired
    private AuroraPermissionService auroraPermissionService;

    public int deleteByUid(long uid) {
        return auroraPermissionService.deleteById(uid);
    }

    public void insertPermission(PermissionPojo permission) {
        Objects.requireNonNull(permission,"方法路径信息不能为null");
        // 判断path是否符合规范，必须是GET:Path这种形式 不支持中文路径
        AssertUtils.stateThrow(matchesResourcePath(permission.getPath()), () -> new PermissionException(ResponseStatusCodeEnum.PERMISSION_RESOURCE_NOT_RIGHT));
        auroraPermissionService.insert(BeanUtils.copyProperties(permission, Permission.class));
    }

    public int updatePermission(PermissionPojo permission) {
        Objects.requireNonNull(permission, "资源路径权限信息不能为null");
        if (StringUtils.hasLength(permission.getPath())) {
            AssertUtils.stateThrow(matchesResourcePath(permission.getPath()),
                    () -> new PermissionException(ResponseStatusCodeEnum.PERMISSION_RESOURCE_NOT_RIGHT));
        }else {
            // 没有path
            permission.setPath(null);
        }
        return auroraPermissionService.updateById(BeanUtils.copyProperties(permission, Permission.class));
    }

    public PageData<Permission> selectAllPermission(Condition<Long> condition) {
        return auroraPermissionService.queryListByCondition(condition);
    }

    private boolean matchesResourcePath(String resourcePath) {
        return Pattern.matches(RegexEnum.REST_FUL_PATH.getRegex(),resourcePath);
    }

    public Permission selectByUid(long uid) {
        return auroraPermissionService.queryById(uid);
    }
}
