package com.zhou.mock;

import com.zhou.datasource.DataSourceProperties;

/**
 * MockData
 * 模拟获取数据库连接信息
 *
 * @author 周超
 * @since 2023/4/19 9:10
 */
public class MockData {

    public static DataSourceProperties getDataSourceProperties(String lookupKey) {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();

        dataSourceProperties.setDataId(lookupKey);
        dataSourceProperties.setGroupId(lookupKey);
        dataSourceProperties.setMerchantName(lookupKey);
        dataSourceProperties.setUrl("localhost");
        dataSourceProperties.setDbName(String.format("dynamic_datasource%s", lookupKey));
        dataSourceProperties.setUsername("root");
        dataSourceProperties.setPassword("root");

        return dataSourceProperties;
    }

}
