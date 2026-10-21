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


package io.fireflysoftware.experience.notifications.web.controllers;

import io.fireflysoftware.experience.notifications.core.commands.UpdatePreferencesCommand;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDTO;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDetailDTO;
import io.fireflysoftware.experience.notifications.core.queries.NotificationPreferencesDTO;
import io.fireflysoftware.experience.notifications.core.queries.UnreadCountDTO;
import io.fireflysoftware.experience.notifications.core.services.NotificationCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing the experience-layer notification centre API.
 * Handles listing, reading, deleting notifications and managing party preferences.
 */
@RestController
@RequestMapping("/api/v1/experience/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification center and preferences")
public class NotificationCenterController {

    // TODO: Extract partyId from JWT token
    private static final UUID PLACEHOLDER_PARTY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final NotificationCenterService notificationCenterService;

    /**
     * Returns the notification list for the authenticated party.
     *
     * @return a {@link Flux} of {@link NotificationDTO} items
     */
    @GetMapping
    @Operation(summary = "List notifications", description = "Returns the notification list for the authenticated party")
    public Flux<NotificationDTO> listNotifications() {
        UUID partyId = PLACEHOLDER_PARTY_ID; // TODO: Extract partyId from JWT token
        return notificationCenterService.listNotifications(partyId);
    }

    /**
     * Returns the full detail for a single notification.
     *
     * @param id the unique identifier of the notification
     * @return a {@link Mono} emitting the {@link NotificationDetailDTO}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get notification detail", description = "Returns full detail for a single notification")
    public Mono<NotificationDetailDTO> getNotification(@PathVariable UUID id) {
        return notificationCenterService.getNotification(id);
    }

    /**
     * Marks the specified notification as read.
     *
     * @param id the unique identifier of the notification to mark as read
     * @return a {@link Mono} that completes with HTTP 204 when successful
     */
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark notification as read")
    public Mono<Void> markAsRead(@PathVariable UUID id) {
        UUID partyId = PLACEHOLDER_PARTY_ID; // TODO: Extract partyId from JWT token
        return notificationCenterService.markAsRead(partyId, id);
    }

    /**
     * Marks all notifications for the authenticated party as read.
     *
     * @return a {@link Mono} that completes with HTTP 204 when successful
     */
    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark all notifications as read")
    public Mono<Void> markAllAsRead() {
        UUID partyId = PLACEHOLDER_PARTY_ID; // TODO: Extract partyId from JWT token
        return notificationCenterService.markAllAsRead(partyId);
    }

    /**
     * Permanently deletes the specified notification.
     *
     * @param id the unique identifier of the notification to delete
     * @return a {@link Mono} that completes with HTTP 204 when successful
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a notification")
    public Mono<Void> deleteNotification(@PathVariable UUID id) {
        UUID partyId = PLACEHOLDER_PARTY_ID; // TODO: Extract partyId from JWT token
        return notificationCenterService.deleteNotification(partyId, id);
    }

    /**
     * Returns the number of unread notifications for the authenticated party.
     *
     * @return a {@link Mono} emitting an {@link UnreadCountDTO}
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public Mono<UnreadCountDTO> getUnreadCount() {
        UUID partyId = PLACEHOLDER_PARTY_ID; // TODO: Extract partyId from JWT token
        return notificationCenterService.getUnreadCount(partyId);
    }

    /**
     * Returns the notification channel preferences for the authenticated party.
     *
     * @return a {@link Mono} emitting a {@link NotificationPreferencesDTO}
     */
    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public Mono<NotificationPreferencesDTO> getPreferences() {
        UUID partyId = PLACEHOLDER_PARTY_ID; // TODO: Extract partyId from JWT token
        return notificationCenterService.getPreferences(partyId);
    }

    /**
     * Updates the notification channel preferences for the authenticated party.
     *
     * @param command the preference changes to apply (null fields are ignored)
     * @return a {@link Mono} emitting the updated {@link NotificationPreferencesDTO}
     */
    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public Mono<NotificationPreferencesDTO> updatePreferences(@RequestBody UpdatePreferencesCommand command) {
        UUID partyId = PLACEHOLDER_PARTY_ID; // TODO: Extract partyId from JWT token
        return notificationCenterService.updatePreferences(partyId, command);
    }
}
