# OPCUA 

> 最近 老板让我 做一个 类似 物联网的 实时 展示 机床 状态 生产数据 的一个系统
>
> 本身我是做Java 的   一开始  是在网上 找到了  PLC 的 但是有些数据PLC 里面没有
>
> 后续找到 了 eclipse 旗下的 [milo]([eclipse/milo: Eclipse Milo™ - an open source implementation of OPC UA (IEC 62541). (github.com)](https://github.com/eclipse/milo))   通过OPC UA 连接
>
> 踩了很多坑 在这里  写一下  参考了许多大佬的 文章 



### 1.一开始 搜索到 OPC 

参考[ioufev](https://home.cnblogs.com/u/ioufev/) 大佬的   [Java实现OPC通信](https://www.cnblogs.com/ioufev/p/9928971.html) 一文  

下载了 KEPServer V6  里面详细介绍了 OPC  与 KEPServer V6 的使用教程

为我后面 的 OPC UA  搭好了基础

### 2.后续听说 使用 OPC UA 可以

找到 了 eclipse 旗下的 [milo]([eclipse/milo: Eclipse Milo™ - an open source implementation of OPC UA (IEC 62541). (github.com)](https://github.com/eclipse/milo))  

在网上找了许多资料  有很多大佬的资料  多多少少会有一些问题  

最后 选择了 [ 使用java的milo框架访问OPCUA服务的方法_玩火的稻草人的博客-CSDN博客](https://blog.csdn.net/yhj_911/article/details/107710566)   

- 第一次访问会出现 诸如

  ```java
  UaServiceFaultException: status=Bad_UserAccessDenied, message=User does not have permission to perform the requested operation.
  ```

  如果出现此异常   > 右击右下角KEP状态栏图标> OPC UA配置  > 受信任的客户端 > 找到刚生成的证书 打X的证书 选择信任 保存   再右击图标  > 重新初始化

- ```java
  UaServiceFaultException: status=Bad_CertificateUriInvalid, message=The URI specified in the ApplicationDescription does not match the URI in the Certificate.
  ```

  如果出现此类错误 找到  OpcUAClientRunner类里面的 createClient  方法

  ~~~java
  OpcUaClientConfig.builder()
          .setApplicationUri(" ")//这里的值
  ~~~
  
  和生成证书的  KeyStoreLoader 类里面的   填写一致
  ~~~java 
  SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                      .setCommonName("Eclipse Milo Example Client")
                      .setOrganization("SanQuanKeJi")
                      .setOrganizationalUnit("dev")
                      .setLocalityName("CryanNing")
                      .setStateName("CA")
                      .setCountryCode("US")
                      .setApplicationUri("urn:eclipse:milo:examples:client")//与这里对应
                      .addDnsName("localhost")
                      .addIpAddress("127.0.0.1");       
  
  ~~~

### 

