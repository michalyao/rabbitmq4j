package rpc;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RPCServer {
    public static final String RPC_QUEUE_NAME = "rpc_queue";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            // queue
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            // qos
            channel.basicQos(1);
            System.out.println("[x] Awaiting RPC request.");
            // consumer
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    String response = "";

                    String msg = new String(body, StandardCharsets.UTF_8);
                    int n = Integer.parseInt(msg);
                    System.out.println("[.] fib(" + msg + ")");
                    response += fib(n);
                    channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));
                    channel.basicAck(envelope.getDeliveryTag(), false); // ack for msg received
                }
            };
            // consumer
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int fib(int n) {
        if (n <= 0) return 0;
        if (n == 1 || n == 2) {
            return 1;
        } else {
            return fib(n);
        }
    }
}
