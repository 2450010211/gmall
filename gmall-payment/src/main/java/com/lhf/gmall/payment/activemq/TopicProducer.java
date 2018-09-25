package com.lhf.gmall.payment.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * @author shkstart
 * @create 2018-09-25 11:32
 */
public class TopicProducer {
    public static void main(String[] args) {
        //发布订阅消息
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.234.128:61616");
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            //订阅消息持久化设置Id
            connection.setClientID("topicId");
            connection.start();
            //不能开启事务模式 是自动确认模式，不需客户端进行确认
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            //创建话题模式的消息
            Topic kaihui = session.createTopic("KAIHUI");
            //订阅消息持久化设置
            session.createDurableSubscriber(kaihui, "topicId");
            //话题模式的发布者
            MessageProducer producer = session.createProducer(kaihui);
            TextMessage textMessage = new ActiveMQTextMessage();
            //持久化
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            textMessage.setText("为中华之崛起而读书");

            producer.send(textMessage);

        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            if(session != null){
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
