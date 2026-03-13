package com.firefly.experience.notifications.core.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Summary representation of a single notification shown in the notification centre list view.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private UUID notificationId;
    private String title;
    private String message;
    private String channel;
    private boolean isRead;
    private LocalDateTime createdAt;
}
