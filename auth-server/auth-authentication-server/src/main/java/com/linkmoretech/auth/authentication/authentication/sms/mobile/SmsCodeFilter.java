package com.linkmoretech.auth.authentication.authentication.sms.mobile;

import com.linkmoretech.auth.authentication.authentication.MultiReadHttpServletRequest;
import com.linkmoretech.auth.authentication.authentication.ValidateFailureHandler;
import com.linkmoretech.auth.authentication.component.ValidateCodeManage;
import com.linkmoretech.auth.common.construct.ParamsConstruct;
import com.linkmoretech.auth.common.util.HttpRequestBodyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @Author: alec
 * Description: 短信验证码过滤器，过滤所有以短信登录到请求
 * @date: 16:15 2019-06-19
 */
@Slf4j
public abstract class SmsCodeFilter extends OncePerRequestFilter implements InitializingBean {

    private ValidateFailureHandler validateFailureHandler;
    /**
     * 定义需要拦截到请求
     * */
    protected Set<String> urls = new HashSet<>();

    private ValidateCodeManage validateCodeManage;

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    private Integer smsType;

    public SmsCodeFilter (ValidateCodeManage validateCodeManage, ValidateFailureHandler validateFailureHandler, Integer smsType) {
        this.validateCodeManage = validateCodeManage;
        this.validateFailureHandler = validateFailureHandler;
        this.smsType = smsType;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        boolean action = false;
        log.info("过滤uris {}", urls);
        for (String url : urls) {
            if (pathMatcher.match(url, request.getRequestURI())) {
                action = true;
            }
        }
        if (action) {
            try {
                MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);
                redisValidate(multiReadHttpServletRequest);
                log.info("短信校验码过滤器，拦截请求");
                chain.doFilter(multiReadHttpServletRequest, response);
            } catch (ValidateCodeException e) {
                validateFailureHandler.onAuthenticationFailure(request, response, e);
                return;
            }
        } else {
            chain.doFilter(request, response);
        }

    }

    private void redisValidate( MultiReadHttpServletRequest multiReadHttpServletRequest) throws  ValidateCodeException {
        Map<String, Object> loginParams = HttpRequestBodyUtil.getHttpBody(multiReadHttpServletRequest);
        String clientId = (String)loginParams.get(ParamsConstruct.CLIENT_ID);
        String mobile = (String)loginParams.get(ParamsConstruct.MOBILE_PARAMS);
        String CODE_FIELD = "validateCode";
        String code = (String)loginParams.get(CODE_FIELD);
        String validateCode =  validateCodeManage.findValidateCode(clientId, this.smsType, mobile);
        log.info("code {} - validate {} ", code, validateCode);
        if (StringUtils.isEmpty(validateCode)) {
            throw new ValidateCodeException("验证码不存在");
        }
        if (!validateCode.equals(code)) {
            throw new ValidateCodeException("验证码不正确");
        }
        validateCodeManage.deleteValidateCode(this.smsType, clientId, mobile);
        log.info("login params {}" ,loginParams);
    }
}
