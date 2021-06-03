package com.wff.mall.ware.listener;

import com.rabbitmq.client.Channel;
import com.wff.common.to.mq.OrderTo;
import com.wff.common.to.mq.StockLockedTo;
import com.wff.mall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/2 22:32
 */
@Slf4j
@Service
@RabbitListener(queues = "${myRabbitmq.MQConfig.queues}")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 下单成功 库存解锁 接下来业务调用失败
     *
     *  只要解锁库存消息失败 一定要告诉服务解锁失败
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("接收到库存解锁信息");
        try {
            wareSkuService.unlockStock(to);
            // 执行成功的 回复 [仅回复自己的消息]
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 订单关闭后 发送的消息这里接收
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        log.info("接收到订单发来的解锁信息");
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
