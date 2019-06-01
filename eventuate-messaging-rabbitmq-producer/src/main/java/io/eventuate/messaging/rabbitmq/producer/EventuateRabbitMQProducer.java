package io.eventuate.messaging.rabbitmq.producer;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class EventuateRabbitMQProducer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private Connection connection;
  private Channel channel;

  public EventuateRabbitMQProducer(String url) {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(url);
    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public CompletableFuture<?> send(String topic, String key, String body) {
    try {
      AMQP.BasicProperties bp = new AMQP.BasicProperties.Builder().headers(Collections.singletonMap("key", key)).build();

      channel.exchangeDeclare(topic, "fanout");
      channel.basicPublish(topic, key, bp, body.getBytes("UTF-8"));

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return CompletableFuture.completedFuture(null);
  }

  public void close() {
    try {
      channel.close();
      connection.close();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
    }
  }
}
