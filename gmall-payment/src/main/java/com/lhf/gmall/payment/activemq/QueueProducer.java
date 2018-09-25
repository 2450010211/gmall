package com.lhf.gmall.payment.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * @author shkstart
 * @create 2018-09-25 10:20
 */
public class QueueProducer {
    public static void main(String[] args) {
        Connection connection = null;
        Session session = null;
        //消息提供者
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.234.128:61616");
        try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue heshui = session.createQueue("HESHUI");
            //消息提供者
            MessageProducer producer = session.createProducer(heshui);

            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText("老大口渴要喝水");
            //延迟和定时消息投递
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }finally {
            if(session!=null){
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
            if(connection != null){
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
