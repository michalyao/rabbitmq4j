package singlemsg;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Worker {
    public static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println("[y] Wating for messages. Press CTRL+C to exit.");

        // 每次只发送一条消息，当消费者消费并ack后再发送，避免产生背压。
        channel.basicQos(1);
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // receive msg
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("[y] received: " + message);
                try {
                    doWork(message);
                } finally {
                    System.out.println("[y] Work done.");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
                super.handleDelivery(consumerTag, envelope, properties, body);
            }
        };
        boolean autoAck = false; // turn on ack
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, consumer);
    }

    private static void doWork(String task) {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
