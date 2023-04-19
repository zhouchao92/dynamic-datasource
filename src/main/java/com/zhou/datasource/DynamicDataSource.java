package com.zhou.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallFilter;
import com.zhou.mock.MockData;
import com.zhou.util.HeadThreadLocal;
import com.zhou.util.SpringContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DynamicDataSource
 * 多数据源核心实现
 * MyBatis 根据 lookUpKey 获取数据源连接
 *
 * @author 周超
 * @since 2022/6/18 12:16
 */
@Slf4j
public class DynamicDataSource extends AbstractDataSource implements InitializingBean {

    private Map<Object, Object> targetDataSources;

    private Object defaultTargetDataSource;

    private volatile boolean lenientFallback = true;

    private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

    private Map<Object, DataSource> resolvedDataSources;

    private DataSource resolvedDefaultDataSource;

    private volatile boolean autoInitDatasource = false;

    private final Map<String, Object> dbNameKeyMapping = new ConcurrentHashMap<>();

    static class DbNameLock {
        private static final Map<Object, Object> locks = new HashMap<>();

        public static Object getLock(Object key) {
            Object dbNameLock = locks.get(key);
            if (dbNameLock == null) {
                synchronized (DbNameLock.class) {
                    return locks.computeIfAbsent(key, k -> new Object());
                }
            }
            return dbNameLock;
        }
    }


    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        this.targetDataSources = targetDataSources;
    }

    public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
        this.defaultTargetDataSource = defaultTargetDataSource;
    }

    public void setLenientFallback(boolean lenientFallback) {
        this.lenientFallback = lenientFallback;
    }

    public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
        this.dataSourceLookup = dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup();
    }

    public void addDataSource(HashMap<Object, Object> dataSourceMap) {
        dataSourceMap.forEach((k, value) -> {
            Object lookupKey = resolveSpecifiedLookupKey(k);
            DataSource dataSource = this.resolveSpecifiedDataSource(value);
            this.resolvedDataSources.put(lookupKey, dataSource);
        });
    }

    public void setAutoInitDatasource(boolean autoInitDatasource) {
        this.autoInitDatasource = autoInitDatasource;
    }

    public void addOrRefreshDataSource(Object key, DataSource dataSource) {
        Object lookupKey = resolveSpecifiedLookupKey(key);
        DataSource source = resolvedDataSources.get(lookupKey);
        this.resolvedDataSources.put(lookupKey, dataSource);
        if (source instanceof DruidDataSource) {
            ((DruidDataSource) source).close();
        }
    }

    public void addOrRefreshDataSource(Object key, String dataSourceName) {
        Object lookupKey = resolveSpecifiedLookupKey(key);
        DataSource dataSource = this.dataSourceLookup.getDataSource(dataSourceName);
        this.resolvedDataSources.put(lookupKey, dataSource);
    }

    public void removeDataSource(Object lookupKey) {
        this.resolvedDataSources.remove(lookupKey);
    }

    public void removeAllDataSource() {
        resolvedDataSources.keySet().forEach(key -> this.resolvedDataSources.remove(key));
    }

    public void afterPropertiesSet() {
        if (this.targetDataSources == null) {
            throw new IllegalArgumentException("Property 'targetDataSources' is required");
        } else {
            this.resolvedDataSources = new HashMap<>(this.targetDataSources.size());
            this.targetDataSources.forEach((key, value) -> {
                Object lookupKey = this.resolveSpecifiedLookupKey(key);
                DataSource dataSource = this.resolveSpecifiedDataSource(value);
                this.resolvedDataSources.put(lookupKey, dataSource);
            });
            if (this.defaultTargetDataSource != null) {
                this.resolvedDefaultDataSource = this.resolveSpecifiedDataSource(this.defaultTargetDataSource);
            }

        }
    }

    protected Object resolveSpecifiedLookupKey(Object lookupKey) {
        return lookupKey;
    }

    protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
        if (dataSource instanceof DataSource) {
            return (DataSource) dataSource;
        } else if (dataSource instanceof String) {
            return this.dataSourceLookup.getDataSource((String) dataSource);
        } else {
            throw new IllegalArgumentException("Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
        }
    }

    public Map<Object, DataSource> getResolvedDataSources() {
        Assert.state(this.resolvedDataSources != null, "DataSources not resolved yet - call afterPropertiesSet");
        return Collections.unmodifiableMap(this.resolvedDataSources);
    }

    public DataSource getResolvedDefaultDataSource() {
        return this.resolvedDefaultDataSource;
    }

    public Connection getConnection() throws SQLException {
        return this.determineTargetDataSource().getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return this.determineTargetDataSource().getConnection(username, password);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return iface.isInstance(this) ? (T) this : this.determineTargetDataSource().unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || this.determineTargetDataSource().isWrapperFor(iface);
    }

    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        Object lookupKey = this.determineCurrentLookupKey();
        DataSource dataSource = this.resolvedDataSources.get(lookupKey);

        // lookupKey not null but datasource not init
        if (lookupKey != null && dataSource == null && autoInitDatasource) {
            dataSource = autoInit(lookupKey);
        }
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.resolvedDefaultDataSource;
        }

        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        } else {
            return dataSource;
        }
    }

    private DataSource autoInit(Object lookupKey) {
        log.info("开始初始化数据源 key：{}", lookupKey);

        // 根据 lookupKey 获取数据源信息
        DataSourceProperties dataSourceProperties = MockData.getDataSourceProperties(lookupKey.toString());

        String dbName = dataSourceProperties.getDbName();
        if (dbName != null) {
            Object o = dbNameKeyMapping.get(dbName);
            if (o != null) {
                DataSource dataSource = this.resolvedDataSources.get(o);
                if (dataSource != null) {
                    this.resolvedDataSources.put(lookupKey, dataSource);
                    return dataSource;
                }
            }
        } else {
            return null;
        }
        WallFilter bean = SpringContextHelper.getBean(WallFilter.class);
        log.info("数据源初始化成功 url：{}", dataSourceProperties.getUrl());
        synchronized (DbNameLock.getLock(dbName)) {
            DataSource dataSource = this.resolvedDataSources.get(lookupKey);
            if (dataSource == null) {
                dataSource = DataSourceInit.dataSource(dataSourceProperties, this, bean);
                if (dataSource != null) {
                    dbNameKeyMapping.put(dbName, lookupKey);
                }
            }
            return dataSource;
        }
    }

    protected Object determineCurrentLookupKey() {
        return HeadThreadLocal.getInstance().getDataId();
    }

}
