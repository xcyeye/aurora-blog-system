package xyz.xcye.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.xcye.admin.exception.UserException;
import xyz.xcye.admin.service.UserService;
import xyz.xcye.admin.vo.RoleVO;
import xyz.xcye.admin.vo.UserVO;
import xyz.xcye.common.annotaion.ResponseResult;
import xyz.xcye.common.dos.UserAccountDO;
import xyz.xcye.common.dos.UserDO;
import xyz.xcye.common.dto.PaginationDTO;
import xyz.xcye.common.entity.result.ModifyResult;
import xyz.xcye.common.valid.Insert;
import xyz.xcye.common.valid.Update;

import javax.validation.groups.Default;
import java.util.List;

/**
 * @author qsyyke
 */


@RequestMapping("/admin/user")
@RestController
@Api(tags = "用户相关写操作")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("")
    @ResponseResult
    @ApiOperation(value = "添加新用户")
    public ModifyResult insertUser(@Validated({Insert.class, Default.class})UserDO userDO,
                                   @Validated({Insert.class, Default.class}) UserAccountDO userAccountDO) throws BindException, UserException, InstantiationException, IllegalAccessException {
        return userService.insertUser(userDO,userAccountDO);
    }

    @PutMapping("")
    @ResponseResult
    @ApiOperation(value = "修改用户信息")
    public ModifyResult updateUser(@Validated({Update.class, Default.class})UserDO userDO) throws UserException {
        return userService.updateUser(userDO);
    }

    @PostMapping("/deleteStatus")
    @ResponseResult
    @ApiOperation(value = "修改用户删除状态")
    public ModifyResult updateUserDeleteStatus(@Validated({Update.class, Default.class})UserDO userDO) throws InstantiationException, IllegalAccessException, UserException {
        return userService.updateDeleteStatus(userDO);
    }

    @DeleteMapping("/{uid}")
    @ResponseResult
    @ApiOperation(value = "删除用户信息")
    public ModifyResult deleteUserByUid(@PathVariable("uid") long uid) throws UserException, InstantiationException, IllegalAccessException {
        return userService.deleteByUid(uid);
    }

    @GetMapping("/{uid}")
    @ResponseResult
    @ApiOperation(value = "通过uid查询用户信息")
    public UserVO insertUser(@PathVariable("uid") long uid) throws InstantiationException, IllegalAccessException {
        return userService.queryByUid(uid);
    }

    @GetMapping("")
    @ResponseResult
    @ApiOperation(value = "查询所有满足要求的用户信息")
    public List<UserVO> insertUser(UserDO userDO, PaginationDTO pagination) throws InstantiationException, IllegalAccessException {
        return userService.queryAll(userDO,pagination);
    }
}