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


package io.fireflysoftware.experience.notifications.core.services;

import io.fireflysoftware.experience.notifications.core.commands.UpdatePreferencesCommand;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDTO;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDetailDTO;
import io.fireflysoftware.experience.notifications.core.queries.NotificationPreferencesDTO;
import io.fireflysoftware.experience.notifications.core.queries.UnreadCountDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service contract for the notification centre experience layer.
 * Provides read and write operations over a party's notifications and preferences.
 */
public interface NotificationCenterService {

    /**
     * Returns all notifications belonging to the given party.
     *
     * @param partyId the party whose notifications are retrieved
     * @return a {@link Flux} of {@link NotificationDTO} items
     */
    Flux<NotificationDTO> listNotifications(UUID partyId);

    /**
     * Returns the full detail of a single notification.
     *
     * @param notificationId the unique identifier of the notification
     * @return a {@link Mono} emitting the {@link NotificationDetailDTO}
     */
    Mono<NotificationDetailDTO> getNotification(UUID notificationId);

    /**
     * Marks a specific notification as read for the given party.
     *
     * @param partyId        the party that owns the notification
     * @param notificationId the notification to mark as read
     * @return a {@link Mono} that completes when the operation succeeds
     */
    Mono<Void> markAsRead(UUID partyId, UUID notificationId);

    /**
     * Marks all notifications as read for the given party.
     *
     * @param partyId the party whose notifications are marked read
     * @return a {@link Mono} that completes when the operation succeeds
     */
    Mono<Void> markAllAsRead(UUID partyId);

    /**
     * Permanently deletes a notification for the given party.
     *
     * @param partyId        the party that owns the notification
     * @param notificationId the notification to delete
     * @return a {@link Mono} that completes when the deletion succeeds
     */
    Mono<Void> deleteNotification(UUID partyId, UUID notificationId);

    /**
     * Returns the number of unread notifications for the given party.
     *
     * @param partyId the party whose unread count is requested
     * @return a {@link Mono} emitting an {@link UnreadCountDTO}
     */
    Mono<UnreadCountDTO> getUnreadCount(UUID partyId);

    /**
     * Returns the current notification channel preferences for the given party.
     *
     * @param partyId the party whose preferences are retrieved
     * @return a {@link Mono} emitting a {@link NotificationPreferencesDTO}
     */
    Mono<NotificationPreferencesDTO> getPreferences(UUID partyId);

    /**
     * Applies the supplied preference changes for the given party and returns
     * the resulting preference state.
     *
     * @param partyId the party whose preferences are updated
     * @param command the set of preference changes to apply (null fields are ignored)
     * @return a {@link Mono} emitting the updated {@link NotificationPreferencesDTO}
     */
    Mono<NotificationPreferencesDTO> updatePreferences(UUID partyId, UpdatePreferencesCommand command);
}
