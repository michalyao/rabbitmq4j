package log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Task {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connectio = factory.newConnection();
        Channel channel = connectio.createChannel();
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclare();
        System.out.println(declareOk.getQueue());
    }
}
