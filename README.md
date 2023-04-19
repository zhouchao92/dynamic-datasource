# 多租户下动态数据源切换
基于 MyBatis 的 session 需要获取到 datasource，自定义修改 datasource 获取连接的方法（ThreadLocal定向修改index），ThreadLocal 存储需要加载的数据库的 lookup key，再根据缓存获取对应的数据库连接信息（已加载的数据库信息会存储在jvm本地缓存）

## 配置信息
```yaml
spring:
  # 主数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/study?useSSL=false&useUnicode=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root
    # 多数据源配置
    multi:
      auto-init: true
      enable: true
db:
  dynamic:
    datasource:
      # 多数据源连接格式
      path: jdbc:mysql://%s:3306/%s?useSSL=false&useUnicode=true
```

## 工作流程
自定义过滤器（com.zhou.filter.ServletTraceInfoAttachmentFilter）处理请求中携带的商户信息，将 dataId（唯一标识） 存储在 ThreadLocal，操作数据库时 MyBatis 需要获取到 SqlSession，SqlSessionFactory 会加载 DynamicDataSource 再获取数据库连接（com.zhou.datasource.DynamicDataSource.getConnection()），getConnection() 会通过 ThreadLocal 中存储的唯一标识信息拿对应的数据库连接信息，从而实现多数据源的切换。
