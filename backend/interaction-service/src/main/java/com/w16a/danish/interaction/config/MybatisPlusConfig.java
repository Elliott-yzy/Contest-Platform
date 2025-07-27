package com.w16a.danish.interaction.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author Eddy ZHANG
 * @date 2025/04/01
 * @description MyBatis Plus configuration class
 */
@Configuration
@MapperScan("com.w16a.danish.interaction.mapper")
public class MybatisPlusConfig {

    /**
     *
     * MyBatis Plus interceptor bean for pagination
     * @return {@link MybatisPlusInterceptor }
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
