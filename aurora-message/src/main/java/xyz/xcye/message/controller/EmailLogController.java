package xyz.xcye.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.xcye.common.annotaion.ResponseResult;
import xyz.xcye.common.dos.EmailLogDO;
import xyz.xcye.common.dto.PaginationDTO;
import xyz.xcye.common.entity.result.ModifyResult;
import xyz.xcye.common.entity.result.R;
import xyz.xcye.common.valid.Insert;
import xyz.xcye.common.valid.Update;
import xyz.xcye.message.service.EmailLogService;

import javax.validation.groups.Default;
import java.math.BigInteger;
import java.util.List;

/**
 * 操作au_email_log表的controller
 * @author qsyyke
 */

@Api(tags = "邮件发送日志相关操作")
@RequestMapping("/message/emailLog")
@RestController
public class EmailLogController {

    @Autowired
    private EmailLogService emailLogService;

    @ApiOperation(value = "插入邮件发送日志",notes = "插入邮件发送日志")
    @ResponseResult
    @PostMapping("/insert")
    public ModifyResult insertEmailLog(@Validated({Insert.class, Default.class}) EmailLogDO emailLog) {
        return emailLogService.insertEmailLog(emailLog);
    }

    @ApiOperation(value = "根据uid更新邮件发送日志")
    @ResponseResult
    @PutMapping("/update")
    public ModifyResult updateEmailLog(@Validated({Update.class}) EmailLogDO emailLog) throws BindException {
        return emailLogService.updateEmailLog(emailLog);
    }

    @ApiOperation(value = "删除uid对应邮件发送日志")
    @ResponseResult
    @DeleteMapping("/delete/{uid}")
    public ModifyResult deleteEmailLog(@PathVariable("uid") long uid) {
        return emailLogService.deleteEmailLog(uid);
    }

    @ApiOperation(value = "查询所有邮件发送日志")
    @ResponseResult
    @GetMapping("/queryAll")
    public List<EmailLogDO> queryAllEmailLog(EmailLogDO emailLog, PaginationDTO pagination) {
        if (emailLog == null) {
            emailLog = new EmailLogDO();
        }

        if (pagination == null) {
            pagination = new PaginationDTO();
        }

        return emailLogService.queryAll(emailLog,pagination);
    }
}