## 什么是BI

BI（Business Intelligence），中文可译作商业智能，是一种用于支持业务决策和数据分析的技术、工具和流程的综合体系。BI的主要目标是帮助组织从大量的数据中提取有价值的信息，以可视化图表的方式，让人们更好地理解业务状况、制定战略和做出决策。
如下图：
![](https://cdn.nlark.com/yuque/0/2023/png/28467887/1692408574548-41466685-cfb4-4e94-a652-abee0fff24f9.png#averageHue=%23ded7d2&clientId=u80ce4461-63cb-4&from=paste&id=uadf5252e&originHeight=860&originWidth=1600&originalType=url&ratio=1.25&rotation=0&showTitle=false&status=done&style=none&taskId=ua1224246-c94f-4890-aeae-3e7197b3a79&title=)

主流BI平台：[帆软BI](https://www.finebi.com/)、[小马BI](https://bi.zhls.qq.com/#/)、[微软Power BI](https://powerbi.microsoft.com/zh-cn/)
[阿里云的BI平台](https://chartcube.alipay.com/)
传统的 BI 平台：

1. 手动上传数据
2. 手动选择分析所需的数据行和列（由数据分析师完成）
3. 需要手动选择所需的图表类型（由数据分析师完成）
4. 生成图表并保存配置

## 本项目的智能BI平台

区别于传统的BI，用户（数据分析者）只需要导入最原始的数据集，输入想要进行分析的目标（比如帮我分析一下网站的增长趋势)，就能利用AI自动生成一个符合要求的图表以及分析结论。此外，还会有图表管理、异步生成等功能。
**优点：让不会数据分析的用户也可以通过输入目标快速完成数据分析，大幅节约人力成本，将会用到 AI 接口生成分析结果**

## 项目架构图

### 1. 基础流程

基础流程：客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端利用AI服务处理客户端数据，保持到数据库，并生成图表。处理后的数据由业务后端发送给AI服务，AI服务生成结果并返回给后端，最终将结果返回给客户端展示。
要根据用户的输入生成图标，借助AI服务

![](https://cdn.nlark.com/yuque/0/2023/jpeg/28467887/1692409349028-b1fc1fcd-4cc8-4206-a01d-1466ff5ab4aa.jpeg)
上图的流程会出现一个问题：
假设一个 AI 服务生成图表和分析结果要等50秒，如果有大量用户需要生成图表，每个人都需要等待50秒，那么 AI 服务可能无法受这种压力。为了解决这个问题，可以采用消息队列技术。
这类以于在餐厅点餐时，为了避免顾客排队等待，餐厅会给顾客一个取餐号码，上顾客可以先去坐下或做其他事情，等到餐厅叫到他们的号码时再去领取餐点，这样就能节省等待时间。
同样地，通过消息队列，用户可以提交生成图表的请求，这些请求会进入队列，AI 服务会衣次处理队列中的请求，从而避免了同时处理大量请求造成的压力，同时也影更好地控制资源的使用。

### 2. 优化流程（异步化）

![](https://cdn.nlark.com/yuque/0/2023/jpeg/28467887/1692409924952-a19c055d-8f0a-476e-a637-70e26092ae95.jpeg)

优化流程（异步化）：客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端将请求事件放入消息队列，并为客户端生成取餐号，让要生成图表的客户端去排队，消息队列根据AI服务负载情况，定期检查进度，如果AI服务还能处理更多的图表生成请求，就向任务处理模块发送消息。
任务处理模块调用AI服务处理客户端数据，AI 服务异步生成结果返回给后端并保存到数据库，当后端的AI工服务生成完毕后，可以通过向前端发送通知的方式，或者通过业务后端监控数据库中图表生成服务的状态，来确定生成结果是否可用。若生成结果可用，前端即可获取并处理相应的数据，最终将结果返回给客户端展示。在此期间，用户可以去做自己的事情。

## 技术栈

### 后端

Java Spring Boot
MySQL数据库
MyBatis Plus及MyBatis X自动生成
Redis+Redisson限流
RabbitMQ消息队列
鱼聪明AI SDK(AI能力)
JDK线程池及异步化
Easy Excel表格数据处理
Swagger+Knife4j接口文档生成
Hutool、Apache Common Utils等工具库

## 业务流程

1. 用户输入
    1. 分析目标
    2. 上传原始数据(excel)
    3. 更精细地控制图表：比如图表类型、图表名你等
2. 后端校验
    1. 校验用户的输入是否合法（比如长度）
    2. 成本控制（次数统计和校验、鉴权等
3. 把处理后的数据输入给 AI 模型（调用AI接口），让 AI 模型给我们提供图表信息、结论文本
4. 图表信息（是一段 JSON 配置，是一设代码）、结论文本在前端进行展示

## 项目主要功能截图

![image.png](https://cdn.nlark.com/yuque/0/2023/png/28467887/1692445299927-04dfb341-11b6-4e10-a423-20564981d47d.png#averageHue=%23f8f8f8&clientId=u89c9280f-a31e-4&from=paste&height=465&id=u0eb604c9&originHeight=581&originWidth=1899&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=58693&status=done&style=none&taskId=ue1b6c798-9c5d-4eed-938f-6877219ad48&title=&width=1519.2)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/28467887/1692445333768-bb07b8df-ee51-4687-8619-a2e840669857.png#averageHue=%23f9f9f9&clientId=u89c9280f-a31e-4&from=paste&height=454&id=u00eafd62&originHeight=568&originWidth=1905&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=51907&status=done&style=none&taskId=u7695de8a-870c-44f1-a9a8-18ae640e982&title=&width=1524)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/28467887/1692445355888-31fcdf32-e19b-4b6e-abd1-a9251a1cb615.png#averageHue=%23f7f7f7&clientId=u89c9280f-a31e-4&from=paste&height=686&id=ucbec6fc3&originHeight=857&originWidth=1907&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=111919&status=done&style=none&taskId=u57e4b36b-f6b7-449e-838d-13172eebd0c&title=&width=1525.6)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/28467887/1692445400536-ddcd8be7-3894-486a-88b3-0a13949ad34c.png#averageHue=%23f9f9f9&clientId=u89c9280f-a31e-4&from=paste&height=576&id=ufaab22d5&originHeight=720&originWidth=1912&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=58570&status=done&style=none&taskId=ub9a6bc9d-9373-4e46-82b0-c93ae76edd3&title=&width=1529.6)

## 


