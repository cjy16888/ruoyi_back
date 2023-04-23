package com.xxjs.framework.config;

import com.xxjs.framework.security.filter.JwtAuthenticationTokenFilter;
import com.xxjs.framework.security.handle.AuthenticationEntryPointImpl;
import com.xxjs.framework.security.handle.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * spring security配置
 *
 * @author xxjs
 */
//该注解的作用：允许在 方法上面添加注解，比如 @PreAuthorize("hasAuthority('system:dict:list')")  进行限制访问
//    拥有对应的权限方可以进行访问对应的资源
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter
{

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;
    @Autowired
    private AuthenticationEntryPointImpl unauthenticatedHandler;


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
                //anonymous 匿名访问，也就是可以不进行登录就可以进行访问的
                .antMatchers("/captchaImage","/login").anonymous()
                .anyRequest().authenticated();

        //基于 token，所以不需要session，取消session（取不取消没有影响，只不过是生成了没用的数据）
        //也就是登录的时候，不会生成 JSessionID，只是使用我们的  token 就可以了
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //认证 token 失败的 handler 逻辑
        http.exceptionHandling().authenticationEntryPoint(unauthenticatedHandler);

        // 添加Logout filter，自定义 logout 逻辑
        http.logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler);

        //将 jwt 过滤器放到 username认证 filter 前面，具体原因去看 security
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        http.csrf().disable(); //不然的话，login还是访问不了，因为 login 是post异步请求，403

    }
}