
## 首先搭建一个RabbitMQ服务

### 使用docker-compose搭建rabbitMQ
- 编写docker-compose文件

```yml
version: '3'
services:

  # rabbitMq服务
  breeze-rabbitmq:
    image: rabbitmq:3.9.13-management
    container_name: breeze-rabbitmq
    networks:
      - breeze-net
    hostname: breeze-rabbitmq
    restart: always
    ports:
      - "4369:4369"
      - "15672:15672" # client端通信口
      - "5672:5672" # 管理界面ui端口
      - "25672:25672" # server间内部通信口
    volumes:
      - "./docker/rabbitmq/data:/var/lib/rabbitmq"
      - "./docker/rabbitmq/log:/var/log/rabbitmq/log"

networks:
  breeze-net:
    external: false
```
- 启动docker compose，自动拉镜像启动 
```shell
    docker compose up breeze-rabbitmq -d
```
- 启动成功：
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9dbf4daf2676416ba09fd991ff4be6eb~tplv-k3u1fbpfcp-watermark.image?)

- 浏览器访问 [http://localhost:15672/](http://localhost:15672/)，如图所示

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cee4133aac574ebdabf472b3206effe9~tplv-k3u1fbpfcp-watermark.image?)

- 使用guest/guest 登录进来，在 Admin 菜单创建需要使用的账户 admin/admin 权限 administrator 
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d9a54772b2b749d49511ecc2e6fd7fb0~tplv-k3u1fbpfcp-watermark.image?)
- 给admin用户配置 权限
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/efe2922562a64ba0a7d80e57befa0dec~tplv-k3u1fbpfcp-watermark.image?)

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e1ccafe30dff4648ae0b145336adb27b~tplv-k3u1fbpfcp-watermark.image?)

- ***RabbitMQ单机版搭建完成（开发使用）***

### 整合SpringBoot使用DXL+TTL实现延迟队列

- 配置死信队列所需的常量
```java
package com.example.rabbitmq.demo.constants;

/**
 * @author breeze
 * @version 1.0
 * @createDate
 **/
public interface DeadMQConstants {

    String ORDER_QUEUE_KEY = "order.routing.key";

    String ORDER_DEAD_QUEUE = "order.dead.queue";

    String ORDER_DEAD_DIRECT_EXCHANGE = "order.dead.direct.exchange";

    String ORDER_QUEUE = "order.queue";

    String ORDER_DIRECT_EXCHANGE = "order.direct.exchange";

}
``` 

- 配置死信队列

```java
package com.example.rabbitmq.demo.config;

import com.example.rabbitmq.demo.constants.DeadMQConstants;
import com.google.common.collect.Maps;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author breeze
 * @version 1.0
 * @createDate
 **/
@Configuration
public class RabbitMQDeadConfig {


    /**
     * 死信队列
     *
     * @return org.springframework.amqp.core.Queue
     */
    @Bean
    public Queue orderDeadQueue() {
        return new Queue(DeadMQConstants.ORDER_DEAD_QUEUE);
    }

    /**
     * 死信交换机
     *
     * @return org.springframework.amqp.core.DirectExchange
     */
    @Bean
    public DirectExchange orderDeadDirectExchange() {
        return new DirectExchange(DeadMQConstants.ORDER_DEAD_DIRECT_EXCHANGE);
    }

    /**
     * 绑定死信队列到死信交换机
     *
     * @param orderDeadQueue
     * @param orderDeadDirectExchange
     * @return org.springframework.amqp.core.Binding
     */
    @Bean
    public Binding orderDeadBinding(@Qualifier("orderDeadQueue") Queue orderDeadQueue,
                                    @Qualifier("orderDeadDirectExchange") DirectExchange orderDeadDirectExchange) {
        return BindingBuilder.bind(orderDeadQueue).to(orderDeadDirectExchange).with(DeadMQConstants.ORDER_QUEUE_KEY);
    }

    /**
     * x-dead-letter-routing-key 路由key
     * x-dead-letter-exchange 死信交换机的name
     * x-message-ttl 也可以设置整个队列的过期时间
     * <p>
     * 定义业务队列  绑定死信交换机
     *
     * @return org.springframework.amqp.core.Queue
     */
    @Bean
    public Queue orderQueue() {
        Map<String, Object> args = Maps.newHashMap();
        args.put("x-dead-letter-exchange", DeadMQConstants.ORDER_DEAD_DIRECT_EXCHANGE);
        args.put("x-dead-letter-routing-key", DeadMQConstants.ORDER_QUEUE_KEY);
        return new Queue(DeadMQConstants.ORDER_QUEUE, true, false, false, args);
    }

    /**
     * 定义业务交换机
     *
     * @return org.springframework.amqp.core.DirectExchange
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(DeadMQConstants.ORDER_DIRECT_EXCHANGE);
    }

    /**
     * 绑定业务队列和业务交换机
     *
     * @param orderQueue
     * @param orderExchange
     * @return org.springframework.amqp.core.Binding
     */
    @Bean
    public Binding bindingOrder(@Qualifier("orderQueue") Queue orderQueue,
                                @Qualifier("orderExchange") DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(DeadMQConstants.ORDER_QUEUE_KEY);
    }

}

```
 - 测试类
```java
/**
 *
 * @return
 */
@Test
public void testSendDeadMsg() {
    String msg = "hello dead message";
    this.amqpTemplate.convertAndSend(
            DeadMQConstants.ORDER_DIRECT_EXCHANGE,
            DeadMQConstants.ORDER_QUEUE_KEY,
            msg,
            message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message.getMessageProperties().setExpiration("3000");
                return message;
            });
}
```
### 配置插件实现延迟队列
- 此时刚启动可见只创建direct、fanout、headers、topic 交换机

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/48d7ea2ba5924a49badc80b15a3d621b~tplv-k3u1fbpfcp-watermark.image?)

- 下载插件
https://www.rabbitmq.com/community-plugins.html
![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3f4745651cff4510adcb19667377f8ad~tplv-k3u1fbpfcp-watermark.image?)

- 把插件拷贝到容器中
```shell
    docker cp rabbitmq_delayed_message_exchange-3.10.0.ez breeze-rabbitmq:/plugins
    docker exec -it breeze-rabbitmq /bin/bash
```
![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e3550dfd786d47488cfd58319d1a3237~tplv-k3u1fbpfcp-watermark.image?)

- 启动插件
```shell
    rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

- 启动插件成功
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d574d8636fb146c8ac2bf3cfbf5645e5~tplv-k3u1fbpfcp-watermark.image?)

- 查看web控制台 已经可以创建延迟队列
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3a35cb2aaacc4e4eb14e9a5186cdcb75~tplv-k3u1fbpfcp-watermark.image?)

### 延迟队列插件实现延迟队列

- 常量
```
package com.example.rabbitmq.demo.constants;

/**
 * @author breeze
 * @version 1.0
 * @createDate
 **/
public interface DelayedMQConstants {

    String ORDER_DELAYED_ROUTING_KEY = "order.delayed.routing.key";

    String ORDER_DELAYED_DIRECT_EXCHANGE = "order.delayed.direct.exchange";

    String ORDER_DELAYED_QUEUE = "order.delayed.queue";
}
```

- 配置
```java
package com.example.rabbitmq.demo.config;

import com.example.rabbitmq.demo.constants.DelayedMQConstants;
import com.google.common.collect.Maps;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author breeze
 * @version 1.0
 * @createDate
 **/
@Configuration
public class RabbitMQDelayedConfig {


    /**
     * 延时队列
     *
     * @return org.springframework.amqp.core.Queue
     */
    @Bean
    public Queue orderDelayedQueue() {
        return new Queue(DelayedMQConstants.ORDER_DELAYED_QUEUE);
    }

    /**
     * 延迟交换机
     *
     * @return org.springframework.amqp.core.CustomExchange
     */
    @Bean
    public CustomExchange orderDelayDirectExchange() {
        Map<String, Object> args = Maps.newHashMap();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DelayedMQConstants.ORDER_DELAYED_DIRECT_EXCHANGE, "x-delayed-message", true, false, args);
    }

    /**
     * 绑定队列到这个延迟交换机上
     *
     * @param queue
     * @param customExchange
     * @return org.springframework.amqp.core.Binding
     */
    @Bean
    public Binding bindingNotify(@Qualifier("orderDelayedQueue") Queue queue,
                                 @Qualifier("orderDelayDirectExchange") CustomExchange customExchange) {
        return BindingBuilder.bind(queue).to(customExchange).with(DelayedMQConstants.ORDER_DELAYED_ROUTING_KEY).noargs();
    }

}
```
- 测试
```java
/**
 *
 * @return
 */
@Test
public void testSendDelayedMsg() {
    String msg = "hello delay message";
    amqpTemplate.convertAndSend(
            DelayedMQConstants.ORDER_DELAYED_DIRECT_EXCHANGE,
            DelayedMQConstants.ORDER_DELAYED_ROUTING_KEY,
            msg,
            message -> {
                message.getMessageProperties().setDelay(6000);
                return message;
            });

}
```
