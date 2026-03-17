package com.oms.notification.controller;

import com.oms.common.dto.ApiResponse;
import com.oms.common.security.UserContext;
import com.oms.notification.dto.NotificationRequest;
import com.oms.notification.dto.NotificationResponse;
import com.oms.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @RequestBody NotificationRequest request) {
        log.info("Sending notification: {}", request.getType());
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<NotificationResponse>>>> getNotifications(
            UserContext userContext) {
        log.info("Getting notifications for user: {}", userContext.getUserId());
        List<NotificationResponse> notifications = notificationService.getNotifications(userContext.getUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("notifications", notifications)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String id,
            UserContext userContext) {
        log.info("Marking notification {} as read for user: {}", id, userContext.getUserId());
        notificationService.markAsRead(userContext.getUserId(), id);
        return ResponseEntity.ok().build();
    }
}
