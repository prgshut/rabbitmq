package ru.gb.producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Producer {
    private static final String EXCHANGE_NAME = "news_exchange";

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
            String baseRoutingKey = "programming.";


            boolean isRun = true;
            while (isRun) {

                System.out.println("write command");
                String command = reader.readLine();
                String[] splitCommand = command.split("\\s+", 2);
                switch (splitCommand[0]) {
                     case "/put":
                        String[] putSplit = splitCommand[1].split("\\s+", 2);
                        String routingKey = baseRoutingKey+putSplit[0];
                         System.out.println(routingKey);
                        channel.basicPublish(EXCHANGE_NAME, routingKey, null, putSplit[1].getBytes(StandardCharsets.UTF_8));
                        System.out.println("[X] putting to exchange");
                        break;
                    case "/exit":
                        isRun = false;
                        break;
                }
            }
        }
    }
}
