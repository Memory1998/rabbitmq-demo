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
 * 死信队列 交换机配置类
 *
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
