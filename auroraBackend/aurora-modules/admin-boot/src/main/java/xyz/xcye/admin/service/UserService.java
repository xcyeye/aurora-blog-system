package xyz.xcye.admin.service;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import xyz.xcye.admin.api.feign.ArticleFeignService;
import xyz.xcye.admin.api.feign.EmailFeignService;
import xyz.xcye.admin.dto.EmailVerifyAccountDTO;
import xyz.xcye.admin.enums.GenderEnum;
import xyz.xcye.admin.po.User;
import xyz.xcye.admin.pojo.RolePermissionRelationshipPojo;
import xyz.xcye.admin.pojo.RolePojo;
import xyz.xcye.admin.pojo.SiteSettingPojo;
import xyz.xcye.admin.pojo.UserPojo;
import xyz.xcye.admin.properties.AdminDefaultProperties;
import xyz.xcye.admin.vo.RoleVO;
import xyz.xcye.admin.vo.UserVO;
import xyz.xcye.amqp.comstant.AmqpExchangeNameConstant;
import xyz.xcye.amqp.comstant.AmqpQueueNameConstant;
import xyz.xcye.api.mail.sendmail.entity.StorageSendMailInfo;
import xyz.xcye.api.mail.sendmail.enums.SendHtmlMailTypeNameEnum;
import xyz.xcye.api.mail.sendmail.service.SendMQMessageService;
import xyz.xcye.api.mail.sendmail.util.AccountInfoUtils;
import xyz.xcye.api.mail.sendmail.util.StorageEmailVerifyUrlUtil;
import xyz.xcye.article.pojo.ArticlePojo;
import xyz.xcye.aurora.properties.AuroraProperties;
import xyz.xcye.aurora.util.UserUtils;
import xyz.xcye.auth.constant.AuthRedisConstant;
import xyz.xcye.core.dto.JwtUserInfo;
import xyz.xcye.core.entity.R;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.exception.email.EmailException;
import xyz.xcye.core.exception.user.UserException;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.ConvertObjectUtils;
import xyz.xcye.core.util.DateUtils;
import xyz.xcye.core.util.JSONUtils;
import xyz.xcye.core.util.id.GenerateInfoUtils;
import xyz.xcye.core.util.lambda.AssertUtils;
import xyz.xcye.data.entity.Condition;
import xyz.xcye.data.entity.PageData;
import xyz.xcye.data.util.PageUtils;
import xyz.xcye.message.pojo.EmailPojo;
import xyz.xcye.message.vo.EmailVO;

import java.util.*;

/**
 * @author qsyyke
 */

@Slf4j
@Service
public class UserService {

    private final String bindEmailKey = "bindEmail";
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailFeignService emailFeignService;
    @Autowired
    private AuroraProperties auroraProperties;
    @Autowired
    private AuroraProperties.AuroraAccountProperties auroraAccountProperties;
    @Autowired
    private AdminDefaultProperties adminDefaultProperties;
    @Autowired
    private SendMQMessageService sendMQMessageService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AuroraUserService auroraUserService;
    @Autowired
    private AuroraSettingService auroraSettingService;
    @Autowired
    private SiteSettingService siteSettingService;

    @Autowired
    private PermissionRelationService permissionRelationService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ArticleFeignService articleFeignService;

    @Autowired
    private AuroraProperties.AuroraDefaultUserInfoProperties auroraDefaultUserInfoProperties;

    @Transactional(rollbackFor = Exception.class)
    public void insertUser(UserPojo user)
            throws UserException {
        // ???????????????????????????
        AssertUtils.stateThrow(!existsUsername(user.getUsername()),
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_EXIST));
        // ??????????????????
        setUserProperties(user);
        auroraUserService.insert(BeanUtils.copyProperties(user, User.class));
        // ?????????????????????????????????????????????

        List<String> defaultRoleList = auroraProperties.getDefaultRoleList();
        if (defaultRoleList == null || defaultRoleList.isEmpty()) {
            return;
        }

        defaultRoleList.forEach(roleName -> {
            RolePojo rolePojo = new RolePojo();
            rolePojo.setName(roleName);
            RoleVO roleVO = roleService.queryOneRole(rolePojo);
            if (roleVO == null) {
                return;
            }
            RolePermissionRelationshipPojo relationshipPojo = new RolePermissionRelationshipPojo();
            relationshipPojo.setUserUidArr(Collections.singletonList(user.getUid()));
            relationshipPojo.setRoleUidArr(Collections.singletonList(roleVO.getUid()));
            permissionRelationService.insertUserRoleBatch(relationshipPojo);
        });

        // ???????????????????????????????????????????????????
        if (auroraDefaultUserInfoProperties != null) {

            if (StringUtils.hasLength(auroraDefaultUserInfoProperties.getSiteInfo())) {
                // ??????????????????
                SiteSettingPojo siteSettingPojo = new SiteSettingPojo();
                siteSettingPojo.setUserUid(user.getUid());
                siteSettingPojo.setParamName(user.getUid() + "SiteInfo");
                siteSettingPojo.setParamValue(auroraDefaultUserInfoProperties.getSiteInfo().replaceAll("auUsernameua", user.getUsername()));
                siteSettingService.insertSiteSetting(siteSettingPojo);
            }

            if (StringUtils.hasLength(auroraDefaultUserInfoProperties.getNavbarInfo())) {
                SiteSettingPojo navbarInfoPojo = new SiteSettingPojo();
                navbarInfoPojo.setUserUid(user.getUid());
                navbarInfoPojo.setParamName(user.getUid() + "NavbarInfo");
                navbarInfoPojo.setParamValue(auroraDefaultUserInfoProperties.getNavbarInfo().replaceAll("auUserUidua", user.getUid() + ""));
                siteSettingService.insertSiteSetting(navbarInfoPojo);
            }

            if (StringUtils.hasLength(auroraDefaultUserInfoProperties.getPageInfo())) {
                SiteSettingPojo pagePojo = new SiteSettingPojo();
                pagePojo.setUserUid(user.getUid());
                pagePojo.setParamName(user.getUid() + "AllPageInfo");
                pagePojo.setParamValue(auroraDefaultUserInfoProperties.getPageInfo().replaceAll("auUserUidua", user.getUid() + ""));
                siteSettingService.insertSiteSetting(pagePojo);
            }

            // ??????????????????
            if (StringUtils.hasLength(auroraDefaultUserInfoProperties.getWelcomeArticle())) {
                ArticlePojo articlePojo = new ArticlePojo();
                articlePojo.setUserUid(user.getUid());
                articlePojo.setDelete(false);
                articlePojo.setPublish(true);
                articlePojo.setOriginalArticle(true);
                // articlePojo.setCategoryNames("?????????");
                articlePojo.setTitle("HI " + user.getUsername());
                articlePojo.setContent(auroraDefaultUserInfoProperties.getWelcomeArticle());
                articleFeignService.insertArticle(articlePojo);
            }
        }
    }

    @Transactional
    public int updateUser(UserPojo user) throws UserException {
        Objects.requireNonNull(user, "?????????????????????null");
        // ????????????????????????
        Optional.ofNullable(user.getPassword()).ifPresent(t -> user.setPassword(null));

        if (StringUtils.hasLength(user.getUsername()) && existsUsername(user.getUsername())) {
            //throw new UserException(ResponseStatusCodeEnum.PERMISSION_USER_EXIST);
            // ????????????????????????????????????
            user.setUsername(null);
        }
        // ????????????????????????????????????userUid???????????????????????????
        String username = getUsername(user.getUid());
        int updateNum = auroraUserService.updateById(BeanUtils.copyProperties(user, User.class));
        if (updateNum == 1) {
            removeUserDetailsFromRedis(username);
        }
        return updateNum;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????username,originPwd,newPwd?????????????????????????????????????????????username???secretKey
     * @return
     */
    public int updatePassword(UserPojo userPojo) {
        String username = userPojo.getUsername();
        String originPwd = userPojo.getOriginPwd();
        String newPwd = userPojo.getNewPwd();
        AssertUtils.stateThrow(StringUtils.hasLength(username), () -> new UserException("?????????????????????"));
        // ??????????????????????????????
        User user = queryByUsernameContainPassword(username);
        AssertUtils.stateThrow(user != null, () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST));
        // ????????????????????????
        boolean matches = passwordEncoder.matches(originPwd, user.getPassword());
        AssertUtils.stateThrow(matches, () -> new UserException("????????????"));

        // ????????????
        user.setPassword(passwordEncoder.encode(newPwd));
        int updateNum = auroraUserService.updateById(user);
        if (updateNum == 1) {
            removeUserDetailsFromRedis(user.getUsername());
        }
        return updateNum;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????email???????????????email????????????????????????????????????????????????????????????
     * ???html??????email???
     * @param userPojo
     * @return
     */
    public int forgotPassword(UserPojo userPojo) {

        return 0;
    }

    @Transactional
    public Integer physicalDeleteUser(long uid) {
        return auroraUserService.deleteById(uid);
    }

    public Integer queryTotalUserCount(UserPojo user) {
        return auroraUserService.countByWhere(BeanUtils.copyProperties(user, User.class));
    }

    public Integer logicDeleteUser(long uid) {
        UserPojo pojo = new UserPojo();
        pojo.setDelete(true);
        pojo.setUid(uid);
        return updateUser(pojo);
    }

    public PageData<UserVO> queryAllByCondition(Condition<Long> condition) {
        return PageUtils.copyPageDataResult(auroraUserService.queryListByCondition(condition), UserVO.class);
    }

    public UserVO queryUserByUid(long uid) {
        return BeanUtils.copyProperties(auroraUserService.queryById(uid), UserVO.class);
    }

    public User queryByUsernameContainPassword(String username) {
        return auroraUserService.queryOne(new User(){{
            setUsername(username);
        }});
    }

    public UserVO queryOneData(UserPojo pojo) {
        return BeanUtils.copyProperties(auroraUserService.queryOne(BeanUtils.copyProperties(pojo, User.class)), UserVO.class);
    }

    public User queryByUidContainPassword(long uid) {
        return auroraUserService.queryById(uid);
    }

    public UserVO queryUserByUsername(String username) {
        User user = new User();
        user.setUsername(username);
        return BeanUtils.copyProperties(auroraUserService.queryOne(user), UserVO.class);
    }

    /**
     * ????????????????????????????????????????????????
     * @param pojo
     * @return
     * @throws BindException
     * @throws EmailException
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public int bindingEmail(UserPojo pojo) throws BindException, EmailException {
        String email = pojo.getEmailNumber();
        Long userUid = pojo.getUid();
        JwtUserInfo currentUser = UserUtils.getCurrentUser();
        AssertUtils.stateThrow(currentUser != null, () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_LOGIN));
        // ??????????????????????????????uid????????????????????????uid??????
        AssertUtils.stateThrow(currentUser.getUserUid().equals(pojo.getUid()), () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_ILLEGAL_ACCESS));
        AssertUtils.stateThrow(StringUtils.hasLength(email), () -> new EmailException(ResponseStatusCodeEnum.PARAM_IS_INVALID));
        // ????????????aurora-message??????????????????email???uid????????????
        EmailPojo emailPojo = new EmailPojo();
        emailPojo.setEmail(email);
        emailPojo.setUserUid(userUid);
        R r = emailFeignService.queryByEmailNumber(emailPojo);
        EmailVO queriedEmailInfo = JSONUtils.parseObjFromResult(ConvertObjectUtils.jsonToString(r), "data", EmailVO.class);

        // AssertUtils.ifNullThrow(queriedEmailInfo,
        //         () -> new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_NOT_EXISTS));
        // AssertUtils.ifNullThrow(queriedEmailInfo.getEmail(),
        //         () -> new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_NOT_EXISTS));
        // ????????????????????????????????????
        if (queriedEmailInfo == null || queriedEmailInfo.getUid() == null) {
            R insertEmailR = emailFeignService.insertEmail(emailPojo);
            if (insertEmailR.getSuccess()) {
                // ????????????
                R r1 = emailFeignService.queryByEmailNumber(emailPojo);
                queriedEmailInfo = JSONUtils.parseObjFromResult(ConvertObjectUtils.jsonToString(r1), "data", EmailVO.class);
            }else {
                throw new EmailException("??????????????????");
            }
        }
        AssertUtils.ifNullThrow(queriedEmailInfo, () -> new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_FAIL_BINDING));
        if (queriedEmailInfo.getUserUid() != null && !Objects.equals(queriedEmailInfo.getUserUid(), userUid)) {
            // ???????????????????????????
            UserVO otherUserInfo = queryUserByUid(queriedEmailInfo.getUserUid());
            if (otherUserInfo != null && otherUserInfo.getVerifyEmail()) {
                throw new EmailException("??????????????????????????????");
            }
        }
        UserPojo userPojo = new UserPojo();
        userPojo.setEmailUid(queriedEmailInfo.getUid());
        userPojo.setUid(userUid);
        // ???????????????????????????
        UserVO userVO = queryUserByUid(userPojo.getUid());
        // ????????????????????????????????????
        userVO.setEmailUid(queriedEmailInfo.getUid());
        userVO.setVerifyEmail(false);
        // ??????????????????????????????
        AssertUtils.stateThrow(!userVO.getDelete(), () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_DELETE));

        int updateUserNum = updateUser(userPojo);
        if (updateUserNum == 1) {
            sendVerifyEmail(userVO, queriedEmailInfo);
        }

        // ??????redis????????????
        return updateUserNum;
    }

    /**
     * ??????????????????????????????????????????
     * @param username
     * @return
     */
    private boolean existsUsername(String username) {
        Condition<Long> condition = Condition.instant(username, null, null);
        return !auroraUserService.queryListByCondition(condition).getResult().isEmpty();
    }

    private String getUsername(Long userUid) {
        UserVO userVO = queryUserByUid(userUid);
        return userVO == null ? "" : userVO.getUsername();
    }

    private void setUserProperties(UserPojo user) {
        user.setDelete(false);
        user.setVerifyEmail(false);
        user.setAccountLock(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUid(GenerateInfoUtils.generateUid(auroraProperties.getSnowFlakeWorkerId(),auroraProperties.getSnowFlakeDatacenterId()));

        if (!StringUtils.hasLength(user.getNickname())) {
            user.setNickname(adminDefaultProperties.getNickname());
        }

        if (!StringUtils.hasLength(user.getAvatar())) {
            user.setAvatar(adminDefaultProperties.getAvatar());
        }

        // ????????????????????????????????????????????????(0)
        user.setGender(Optional.ofNullable(user.getGender()).orElse(GenderEnum.SECRET));
    }

    private void sendVerifyEmail(UserVO userVO, EmailVO emailVO) throws BindException {
        String verifyAccountUrl = AccountInfoUtils.generateVerifyUrl(userVO.getUid(),
                bindEmailKey, userVO.hashCode(), auroraAccountProperties.getMailVerifyAccountPrefixPath());
        long time = new Date().getTime();
        if (auroraAccountProperties.getMailVerifyAccountExpirationTime() != null) {
            time = time + auroraAccountProperties.getMailVerifyAccountExpirationTime();
        }else {
            // TODO ???????????????????????????????????????????????????10??????
            time = time + (10 * 60 * 1000);
        }
        String expirationTimeStr = DateUtils.format(new Date(time));
        EmailVerifyAccountDTO verifyAccountInfo = EmailVerifyAccountDTO.builder()
                .userUid(userVO.getUid())
                .expirationTime(auroraAccountProperties.getMailVerifyAccountExpirationTime())
                .verifyAccountUrl(verifyAccountUrl)
                .expirationTimeStr(expirationTimeStr)
                .receiverEmail(emailVO.getEmail()).subject(null).build();

        List<Map<SendHtmlMailTypeNameEnum, Object>> list = new ArrayList<>();
        Map<SendHtmlMailTypeNameEnum, Object> map = new HashMap<>();
        map.put(SendHtmlMailTypeNameEnum.VERIFY_ACCOUNT, verifyAccountInfo);
        list.add(map);

        StorageSendMailInfo mailInfo = new StorageSendMailInfo();
        mailInfo.setReceiverEmail(emailVO.getEmail());
        mailInfo.setSendType(SendHtmlMailTypeNameEnum.VERIFY_ACCOUNT);
        mailInfo.setSubject(userVO.getUsername() + " ???????????????????????????");
        mailInfo.setUserUid(userVO.getUid());

        // ??????????????????????????????redis????????????
        boolean storageVerifyUrlToRedis = StorageEmailVerifyUrlUtil.storageVerifyUrlToRedis(bindEmailKey, userVO.hashCode(),
                auroraAccountProperties.getMailVerifyAccountExpirationTime(), userVO.getUid());
        if (!storageVerifyUrlToRedis) {
            throw new UserException("??????????????????");
        }
        sendMQMessageService.sendCommonMail(mailInfo, AmqpExchangeNameConstant.AURORA_SEND_MAIL_EXCHANGE,
                "topic", AmqpQueueNameConstant.SEND_HTML_MAIL_ROUTING_KEY, list);
    }

    private void removeUserDetailsFromRedis(String username) {
        // ????????????????????????redis??????userService??????
        redisTemplate.delete(AuthRedisConstant.USER_DETAILS_CACHE_PREFIX + username);
    }
}
