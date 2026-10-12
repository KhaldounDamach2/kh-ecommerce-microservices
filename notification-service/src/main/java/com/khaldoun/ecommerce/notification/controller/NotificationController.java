package com.khaldoun.ecommerce.notification.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.khaldoun.ecommerce.notification.dto.NotificationResponse;
import com.khaldoun.ecommerce.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

// Every endpoint works on "my" notifications only: the user id always comes
// from the gateway header, never from the URL or the body.
// SecurityConfig already rejects requests without the gateway headers.
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/mine")
    public Page<NotificationResponse> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.getMyNotifications(userId, PageRequest.of(page, size));
    }

    @GetMapping("/mine/unread-count")
    public Map<String, Long> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Map.of("count", notificationService.countUnread(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/mine/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
