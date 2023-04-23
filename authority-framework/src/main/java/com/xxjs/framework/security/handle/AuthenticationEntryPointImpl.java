package com.xxjs.framework.security.handle;

import com.alibaba.fastjson2.JSON;
import com.xxjs.common.constant.HttpStatus;
import com.xxjs.common.core.domain.AjaxResult;
import com.xxjs.common.utils.ServletUtils;
import com.xxjs.common.utils.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * 判断请求是否携带了 token，并且该 token 是否正确的
 * 认证失败处理类 返回未授权
 * 认证失败之后，走的逻辑
 *
 * @author xxjs
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable
{
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException
    {
        int code = HttpStatus.UNAUTHORIZED;
        String msg = StringUtils.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());
        //返回前端相关信息
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
    }
}
