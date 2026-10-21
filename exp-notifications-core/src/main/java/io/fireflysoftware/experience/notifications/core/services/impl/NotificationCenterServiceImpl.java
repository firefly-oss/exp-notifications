/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.notifications.core.services.impl;

import io.fireflysoftware.domain.common.notifications.sdk.api.NotificationsApi;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDTO;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDetailDTO;
import io.fireflysoftware.experience.notifications.core.queries.UnreadCountDTO;
import io.fireflysoftware.experience.notifications.core.services.NotificationCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link NotificationCenterService} that delegates
 * to the domain-common-notifications SDK.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCenterServiceImpl implements NotificationCenterService {

    private final NotificationsApi notificationsApi;

    /** {@inheritDoc} */
    @Override
    public Flux<NotificationDTO> listNotifications(UUID partyId) {
        log.debug("Listing notifications for partyId={}", partyId);
        return notificationsApi.getNotificationsForParty(partyId, null, null, UUID.randomUUID().toString())
                .map(this::mapToNotificationDTO);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<NotificationDetailDTO> getNotification(UUID notificationId) {
        log.debug("Getting notification notificationId={}", notificationId);
        return notificationsApi.getNotificationDetail(notificationId, UUID.randomUUID().toString())
                .map(this::mapToNotificationDetailDTO);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Void> markAsRead(UUID partyId, UUID notificationId) {
        log.debug("Marking as read notificationId={} partyId={}", notificationId, partyId);
        return notificationsApi.markAsRead(notificationId, partyId, UUID.randomUUID().toString()).then();
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Void> markAllAsRead(UUID partyId) {
        log.debug("Marking all as read partyId={}", partyId);
        return notificationsApi.markAllAsRead(partyId, UUID.randomUUID().toString()).then();
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Void> deleteNotification(UUID partyId, UUID notificationId) {
        log.debug("Deleting notificationId={} partyId={}", notificationId, partyId);
        return notificationsApi.deleteNotification(notificationId, partyId, UUID.randomUUID().toString()).then();
    }

    /** {@inheritDoc} */
    @Override
    public Mono<UnreadCountDTO> getUnreadCount(UUID partyId) {
        log.debug("Getting unread count for partyId={}", partyId);
        return notificationsApi.getNotificationsForParty(partyId, null, null, UUID.randomUUID().toString())
                .filter(n -> Boolean.FALSE.equals(n.getRead()))
                .count()
                .map(UnreadCountDTO::new);
    }

    private NotificationDTO mapToNotificationDTO(io.fireflysoftware.domain.common.notifications.sdk.model.NotificationDTO sdk) {
        return NotificationDTO.builder()
                .notificationId(sdk.getNotificationId())
                .title(sdk.getSubject())
                .message(sdk.getBody())
                .channel(sdk.getChannel())
                .isRead(Boolean.TRUE.equals(sdk.getRead()))
                .createdAt(sdk.getCreatedAt())
                .build();
    }

    private NotificationDetailDTO mapToNotificationDetailDTO(io.fireflysoftware.domain.common.notifications.sdk.model.NotificationDTO sdk) {
        return NotificationDetailDTO.builder()
                .notificationId(sdk.getNotificationId())
                .title(sdk.getSubject())
                .message(sdk.getBody())
                .channel(sdk.getChannel())
                .isRead(Boolean.TRUE.equals(sdk.getRead()))
                .createdAt(sdk.getCreatedAt())
                .metadata(Map.of())
                .relatedEntityId(null)
                .relatedEntityType(null)
                .build();
    }
}
