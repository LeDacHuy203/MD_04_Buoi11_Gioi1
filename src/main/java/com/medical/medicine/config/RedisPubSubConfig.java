package com.medical.medicine.config;

import com.medical.medicine.pubsub.AlertSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    public static final String CHANNEL_NAME = "pharmacy-alerts";

    @Bean
    public ChannelTopic alertTopic() {
        return new ChannelTopic(CHANNEL_NAME);
    }

    /**
     * Dang ky Container lang nghe tin nhan tu Redis Channel 'pharmacy-alerts'
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            AlertSubscriber alertSubscriber,
            ChannelTopic alertTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(alertSubscriber, alertTopic);
        return container;
    }
}
