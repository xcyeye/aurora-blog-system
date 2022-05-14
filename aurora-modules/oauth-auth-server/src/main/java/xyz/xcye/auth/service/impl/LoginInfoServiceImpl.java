package xyz.xcye.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import xyz.xcye.auth.dao.LoginInfoMapper;
import xyz.xcye.auth.po.LoginInfo;
import xyz.xcye.auth.service.LoginInfoService;
import xyz.xcye.auth.vo.LoginInfoVO;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.DateUtils;
import xyz.xcye.data.entity.Condition;
import xyz.xcye.data.entity.PageData;
import xyz.xcye.data.util.PageUtils;

import java.util.Arrays;

/**
 * @author qsyyke
 * @date Created in 2022/5/13 23:10
 */

@Service
public class LoginInfoServiceImpl implements LoginInfoService {

    @Autowired
    private LoginInfoMapper loginInfoMapper;

    @Override
    public int deleteByPrimaryKey(Long uid) {
        Assert.notNull(uid, "uid不能为null");
        return loginInfoMapper.deleteByPrimaryKey(uid);
    }

    @Override
    public int deleteByUidBatch(Long[] uids) {
        Assert.notNull(uids, "批量删除记录，uids不能为null");
        final int[] successDeleteNum = {0};
        Arrays.stream(uids).forEach(uid -> {
            successDeleteNum[0] = successDeleteNum[0] + loginInfoMapper.deleteByPrimaryKey(uid);
        });
        return successDeleteNum[0];
    }

    @Override
    public int insertSelective(LoginInfo record) {
        Assert.notNull(record, "登录信息不能为null");
        record.setStatus(false);
        return loginInfoMapper.insertSelective(record);
    }

    @Override
    public PageData<LoginInfoVO> selectByCondition(Condition<Long> condition) {
        Assert.notNull(condition, "查询条件不能为null");
        return PageUtils.pageList(condition, t -> loginInfoMapper.selectByCondition(condition), LoginInfoVO.class);
    }

    @Override
    public LoginInfoVO selectByUsername(String username) {
        return BeanUtils.getSingleObjFromList(loginInfoMapper.selectByCondition(Condition.instant(username)), LoginInfoVO.class);
    }

    @Override
    public int updateByPrimaryKeySelective(LoginInfo record) {
        Assert.notNull(record, "登录信息不能为null");
        record.setUpdateTime(DateUtils.format());
        return loginInfoMapper.updateByPrimaryKeySelective(record);
    }
}