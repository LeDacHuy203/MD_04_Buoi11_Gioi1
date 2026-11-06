package com.medical.medicine.pubsub;

import com.medical.medicine.dto.request.AlertMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AlertPubSubIntegrationTest {

    @Autowired
    private AlertPublisher alertPublisher;

    @SpyBean
    private AlertSubscriber alertSubscriber;

    @Test
    @DisplayName("Kiem tra Redis Pub/Sub: Publisher gui alert -> Subscriber nhan duoc message")
    void testPublishAndSubscribe() {
        AlertMessage alert = new AlertMessage("pharmacy-alerts", "IMPORT", "Đã nhập 100 hộp Panadol");

        String publishedTopic = alertPublisher.publishAlert(alert);
        assertThat(publishedTopic).isEqualTo("pharmacy-alerts");

        // Cho Subscriber lang nghe va xu ly message bat dong bo
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(alertSubscriber, atLeastOnce()).onMessage(any(), any());
        });
    }
}
