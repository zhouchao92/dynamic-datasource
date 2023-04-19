package com.zhou.datasource;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

/**
 * 默认数据源配置信息：主库配置 ， 从库公共配置
 */
@Slf4j
public class DefaultDataSourceProperties {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    /**
     * 要启用PSCache
     */
    @Value("${dataSource.maxPoolPreparedStatementPerConnectionSize:20}")
    private Integer maxPoolPreparedStatementPerConnectionSize;

    /**
     * 初始化时建立物理连接的个数
     */
    @Value("${dataSource.initialSize:10}")
    private Integer initialSize;

    /**
     * 最小连接池数量
     */
    @Value("${dataSource.minIdle:5}")
    private Integer minIdle;

    /**
     * 最大连接池数量
     */
    @Value("${dataSource.maxActive:100}")
    private Integer maxActive;

    /**
     * 获取连接时最大等待时间，单位毫秒
     */
    @Value("${dataSource.maxWait:60000}")
    private Long maxWait;

    /**
     * 申请连接时执行validationQuery检测连接是否有效
     */
    @Value("${dataSource.testOnBorrow:true}")
    private Boolean testOnBorrow;

    /**
     * 归还连接时执行validationQuery检测连接是否有效
     */
    @Value("${dataSource.testOnReturn:false}")
    private Boolean testOnReturn;

    /**
     * 建议配置为true，不影响性能，并且保证安全性
     */
    @Value("${dataSource.testWhileIdle:false}")
    private Boolean testWhileIdle;

    /**
     * Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接
     */
    @Value("${dataSource.timeBetweenEvictionRunsMillis:1800000}")
    private Long timeBetweenEvictionRunsMillis;

    /**
     * 连接保持空闲而不被驱逐的最小时间
     */
    @Value("${dataSource.minEvictableIdleTimeMillis:1800000}")
    private Long minEvictableIdleTimeMillis;

    /**
     * 是否缓存preparedStatement，也就是PSCache
     */
    @Value("${dataSource.poolPreparedStatements:true}")
    private Boolean poolPreparedStatements;

    /**
     * 插件配置监控统计用的stat、log4j、wall
     */
    @Value("${dataSource.filters:stat,wall}")
    private String filters;

    /**
     * 主数据源加载
     */
    public DataSource dataSource(Filter wallFilter) throws Exception {
        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWait);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setPoolPreparedStatements(poolPreparedStatements);
        List<Filter> proxyFilters = dataSource.getProxyFilters();
        if (filters.contains("wall")) {
            proxyFilters.add(wallFilter);
        }
        dataSource.setFilters(filters);
        dataSource.setConnectionInitSqls(Collections.singletonList("set names utf8mb4;"));
        log.info("初始化数据源信息dataSource={}", dataSource.toString());
        dataSource.init();
        log.info("DataSource init success!!!!!!!");
        return dataSource;
    }
}
