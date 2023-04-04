package com.xxjs.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * spring security配置
 *
 * @author xxjs
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //放行的只有 get 请求
                .antMatchers("/captchaImage","/login").anonymous();

        http.csrf().disable(); //不然的话，login还是访问不了，因为 login 是post异步请求，403

    }
}