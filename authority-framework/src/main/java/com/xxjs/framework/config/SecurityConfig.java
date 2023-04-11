package com.xxjs.framework.config;

import com.xxjs.framework.security.filter.JwtAuthenticationTokenFilter;
import com.xxjs.framework.security.handle.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * spring security配置
 *
 * @author xxjs
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter
{

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;


    //对 AuthenticationManager 进行重写（逻辑不动）只是为了注入到 spring 容器中，供我们进行使用
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        //调用原来的逻辑
        return super.authenticationManagerBean();
    }

    /**
     * token认证过滤器
     */
    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //放行的只有 get 请求
                .antMatchers("/captchaImage","/login").anonymous();

        // 添加Logout filter
        http.logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler);

        //将 jwt 过滤器放到 username认证 filter 前面，具体原因去看 security
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        http.csrf().disable(); //不然的话，login还是访问不了，因为 login 是post异步请求，403

    }
}