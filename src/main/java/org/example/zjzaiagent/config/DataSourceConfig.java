package org.example.zjzaiagent.config;


import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${postgres.datasource.driver-class-name}")
    private String pgDriverClassName;

    @Value("${postgres.datasource.url}")
    private String pgUrl;

    @Value("${postgres.datasource.username}")
    private String pgUsername;

    @Value("${postgres.datasource.password}")
    private String pgPassword;

    //mysql数据源
    @Primary
    @Bean(name = "dataSource")
    public DataSource mysqlDataSource(){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(mysqlDriverClassName);
        hikariDataSource.setJdbcUrl(mysqlUrl);
        hikariDataSource.setUsername(mysqlUsername);
        hikariDataSource.setPassword(mysqlPassword);
        return hikariDataSource;
    }

    //pg数据源
    @Bean(name = "pgDataSource")
    public DataSource pgDataSource(){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(pgDriverClassName);
        hikariDataSource.setJdbcUrl(pgUrl);
        hikariDataSource.setUsername(pgUsername);
        hikariDataSource.setPassword(pgPassword);
        return hikariDataSource;
    }

}
