package com.zhou.datasource;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 自定义多数据库配置
 */
@Getter
@Component
public class DynamicDataProperties {

    // @Value("${db.dynamic.datasource.username:root}")
    // private String username;

    // @Value("${db.dynamic.datasource.password:root}")
    // private String password;

    @Value("${db.dynamic.source.path:jdbc:mysql://%s:3306/%s?useSSL=false&useUnicode=true}")
    private String path;

}
