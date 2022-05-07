package com.example.rabbitmq.demo.constants;

/**
 * 死信队列常量
 *
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
