# gmall（谷粒商城项目）
##新建一个demo程序，初始化项目
##编写一个基本的gmall-user模块
服务端口：8080 

接口服务如下：

查询所有用户：http://user.gmall.com:8080/getAllUser

根据用户id查询用户地址：http://user.gmall.com:8080/getAllUser

##工程结构分层
- gmall-parent (父工程，管理依赖)
- gmall-common-util (通用的jar包依赖)
- gmall-web-util (web前端jar包依赖管理)
- gmall-service-util (service服务jar包依赖管理)
- gmall-api (bean和intaface) 
- gmall-user-web (user的controller层)
- gmall-user-service (user的service服务层)

##分布式项目架构

将user拆成user-web和user-service两个模块

gmall-user-web 通讯端口：8070

gmall-user-service 通讯端口：8080
