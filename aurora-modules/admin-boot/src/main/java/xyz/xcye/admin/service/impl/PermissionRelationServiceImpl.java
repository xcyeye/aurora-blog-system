package xyz.xcye.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.xcye.admin.dto.RolePermissionDTO;
import xyz.xcye.admin.po.Permission;
import xyz.xcye.admin.po.Role;
import xyz.xcye.admin.po.RolePermissionRelationship;
import xyz.xcye.admin.po.UserRoleRelationship;
import xyz.xcye.admin.service.*;
import xyz.xcye.admin.vo.UserVO;
import xyz.xcye.core.exception.permission.PermissionException;
import xyz.xcye.core.util.lambda.AssertUtils;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.exception.role.RoleException;
import xyz.xcye.core.exception.user.UserException;
import xyz.xcye.data.entity.Condition;

import java.util.*;

/**
 * @author qsyyke
 * @date Created in 2022/5/4 22:53
 */

@Service
public class PermissionRelationServiceImpl implements PermissionRelationService {

    private final String rolePrefix = "ROLE_";

    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;
    @Autowired
    private RolePermissionRelationshipService rolePermissionRelationshipService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserService userService;

    @Override
    public Set<Map<String, String>> loadPermissionByUserUid(long userUid) {
        return packagePermission(userUid, true);
    }

    @Override
    public Set<Map<String, String>> loadPermissionByUsername(String username) {
        UserVO userVO = userService.queryByUsername(username);
        if (userVO == null) {
            return new HashSet<>();
        }

        return loadPermissionByUserUid(userVO.getUid());
    }

    @Override
    public Set<Map<String, String>> loadPermissionByRoleName(String roleName) {
        AssertUtils.stateThrow(StringUtils.hasLength(roleName), () -> new RoleException("角色名称不能为null或者空"));
        Set<Map<String,String>> permissionSet = new HashSet<>();
        // 1.通过角色名字，查询所对应的权限uid
        List<Role> roleList = roleService.selectAllRole(Condition.instant(roleName)).getResult();

        // 2. 如果没有特殊情况，一个角色名字，只会对应一个uid，所以上面查询出来记录最多只有一条，但是为了
        roleList.forEach(role -> {
            Long uid = role.getUid();
            // 通过此uid查询对应的权限信息
            permissionSet.addAll(packagePermission(uid, false));
        });
        return permissionSet;
    }

    @Override
    public Set<Map<String, RolePermissionDTO>> loadAllRolePermission(Condition<Long> condition) {

        Set<Map<String, RolePermissionDTO>> rolePermissionSet = new HashSet<>();

        List<RolePermissionRelationship> rolePermissionRelationshipList = rolePermissionRelationshipService
                .selectAllRolePermissionRelationship(condition);

        // 将roleUid和permissionUid转换成文字
        List<Role> roleList = roleService.selectAllRole(new Condition<>()).getResult();
        List<Permission> permissionList = permissionService.selectAllPermission(new Condition<>()).getResult();

        // 保存临时的角色和权限的map集合
        Map<Long, Role> roleMap = new HashMap<>();
        Map<Long, Permission> permissionMap = new HashMap<>();

        // 将每一个role中的Uid作为键， role对象作为值，保存在map中
        roleList.forEach(role -> roleMap.put(role.getUid(), role));
        permissionList.forEach(permission -> permissionMap.put(permission.getUid(), permission));

        rolePermissionRelationshipList.forEach(rolePermissionRelationship -> {
            // 从roleMap和permissionMap去除roleUid和permissionUid所对应的对象
            Role role = roleMap.get(rolePermissionRelationship.getRoleUid());
            Permission permission = permissionMap.get(rolePermissionRelationship.getPermissionUid());

            // 将所需要的信息，放入集合中
            RolePermissionDTO rolePermissionDTO = RolePermissionDTO.builder()
                    .roleName(rolePrefix + role.getName()).roleStatus(role.getStatus())
                    .permissionName(permission.getName()).path(permission.getPath())
                    .build();

            // 将角色名称作为键，rolePermissionDTO作为值放入map中
            Map<String, RolePermissionDTO> rolePermissionDTOMap = new HashMap<>();
            rolePermissionDTOMap.put(rolePrefix + role.getName(), rolePermissionDTO);
            rolePermissionSet.add(rolePermissionDTOMap);
        });

        return rolePermissionSet;
    }

    @Override
    public Set<Role> queryRoleByPermissionPath(String permissionPath) {
        AssertUtils.stateThrow(StringUtils.hasLength(permissionPath), () -> new RoleException("权限路径不能为null或者空"));
        return packageRole(permissionPath);
    }

    @Override
    public Set<UserVO> queryUserByPermissionPath(String permissionPath) {
        Set<UserVO> userVOSet = new HashSet<>();
        AssertUtils.stateThrow(StringUtils.hasLength(permissionPath), () -> new RoleException("权限路径不能为null或者空"));
        // 查询此permissionPath对应的uid
        Set<Role> roleSet = packageRole(permissionPath);
        // 获取role，然后查询用户信息
        roleSet.stream().forEach(role -> {
            List<UserRoleRelationship> userRoleRelationshipList = userRoleRelationshipService
                    .selectAllUserRoleRelationship(Condition.instant(role.getUid(), false));
            // 遍历查询用户
            userRoleRelationshipList.forEach(userRoleRelationship -> {
                UserVO userVO = userService.queryByUid(userRoleRelationship.getUserUid());
                userVOSet.add(userVO);
            });
        });
        return userVOSet;
    }

    @Transactional
    @Override
    public int insertUserRoleBatch(long[] userUidArr, long roleUid) {
        final int[] successNum = {0};
        Role role = roleService.selectByUid(roleUid);
        // 查询看是否存在此角色
        AssertUtils.stateThrow(role != null,
                () -> new RoleException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));

        // 判断此角色是否被禁用
        AssertUtils.stateThrow(!role.getStatus(), () -> new RoleException(ResponseStatusCodeEnum.PERMISSION_ROLE_HAD_DISABLED));

        Arrays.stream(userUidArr)
                .filter(userUid -> userService.queryByUid(userUid) != null)
                .filter(userUid -> userRoleRelationshipService.selectAllUserRoleRelationship(Condition.instant(userUid, roleUid)).isEmpty())
                .forEach(userUid -> {
                    UserRoleRelationship userRoleRelationship = UserRoleRelationship.builder()
                            .roleUid(roleUid).userUid(userUid)
                            .build();
                    successNum[0] = successNum[0] + userRoleRelationshipService.insertUserRoleRelationship(userRoleRelationship);
                });
        return successNum[0];
    }

    @Override
    public int deleteUserRoleBatch(long userUid, long[] roleUidArr) {
        final int[] successNum = {0};
        // 判断用户是否存在
        AssertUtils.stateThrow(userService.queryByUid(userUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST));

        // 组装查询条件
        Condition<Long> condition = new Condition<>();
        condition.setUid(userUid);
        Arrays.stream(roleUidArr).forEach(roleUid -> {
            condition.setOtherUid(roleUid);
            // 查询roleUid和userUid对应的uid
            List<UserRoleRelationship> userRoleRelationshipList = userRoleRelationshipService
                    .selectAllUserRoleRelationship(condition);
            if (userRoleRelationshipList.size() > 0) {
                successNum[0] = successNum[0] + userRoleRelationshipService.deleteByUid(userRoleRelationshipList.get(0).getUid());
            }
        });
        return successNum[0];
    }

    @Override
    public int updateUserRole(long userUid, long originRoleUid, long newRoleUid) {
        final int[] successNum = {0};
        // 判断用户是否存在
        AssertUtils.stateThrow(userService.queryByUid(userUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST));
        // 更新用户角色关系
        Condition<Long> condition = new Condition<>();
        condition.setUid(userUid);
        condition.setOtherUid(originRoleUid);

        List<UserRoleRelationship> userRoleRelationshipList = userRoleRelationshipService
                .selectAllUserRoleRelationship(condition);
        if (userRoleRelationshipList.size() > 0) {
            // 判断newRoleUid是否存在
            AssertUtils.stateThrow(roleService.selectByUid(newRoleUid) != null,
                    () -> new RoleException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));
            // 更新
            userRoleRelationshipList.get(0).setRoleUid(newRoleUid);
            successNum[0] = userRoleRelationshipService.updateUserRoleRelationship(userRoleRelationshipList.get(0));
        }

        return successNum[0];
    }

    @Override
    public int insertRolePermissionBatch(long[] roleUidArr, long permissionUid) {
        final int[] successNum = {0};
        // 判断此permissionUid是否可用
        AssertUtils.stateThrow(permissionService.selectByUid(permissionUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_RESOURCE_NOT_RIGHT));

        // 遍历可用的roleUid
        Arrays.stream(roleUidArr)
                .filter(roleUid -> roleService.selectByUid(roleUid) != null)
                .filter(roleUid -> rolePermissionRelationshipService.selectAllRolePermissionRelationship(Condition.instant(roleUid, permissionUid)).isEmpty())
                .forEach(roleUid -> {
                    // 创建角色路径关系
                    RolePermissionRelationship rolePermissionRelationship = RolePermissionRelationship.builder()
                            .permissionUid(permissionUid).roleUid(roleUid)
                            .build();
                    // 插入角色路径关系
                    successNum[0] = successNum[0] + rolePermissionRelationshipService
                            .insertRolePermissionRelationship(rolePermissionRelationship);
                });
        return successNum[0];
    }

    @Override
    public int deleteRolePermissionBatch(long roleUid, long[] permissionUidArr) {
        final int[] successNum = {0};
        // 判断角色是否存在
        AssertUtils.stateThrow(roleService.selectByUid(roleUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));
        // 组装查询条件
        Condition<Long> condition = new Condition<>();
        condition.setUid(roleUid);
        Arrays.stream(permissionUidArr).forEach(permissionUid -> {
            // 查询出roleUid和permissionUid对应的uid
            condition.setOtherUid(permissionUid);
            List<RolePermissionRelationship> rolePermissionRelationshipList = rolePermissionRelationshipService
                    .selectAllRolePermissionRelationship(condition);
            if (rolePermissionRelationshipList.size() > 0) {
                // 删除
                successNum[0] = successNum[0] + rolePermissionRelationshipService
                        .deleteByUid(rolePermissionRelationshipList.get(0).getUid());
            }
        });

        return successNum[0];
    }

    @Override
    public int updateRolePermission(long roleUid, long originPermissionUid, long newPermissionUid) {
        final int[] successNum = {0};
        // 判断角色是否存在
        AssertUtils.stateThrow(roleService.selectByUid(roleUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));
        // 更新用户角色关系
        Condition<Long> condition = new Condition<>();
        condition.setUid(roleUid);
        condition.setOtherUid(originPermissionUid);
        List<RolePermissionRelationship> permissionRelationshipList = rolePermissionRelationshipService
                .selectAllRolePermissionRelationship(condition);
        if (permissionRelationshipList.size() > 0) {
            // 判断此newPermissionUid是否存在
            AssertUtils.stateThrow(permissionService.selectByUid(newPermissionUid) != null,
                    () -> new PermissionException(ResponseStatusCodeEnum.PERMISSION_RESOURCE_NOT_RIGHT));
            // 更新
            permissionRelationshipList.get(0).setPermissionUid(newPermissionUid);
            successNum[0] = rolePermissionRelationshipService.updateRolePermissionRelationship(permissionRelationshipList.get(0));
        }
        return successNum[0];
    }

    private Set<Map<String,String>> packagePermission(Long uid, boolean isUserUid) {
        Set<Map<String,String>> permissionSet = new HashSet<>();

        // 通过userUid查询此用户所拥有的所有角色信息
        List<UserRoleRelationship> userRoleRelationshipList = userRoleRelationshipService
                .selectAllUserRoleRelationship(Condition.instant(uid, isUserUid));

        // 遍历，在根据角色uid查询所拥有的权限
        userRoleRelationshipList.forEach(userRoleRelationship -> {
            // 查询此roleUid对应的角色信息
            Role role = roleService.selectByUid(userRoleRelationship.getRoleUid());
            // 查询此角色对应的路径权限
            List<RolePermissionRelationship> rolePermissionRelationshipList = rolePermissionRelationshipService
                    .selectAllRolePermissionRelationship(Condition.instant(userRoleRelationship.getRoleUid(), true));
            rolePermissionRelationshipList.forEach(rolePermissionRelationship -> {
                // 遍历此rolePermissionRelationshipList，取出path
                // 查询此permissionUid对应的信息
                Permission permission = permissionService.selectByUid(rolePermissionRelationship.getPermissionUid());
                // 进行角色和路径权限的组装
                Map<String,String> map = new HashMap<>();
                map.put(rolePrefix + role.getName(), permission.getPath());
                permissionSet.add(map);
            });
        });

        return permissionSet;
    }

    private Set<Role> packageRole(String permissionPath) {
        Set<Role> roleSet = new HashSet<>();
        // 1. 获取此permissionPath对应的uid
        List<Permission> permissionList = permissionService.selectAllPermission(Condition.instant(permissionPath)).getResult();

        // 2. 遍历，取出每一个元素，然后查询对应的role
        permissionList.forEach(permission -> {
            Long permissionUid = permission.getUid();
            // 查询role
            List<RolePermissionRelationship> rolePermissionRelationshipList = rolePermissionRelationshipService
                    .selectAllRolePermissionRelationship(Condition.instant(permissionUid, false));

            // 遍历rolePermissionRelationshipList，查询对应的role
            rolePermissionRelationshipList.forEach(rolePermissionRelationship -> {
                Role role = roleService.selectByUid(rolePermissionRelationship.getRoleUid());
                // 添加到set中
                roleSet.add(role);
            });
        });

        return roleSet;
    }
}
