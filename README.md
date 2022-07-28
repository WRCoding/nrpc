# NRPC
![image](https://user-images.githubusercontent.com/34570474/172417396-22ad9c58-c75e-4c50-b34c-9e2a0c03eb0e.png)

# Nrpc
Nrpc，是一个类似dubbo的简易RPC框架，实现了服务注册，负载均衡和重连服务等功能，可以仅通过注解就可以调用远程服务

## 功能介绍
Nrpc的核心是Nrpc-core,所有实现都在这里面

基础需要有个对外提供服务的接口类，同时服务实现了该接口类，该项目事例中提供了一个对外的接口

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cab4ea41c8ca48e490d4e2e696190daa~tplv-k3u1fbpfcp-watermark.image?)

### 注解@EnableNrpc
**@EnableNrpc注解**用在客户端中，标明启用Nrpc

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e7292a7acf2c4c03921ff6aa53379c25~tplv-k3u1fbpfcp-watermark.image?)

### 注解@NService
**@Nservice注解**用在了对外提供服务的接口实现类中，表明该类可以对外提供服务，可以远程调用给类。注解有个必填的属性**ServiceName**，来唯一标明当前服务

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/932d0f6fb88a43eeb9dc14679751482c~tplv-k3u1fbpfcp-watermark.image?)

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2c5739f3f92c44abab360ade3b9290d2~tplv-k3u1fbpfcp-watermark.image?)

在服务启动后，Nrpc会自动发现使用该注解的类，根据serviceName将其注册到Zookeeper中，并启动Netty，等待服务调用

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ae8ec7a729484516b112c6a9afefcc6d~tplv-k3u1fbpfcp-watermark.image?)

### 注解@Ncall

**@Ncall注解**用于客户端远程调用中，该注解的作用类似于dubbo的@Reference，当客户端使用远程调用某个服务的接口时，只需要引用该服务提供的接口，加上该注解，然后写上远程的服务的**ServiceName**。就可以像调用本地方法一样，调用远程服务的接口

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/11e5d94c169249d09713d0e33513493c~tplv-k3u1fbpfcp-watermark.image?)


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5a812d4194a844b0bbcf8a3df0c65060~tplv-k3u1fbpfcp-watermark.image?)

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8fb83bb016144396a6317c490ff14714~tplv-k3u1fbpfcp-watermark.image?)

### SPI
在写这个项目的时候，通过SPI使用插件扩展，可以扩展注册中心，路由处理器，序列化方式，这些都有默认实现，也可以通过插件自定义实现，SPI没有用Java默认的，而是选择了跟dubbo一样的实现方式，通过key-value的方式来加载

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5e9a71a031794b3493f0b2135c6f5d04~tplv-k3u1fbpfcp-watermark.image?)

### 注册中心
注册中心使用的是zookeeper，也可以拓展实现Redis，Nacos等，每个带有 **@NService**的类，在服务启动的时候，都会把相关数据生成path，然后在zookeeper创建成临时节点

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1fd653eb9f22430ba39a936730275bab~tplv-k3u1fbpfcp-watermark.image?)

### 代理
注解 **@Ncall** 在注入接口类的时候，实际注入的是该接口的代理类，通过代理类向远程服务发起方法调用，对外屏蔽了细节

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/27bf531cc3f645dbb516b65283537508~tplv-k3u1fbpfcp-watermark.image?)
实现代理的方式有JDK和Cglib，默认使用JDK

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8a5cfc20c75e4833aaafbd845f40138c~tplv-k3u1fbpfcp-watermark.image?)

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2da4fb8db6844b0096c0cc958dd3af13~tplv-k3u1fbpfcp-watermark.image?)
### 序列化
目前仅仅实现了JSON方式的序列化，可以拓展实现protobuf，kyro等方式的序列化

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/05d101566e6545a98b038899c3386ecd~tplv-k3u1fbpfcp-watermark.image?)


### 路由处理
当一个远程服务是集群模式，有多台服务器时，可通过路由处理器来选取其中一台，目前有轮训和随机获取，可以拓展其他方法，比如一致性哈希等，默认使用随机获取

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/52eb7f17c010485a8b818e202cc911ef~tplv-k3u1fbpfcp-watermark.image?)


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f833e5a8e3294c12b2c6f975e38e4ffe~tplv-k3u1fbpfcp-watermark.image?)
### 心跳检测/重连
Nrpc实现了心跳检测和重连功能，心跳检测确认调用方和被调用方的连接状态。重连功能让调用方在连接的远程服务挂了后，在一定时间内重新寻找可用的服务器进行连接。

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6cd38e48766b46d9b2c84e55a10ac7e4~tplv-k3u1fbpfcp-watermark.image?)

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/665028014f2147fe91c40ab1cbb9e591~tplv-k3u1fbpfcp-watermark.image?)

