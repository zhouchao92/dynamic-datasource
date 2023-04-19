package com.zhou.config;

import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.zhou.datasource.DataSourceInit;
import com.zhou.datasource.DefaultDataSourceProperties;
import com.zhou.datasource.DynamicDataSource;
import com.zhou.datasource.MultiDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Optional;

@Configuration
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Conditional(DynamicDataSourceConfiguration.EnableMultiDataSourceCondition.class)
public class DynamicDataSourceConfiguration {

    @Value("${mybatis.mapper-locations:classpath*:mybatis/mapper/*.xml}")
    private String mapperLocations;

    @Value("${spring.datasource.multi.auto-init:false}")
    private Boolean autoInitDataSource;

    /**
     * 多数据源开关
     */
    public static class EnableMultiDataSourceCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return context.getEnvironment().getProperty("spring.datasource.multi.enable", Boolean.class, false);
        }

    }

    @Bean
    @ConfigurationProperties(prefix = "mybatis.configuration")
    public org.apache.ibatis.session.Configuration globalConfiguration() {
        return new org.apache.ibatis.session.Configuration();
    }

    @Bean(name = "wallFilter")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WallFilter wallFilter() {
        WallConfig wc = new WallConfig();
        wc.setMultiStatementAllow(true);
        WallFilter wallFilter = new WallFilter();
        wallFilter.setConfig(wc);
        return wallFilter;
    }

    @Bean
    @DependsOn("wallFilter")
    public DefaultDataSourceProperties defaultDataSourceProperties() {
        log.info("开始读取默认数据源配置");
        return new DefaultDataSourceProperties();
    }

    @Bean(name = "multiDataSourceProperties")
    public MultiDataSourceProperties multiDataSourceProperties() {
        return new MultiDataSourceProperties();
    }

    @Bean(name = "dynamicDataSource")
    @DependsOn("defaultDataSourceProperties")
    public DynamicDataSource dynamicDataSource(DefaultDataSourceProperties defaultDataSourceProperties,
                                               WallFilter wallFilter,
                                               MultiDataSourceProperties multiDataSourceProperties) throws Exception {
        log.info("开始初始化动态数据源数据源");
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(new HashMap<>());
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSourceProperties.dataSource(wallFilter));
        Optional.ofNullable(multiDataSourceProperties.getDataSources())
                .ifPresent(dataSources -> dataSources.forEach(
                        dataSourceProperties -> DataSourceInit.dataSource(dataSourceProperties, dynamicDataSource, wallFilter)));
        log.info("完成动态数据源初始化");
        dynamicDataSource.setAutoInitDatasource(autoInitDataSource);

        return dynamicDataSource;
    }

    @Bean(name = "multiDataSourceSqlSessionFactory")
    @DependsOn("dynamicDataSource")
    public SqlSessionFactory multiDataSourceSqlSessionFactory(@Qualifier("dynamicDataSource") DataSource dynamicDataSource,
                                                              org.apache.ibatis.session.Configuration configuration) throws Exception {
        log.info("开始初始化 SqlSessionFactory");
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();

        // 解决配置失效问题
        bean.setConfiguration(configuration);

        bean.setDataSource(dynamicDataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        log.info("完成初始化 SqlSessionFactory");
        return bean.getObject();
    }

    @Bean
    @DependsOn("dynamicDataSource")
    public DataSourceTransactionManager transactionManager(@Qualifier("dynamicDataSource") DataSource dynamicDataSource) {
        log.info("开始初始化 事务管理器");
        return new DataSourceTransactionManager(dynamicDataSource);
    }

}
