package com.medical.medicine.pubsub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);

            JsonNode jsonNode = objectMapper.readTree(messageBody);

            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "UNKNOWN";
            String content = jsonNode.has("message") ? jsonNode.get("message").asText() : messageBody;

            // In dinh dang chuan theo dung yeu cau de bai & anh minh hoa
            System.out.println("=== THÔNG BÁO DASHBOARD QUẢN LÝ ===");
            System.out.println("Type : " + type);
            System.out.println("Nội dung: " + content);
            System.out.println("====================================");

            log.info("Subscriber da nhan tin nhan tu channel '{}': type={}, message={}", channel, type, content);

        } catch (Exception e) {
            log.error("Loi khi xu ly message tu Redis Pub/Sub: ", e);
        }
    }
}
