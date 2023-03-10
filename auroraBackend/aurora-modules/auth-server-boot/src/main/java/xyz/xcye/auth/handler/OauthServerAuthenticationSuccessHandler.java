package xyz.xcye.auth.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import xyz.xcye.auth.constant.OauthJwtConstant;
import xyz.xcye.auth.util.OauthServerUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author qsyyke
 * @date Created in 2022/5/8 08:34
 */

@Component
public class OauthServerAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Cookie cookie = new Cookie(OauthJwtConstant.COOKIE_STORAGE_LOGIN_SUCCESS_STATUS, "true");
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        OauthServerUtils.success(request, response, "登录成功");
    }
}
