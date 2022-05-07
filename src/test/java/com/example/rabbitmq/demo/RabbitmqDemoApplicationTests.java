package com.example.rabbitmq.demo;

import com.example.rabbitmq.demo.constants.DeadMQConstants;
import com.example.rabbitmq.demo.constants.DelayedMQConstants;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author breeze
 * @version 1.0
 * @createDate
 **/
@SpringBootTest
class RabbitmqDemoApplicationTests {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    void contextLoads() {
    }

    /**
     * 测试
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

    /**
     * 测试
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

}
