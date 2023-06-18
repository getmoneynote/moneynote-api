# 九快记账后台API

## 系统介绍
一个开源免费的记账解决方案，包括[后端](https://github.com/getmoneynote/moneynote-api)，[网页版](https://github.com/getmoneynote/moneywhere-user-fe)，[App](https://github.com/getmoneynote/moneywhere_user_flutter)，主要用于个人生活记账，开店收支记账，支持docker一键部署自己的记账程序。

![Screen](https://raw.githubusercontent.com/getmoneynote/moneynote-api/main/screencapture.png "Screen Shot")

[示例站点](https://demo.moneywhere.com)  注册邀请码 111111（6个1）

## 使用文档
[从零开始搭建完善的记账体系](https://sspai.com/post/58025)

[https://help.moneywhere.com](https://help.moneywhere.com)

[B站视频](https://www.bilibili.com/video/BV1vz4y1i7dz)

如遇到任何问题欢迎加入 QQ群: 639653091 讨论。

## 技术方案
采用前后端分离模式，[后端](https://github.com/getmoneynote/moneynote-api)使用Spring Boot 3框架，[网页版](https://github.com/getmoneynote/moneywhere-user-fe)使用Antd Pro，[App](https://github.com/getmoneynote/moneywhere_user_flutter)使用Flutter和Bloc。

## 个人部署
请参考[docker compose](https://github.com/getmoneynote/docker-compose-moneywhere)项目

## 主要功能

- 监控个人资产负债。
- 记录个人支出和收入。
- 支持多个账本记账
- 支持多币种
- 支持多种账本模板
- 支持账本的分类和标签管理
- 分组多用户记账
- 报表统计
- 支持docker compose一键部署

## ToDo
- 汇率和账本模板服务downgrade
- 完善代码最佳实践
- 完善分组功能
- 开发微信小程序
