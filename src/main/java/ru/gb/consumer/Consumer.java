package ru.gb.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Consumer {

    private static final String EXCHANGE_NAME = "news_exchange";
    interface Callback{
        void run();
    }

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String queueName = channel.queueDeclare().getQueue();

        String defaultRoutingKey = "programming.#";
        channel.queueBind(queueName, EXCHANGE_NAME, defaultRoutingKey);

        listenCommand(channel, queueName, () -> System.exit(0));

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.printf("New article: %n%s", message);
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    private static void listenCommand(Channel channel, String queueName, Callback callback) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        new Thread(() -> {
            try {
                boolean isRun = true;
                while (isRun) {
                    String command = reader.readLine();
                    String[] splitCommand = command.split("\\s+", 2);
                    switch (splitCommand[0]) {
                        case "/add":
                            channel.queueBind(queueName, EXCHANGE_NAME, "#."+splitCommand[1]);
                            System.out.println("You subscribed to theme " + splitCommand[1]);
                            break;
                        case "/del":
                            channel.queueUnbind(queueName, EXCHANGE_NAME, "#."+splitCommand[1]);
                            System.out.println("You unsubscribed from theme " + splitCommand[1]);
                            break;
                        case "/exit":
                            isRun = false;
                            break;
                    }
                }
                callback.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
