package com.xxjs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * exclude，排除此类的AutoConfig，即禁止 SpringBoot 自动注入数据源配置。
 * DataSourceAutoConfiguration.class 会自动查找 application.yml
 * 或者 properties 文件里的 spring.datasource.* 相关属性并自动配置单数据源「注意这里提到的单数据源」。
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AuthorityApplication
{
    public static void main(String[] args) {
        SpringApplication.run(AuthorityApplication.class);
        System.err.println("启动成功");
    }
}
