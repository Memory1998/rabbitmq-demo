package com.example.rabbitmq.demo.listener;

import com.example.rabbitmq.demo.constants.DeadMQConstants;
import com.example.rabbitmq.demo.constants.DelayedMQConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author breeze
 * @version 1.0
 * @createDate
 **/
@Slf4j
@Component
public class MQListener {

    @RabbitListener(queues = DelayedMQConstants.ORDER_DELAYED_QUEUE)
    public void receiveDelayedMsg(Message message, Channel channel) throws IOException {
        log.info(" msg: {}", new String(message.getBody(), "UTF-8"));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DeadMQConstants.ORDER_DEAD_QUEUE, durable = "true"),
                    exchange = @Exchange(name = DeadMQConstants.ORDER_DEAD_DIRECT_EXCHANGE, durable = "true", type = "direct"),
                    key = DeadMQConstants.ORDER_QUEUE_KEY
            )
    )
    public void receiveDeadMsg(Message message, Channel channel) throws IOException {
        log.info(" msg: {}", new String(message.getBody(), "UTF-8"));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
