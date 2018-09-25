package com.lhf.gmall.payment.activemq;

import com.alipay.api.domain.CraftsmanAssessment;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;

/**
 * @author shkstart
 * @create 2018-09-25 11:47
 */
public class TopicEmployeeConsumerA {

    public static void main(String[] args) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, "tcp://192.168.234.128:61616");
        try {
            Connection connection = connectionFactory.createConnection();
            //订阅消息持久化设置Id
            connection.setClientID("topicId");
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic kaihui = session.createTopic("KAIHUI");
            //TopicConsumer持久化
            MessageConsumer consumer = session.createDurableSubscriber(kaihui, "topicId");
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if(message instanceof TextMessage){
                        try {
                            String text = ((TextMessage) message).getText();
                            System.out.println(text + "TopicEmployeeConsumerA说:吃得苦中苦方为人上人");
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            //8、程序等待接收用户消息
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //9、关闭资源
            consumer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
