package xyz.xcye.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.xcye.admin.dao.AuroraSettingDao;
import xyz.xcye.admin.po.Setting;
import xyz.xcye.service.base.BaseService;

/**
 * @table setting <br/>
 * @description setting 数据表Service层 <br/>
 * @date 2022-12-30 15:46:26 <br/>
 * @author xcye <br/>
 */

@Service
public class AuroraSettingService extends BaseService<Setting> {
	@SuppressWarnings("unused")
	private AuroraSettingDao settingDao;
	
	@Autowired
    public void setInfoDao(AuroraSettingDao settingDao) {
        super.setBaseDao(settingDao);
        this.settingDao = settingDao;
    }
}