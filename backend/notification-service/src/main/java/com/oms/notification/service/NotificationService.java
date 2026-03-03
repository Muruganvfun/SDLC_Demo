package com.oms.notification.service;

import com.oms.notification.dto.NotificationRequest;
import com.oms.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NotificationService {

    private final Map<String, List<NotificationResponse>> userNotifications = new ConcurrentHashMap<>();

    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Sending notification to user {}: {} - {}", 
                request.getUserId(), request.getType(), request.getTitle());

        NotificationResponse notification = NotificationResponse.builder()
                .id(UUID.randomUUID().toString())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .read(false)
                .createdAt(Instant.now())
                .build();

        userNotifications.computeIfAbsent(request.getUserId(), k -> new ArrayList<>())
                .add(notification);

        log.info("Notification sent: {} for user {}", notification.getId(), request.getUserId());
        return notification;
    }

    public List<NotificationResponse> getNotifications(String userId) {
        List<NotificationResponse> notifications = userNotifications.getOrDefault(userId, new ArrayList<>());
        notifications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return notifications;
    }

    public void markAsRead(String userId, String notificationId) {
        List<NotificationResponse> notifications = userNotifications.get(userId);
        if (notifications != null) {
            notifications.stream()
                    .filter(n -> n.getId().equals(notificationId))
                    .forEach(n -> n.setRead(true));
        }
    }
}
