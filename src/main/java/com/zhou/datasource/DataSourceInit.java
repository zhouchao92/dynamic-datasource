package com.zhou.datasource;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallFilter;
import com.zhou.util.SpringContextHelper;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 利用Druid初始化数据源，数据源连接
 */
@Slf4j
public class DataSourceInit {

    public static DruidDataSource dataSource(DataSourceProperties dataSourceProperties, DynamicDataSource dynamicDataSource, WallFilter wallFilter) {
        DruidDataSource druidDataSource = dataSource(dataSourceProperties, wallFilter);
        if (druidDataSource != null) {
            String groupId = dataSourceProperties.getGroupId();
            if (groupId == null) {
                groupId = dataSourceProperties.getDataId();
            }
            dynamicDataSource.addOrRefreshDataSource(groupId, druidDataSource);
        }
        return druidDataSource;
    }

    public static DruidDataSource dataSource(DataSourceProperties dataSourceProperties, WallFilter wallFilter) {

        DynamicDataProperties dynamicDataProperties = SpringContextHelper.getBean(DynamicDataProperties.class);

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(String.format(dynamicDataProperties.getPath(), dataSourceProperties.getUrl(), dataSourceProperties.getDbName()));
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(dataSourceProperties.getMaxPoolPreparedStatementPerConnectionSize());
        dataSource.setInitialSize(dataSourceProperties.getInitialSize());
        dataSource.setMinIdle(dataSourceProperties.getMinIdle());
        dataSource.setMaxActive(dataSourceProperties.getMaxActive());
        dataSource.setMaxWait(dataSourceProperties.getMaxWait());
        dataSource.setTestOnBorrow(dataSourceProperties.getTestOnBorrow());
        dataSource.setTestOnReturn(dataSourceProperties.getTestOnReturn());
        dataSource.setTestWhileIdle(dataSourceProperties.getTestWhileIdle());
        dataSource.setTimeBetweenEvictionRunsMillis(dataSourceProperties.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(dataSourceProperties.getMinEvictableIdleTimeMillis());
        dataSource.setPoolPreparedStatements(dataSourceProperties.getPoolPreparedStatements());
        try {
            String filters = dataSourceProperties.getFilters();
            List<Filter> proxyFilters = dataSource.getProxyFilters();
            if (filters.contains("wall")) {
                proxyFilters.add(wallFilter);
            }
            dataSource.setFilters(filters);
            dataSource.setConnectionInitSqls(Collections.singletonList("set names utf8mb4;"));
            dataSource.init();
            return dataSource;
        } catch (SQLException e) {
            log.error("DataSource  init  failed -------", e);
            return null;
        }
    }
}
