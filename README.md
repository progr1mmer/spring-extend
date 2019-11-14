# Spring-extend
spring-extend算是个人在spring相关框架使用过程中的一些总结，用尽量简洁的代码提供较为实用的扩展，有任何疑问或问题，欢迎随时指出。

## spring-extend-common
* **说明**
  * 公共模块

## spring-extend-security
* **说明**
  * 基于spring-security的扩展，更改默认的内存存储为mysql存储
  * 提供登录拦截，访问授权功能
  * 默认配置下，除绝大部分前端静态资源可匿名访问外其他资源均需要登录后才可访问，在此基础上默认还需要进行访问控制的URL前缀有`/admin/**, /auth/**`，如不符合需求可自行配置，参考[ExtendPathInterceptConfig](https://github.com/Progr1mmer/spring-extend/blob/master/spring-extend-common/src/main/java/com/progr1mmer/security/config/ExtendPathInterceptConfig.java)
* **鉴权模型**
  * 表auth_user, auth_role, auth_resource, auth_user_role, auth_role_resource，分别存储用户、角色、资源、用户角色、角色资源数据
  * 用户登录的时候根据其相关角色获取相关资源的访问权限，并保存在当前会话中，在后续的请求中，根据其权限对受保护的资源进行访问控制
  * 需要注意的是auth_resource中的URL需要在有访问控制的URL前缀之下，后端才会进行访问控制，例如：用户管理模块的URL为`/admin/user/**`，在`/admin/**`之下，此时对于该模块的访问后端会进行访问控制，如果为`/user/**`, 则登录后即可访问
  * 引入thymeleaf模板语言，可控制页面菜单的展示与否，做到前后端双重认证，保护敏感数据
* **使用**
  * 初始化数据库，脚本: [spring-extend-security.sql](https://github.com/Progr1mmer/spring-extend/blob/master/script/spring-extend-security.sql)
  * 复制源码到自己的项目中，或下载项目到本地后install，然后在自己的项目中引入相关依赖即可
  * 为避免包冲突，此项目依赖的scope均为provided，请自行引入其他基础包，明细可参考[spring-extend-security.pom.xml](https://github.com/Progr1mmer/spring-extend/blob/master/spring-extend-security/pom.xml)
  * 确保此项目在包扫描路径下
  * 引入thymeleaf模板语言，可使用以下方式`<li th:if="${session.user.check('child_menu_resource_manage')}"><a href="#">用户管理</a></li>`控制页面菜单的展示与否
  * 默认登录页面为GET /login (自行提供)，登录验证为POST /login (项目自带，默认账户username=admin, password=admin)，登出为GET /logout (项目自带)
  * 数据库user表密码使用org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder进行强加密
  * 提供手机验证码登录

## spring-extend-oauth2-server
* **说明**
  * 基于spring-security-oauth2的扩展，更改默认的内存存储为mysql存储
  * 提供标准的oauth2授权，不清楚oauth2的请移步[OAuth 2.0](http://www.ruanyifeng.com/blog/2019/04/oauth-grant-types.html)
  * 也可做为统一的认证中心，参考示例[spring-extend-test-oauth2-server](https://github.com/Progr1mmer/spring-extend/tree/master/spring-extend-test-oauth2-server)
* **鉴权模型**
  * 同spring-security-oauth2的表结构，并包含[spring-extend-security](https://github.com/Progr1mmer/spring-extend#spring-extend-security)的表结构
  * 包括授权码模式、简化模式、密码模式、客户端凭证模式授权
* **使用**
  * 初始化数据库，脚本: [spring-extend-oauth2.sql](https://github.com/Progr1mmer/spring-extend/blob/master/script/spring-extend-oauth2.sql), [spring-extend-security.sql](https://github.com/Progr1mmer/spring-extend/blob/master/script/spring-extend-security.sql)
  * 复制源码到自己的项目中，或下载项目到本地后install，然后在自己的项目中引入相关依赖即可
  * 为避免包冲突，此项目依赖的scope均为provided，请自行引入其他基础包，明细可参考[spring-extend-oauth2-server.pom.xml](https://github.com/Progr1mmer/spring-extend/blob/master/spring-extend-oauth2-server/pom.xml)
  * 确保此项目在包扫描路径下
  * 部分授权方式需要先进行登录操作，默认登录页面为GET /login (项目自带，页面可自定义)，登录验证为POST /login (项目自带，默认账户username=admin, password=admin)，登出为GET /logout (项目自带)
  * 数据库user表密码使用org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder进行强加密
  * 提供手机验证码登录

## spring-extend-oauth2-client
* **说明**
  * 基于spring-cloud-starter-oauth2单点登录的扩展
  * 其他同[spring-extend-security](https://github.com/Progr1mmer/spring-extend#spring-extend-security)
* **完整示例**
  * 参考[spring-extend-test-oauth2-server](https://github.com/Progr1mmer/spring-extend/tree/master/spring-extend-test-oauth2-server), [spring-extend-test-oauth2-client1](https://github.com/Progr1mmer/spring-extend/tree/master/spring-extend-test-oauth2-client1), [spring-extend-test-oauth2-client2](https://github.com/Progr1mmer/spring-extend/tree/master/spring-extend-test-oauth2-client2)
  
## 持续更新（欢迎提出对其他组件扩展的需求）...
