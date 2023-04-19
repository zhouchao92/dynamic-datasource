package com.zhou.config;

import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.zhou.datasource.DefaultDataSourceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.sql.DataSource;

@Configuration
@Conditional(DataSourceConfiguration.EnableDataSourceCondition.class)
public class DataSourceConfiguration {

    public static class EnableDataSourceCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            boolean b = !context.getEnvironment().getProperty("spring.datasource.multi.enable", Boolean.class, false);
            String url = context.getEnvironment().getProperty("spring.datasource.url", String.class, "");
            return b && url.length() > 0;
        }

    }

    @Bean
    public DefaultDataSourceProperties defaultDataSourceProperties() {
        return new DefaultDataSourceProperties();
    }

    @Bean
    @DependsOn("wallFilter")
    public DataSource dataSource(DefaultDataSourceProperties defaultDataSourceProperties, WallFilter wallFilter) throws Exception {
        return defaultDataSourceProperties.dataSource(wallFilter);
    }

    @Bean
    @DependsOn("wallConfig")
    @ConditionalOnMissingBean(WallFilter.class)
    public WallFilter wallFilter() {
        WallFilter filter = new WallFilter();
        filter.setConfig(wallConfig());
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean(WallConfig.class)
    public WallConfig wallConfig() {
        WallConfig config = new WallConfig();
        config.setMultiStatementAllow(true);
        return config;
    }
}
