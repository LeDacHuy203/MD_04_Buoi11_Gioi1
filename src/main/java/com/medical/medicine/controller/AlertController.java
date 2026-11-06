package com.medical.medicine.controller;

import com.medical.medicine.dto.request.AlertMessage;
import com.medical.medicine.pubsub.AlertPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alert Notifications (Redis Pub/Sub)", description = "API gui thong bao qua Redis Pub/Sub cho Dashboard quan ly chi nhanh")
public class AlertController {

    private final AlertPublisher alertPublisher;

    /**
     * Endpoint gui thong bao Dashboard (Redis Pub/Sub)
     * POST http://localhost:8080/api/v1/alerts
     * Body:
     * {
     *   "topicListen": "pharmacy-alerts",
     *   "type": "IMPORT",
     *   "message": "Đã nhập 100 hộp Panadol"
     * }
     */
    @PostMapping
    @Operation(summary = "Gui thong bao vao kenh Redis Pub/Sub")
    public ResponseEntity<String> sendAlert(@Valid @RequestBody AlertMessage request) {
        String channel = alertPublisher.publishAlert(request);
        return ResponseEntity.ok("Đã gửi thông báo đến kênh : " + channel);
    }
}
