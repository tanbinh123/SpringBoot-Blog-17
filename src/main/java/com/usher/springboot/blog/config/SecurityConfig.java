package com.usher.springboot.blog.config;

import com.usher.springboot.blog.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author: Usher
 * @Description:
 * security配置类
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)//启用方法安全控制
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String KEY = "usherblog.com";

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CustomUserService customUserService() {
        return new CustomUserService();
    }

    /**
     * BCrypt加密
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * 解决UserDetailsService无法注入问题
     * Override this method to expose a UserDetailsService
     * created from configure(AuthenticationManagerBuilder) as a bean.
     * @return
     */
/*    @Bean
    public UserDetailsService userDetailsService() {
        return super.userDetailsService();
    }*/

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder); // 设置密码加密方式
        return authenticationProvider;
    }

    /**
     * 自定义配置
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/css/**", "/js/**", "/fonts/**", "/index").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/admins/**").hasRole("ADMIN")
                .and()
                .formLogin()   //基于 Form 表单登录验证
                .loginPage("/login").failureUrl("/login-error") // 自定义登录界面
                .and().rememberMe().key(KEY) // 启用 remember me
                .and().exceptionHandling().accessDeniedPage("/403");  // 处理异常，拒绝访问就重定向到 403 页面
        http.csrf().ignoringAntMatchers("/h2-console/**"); // 禁用 H2 控制台的 CSRF 防护
        http.headers().frameOptions().sameOrigin(); // 允许来自同一来源的H2 控制台的请求
    }

    /**
     * 认证信息管理
     * @param auth
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsService);
        auth.authenticationProvider(authenticationProvider());
    }
}
