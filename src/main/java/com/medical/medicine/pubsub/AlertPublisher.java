package com.medical.medicine.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.medicine.dto.request.AlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertPublisher {

    public static final String DEFAULT_TOPIC = "pharmacy-alerts";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Gui thong bao den Redis Channel (Pub/Sub)
     * Du lieu duoc serialize sang JSON bang ObjectMapper
     */
    public String publishAlert(AlertMessage alertMessage) {
        String topic = (alertMessage.topicListen() != null && !alertMessage.topicListen().isBlank())
                ? alertMessage.topicListen()
                : DEFAULT_TOPIC;

        try {
            // Tao payload JSON gom type va message theo yeu cau de bai
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", alertMessage.type());
            payload.put("message", alertMessage.message());

            String jsonString = objectMapper.writeValueAsString(payload);

            // Publish message vao Redis Channel theo che do Fire-and-forget
            stringRedisTemplate.convertAndSend(topic, jsonString);

            log.info("Publisher da gui thong bao thanh cong den channel '{}': {}", topic, jsonString);
            return topic;

        } catch (Exception e) {
            log.error("Loi khi publish message den channel '{}': ", topic, e);
            throw new RuntimeException("Khong the gui thong bao qua Redis Pub/Sub", e);
        }
    }
}
