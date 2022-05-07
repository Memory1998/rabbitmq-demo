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
