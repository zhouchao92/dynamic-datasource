package com.zhou.datasource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceProperties {

    private String dataId;

    private String groupId;

    private String merchantName;

    private String url;

    private String dbName;

    private String username;

    private String password;

    private String decryKey = "";

    /**
     * 要启用PSCache
     */
    private Integer maxPoolPreparedStatementPerConnectionSize = 20;

    /**
     * 初始化时建立物理连接的个数
     */
    private Integer initialSize = 10;

    /**
     * 最小连接池数量
     */
    private Integer minIdle = 5;

    /**
     * 最大连接池数量
     */
    private Integer maxActive = 100;

    /**
     * 获取连接时最大等待时间，单位毫秒
     */
    private Long maxWait = 60000L;

    /**
     * 申请连接时执行validationQuery检测连接是否有效
     */
    private Boolean testOnBorrow = true;

    /**
     * 归还连接时执行validationQuery检测连接是否有效
     */
    private Boolean testOnReturn = false;

    /**
     * 建议配置为true，不影响性能，并且保证安全性
     */
    private Boolean testWhileIdle = false;

    /**
     * Destroy线程会检测连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接
     */
    private Long timeBetweenEvictionRunsMillis = 1800000L;

    /**
     * 连接保持空闲而不被驱逐的最小时间
     */
    private Long minEvictableIdleTimeMillis = 1800000L;

    /**
     * 是否缓存preparedStatement，也就是PSCache
     */
    private Boolean poolPreparedStatements = true;

    /**
     * 插件配置监控统计用的stat、log4j、wall
     */
    private String filters = "stat,wall";

}
