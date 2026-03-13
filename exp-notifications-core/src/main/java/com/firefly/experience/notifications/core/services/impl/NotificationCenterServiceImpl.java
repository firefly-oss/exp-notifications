package com.firefly.experience.notifications.core.services.impl;

import com.firefly.domain.common.notifications.sdk.api.NotificationPreferencesApi;
import com.firefly.domain.common.notifications.sdk.api.NotificationsApi;
import com.firefly.experience.notifications.core.commands.UpdatePreferencesCommand;
import com.firefly.experience.notifications.core.queries.NotificationDTO;
import com.firefly.experience.notifications.core.queries.NotificationDetailDTO;
import com.firefly.experience.notifications.core.queries.NotificationPreferencesDTO;
import com.firefly.experience.notifications.core.queries.UnreadCountDTO;
import com.firefly.experience.notifications.core.services.NotificationCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link NotificationCenterService} that delegates
 * to the domain-common-notifications SDK APIs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCenterServiceImpl implements NotificationCenterService {

    /** Preference channel key for email notifications. */
    public static final String PREF_EMAIL = "EMAIL";

    /** Preference channel key for SMS notifications. */
    public static final String PREF_SMS = "SMS";

    /** Preference channel key for push notifications. */
    public static final String PREF_PUSH = "PUSH";

    /** Preference channel key for in-app notifications. */
    public static final String PREF_IN_APP = "IN_APP";

    /** Preference channel key for marketing communications. */
    public static final String PREF_MARKETING = "MARKETING";

    private final NotificationsApi notificationsApi;
    private final NotificationPreferencesApi notificationPreferencesApi;

    /** {@inheritDoc} */
    @Override
    public Flux<NotificationDTO> listNotifications(UUID partyId) {
        log.debug("Listing notifications for partyId={}", partyId);
        return notificationsApi.getNotificationsForParty(partyId, null, null)
                .map(this::mapToNotificationDTO);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<NotificationDetailDTO> getNotification(UUID notificationId) {
        log.debug("Getting notification notificationId={}", notificationId);
        return notificationsApi.getNotificationDetail(notificationId)
                .map(this::mapToNotificationDetailDTO);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Void> markAsRead(UUID partyId, UUID notificationId) {
        log.debug("Marking as read notificationId={} partyId={}", notificationId, partyId);
        return notificationsApi.markAsRead(notificationId, partyId).then();
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Void> markAllAsRead(UUID partyId) {
        log.debug("Marking all as read partyId={}", partyId);
        return notificationsApi.markAllAsRead(partyId).then();
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Void> deleteNotification(UUID partyId, UUID notificationId) {
        log.debug("Deleting notificationId={} partyId={}", notificationId, partyId);
        return notificationsApi.deleteNotification(notificationId, partyId).then();
    }

    /** {@inheritDoc} */
    @Override
    public Mono<UnreadCountDTO> getUnreadCount(UUID partyId) {
        log.debug("Getting unread count for partyId={}", partyId);
        return notificationsApi.getNotificationsForParty(partyId, null, null)
                .filter(n -> Boolean.FALSE.equals(n.getRead()))
                .count()
                .map(UnreadCountDTO::new);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<NotificationPreferencesDTO> getPreferences(UUID partyId) {
        log.debug("Getting preferences for partyId={}", partyId);
        return notificationPreferencesApi.getPreferences(partyId)
                .map(this::mapToPreferencesDTO);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<NotificationPreferencesDTO> updatePreferences(UUID partyId, UpdatePreferencesCommand command) {
        log.debug("Updating preferences for partyId={}", partyId);
        Map<String, Boolean> prefsMap = buildPreferencesMap(command);
        return notificationPreferencesApi.updatePreferences(partyId, prefsMap)
                .then(notificationPreferencesApi.getPreferences(partyId))
                .map(this::mapToPreferencesDTO);
    }

    // --- Mapping helpers ---

    private NotificationDTO mapToNotificationDTO(com.firefly.domain.common.notifications.sdk.model.NotificationDTO sdk) {
        return NotificationDTO.builder()
                .notificationId(sdk.getNotificationId())
                .title(sdk.getSubject())
                .message(sdk.getBody())
                .channel(sdk.getChannel())
                .isRead(Boolean.TRUE.equals(sdk.getRead()))
                .createdAt(sdk.getCreatedAt())
                .build();
    }

    private NotificationDetailDTO mapToNotificationDetailDTO(com.firefly.domain.common.notifications.sdk.model.NotificationDTO sdk) {
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

    private NotificationPreferencesDTO mapToPreferencesDTO(
            com.firefly.domain.common.notifications.sdk.model.NotificationPreferencesDTO sdk) {
        Map<String, Boolean> prefs = sdk.getPreferences() != null ? sdk.getPreferences() : Map.of();
        return NotificationPreferencesDTO.builder()
                .email(prefs.getOrDefault(PREF_EMAIL, true))
                .sms(prefs.getOrDefault(PREF_SMS, true))
                .push(prefs.getOrDefault(PREF_PUSH, true))
                .inApp(prefs.getOrDefault(PREF_IN_APP, true))
                .marketingConsent(prefs.getOrDefault(PREF_MARKETING, false))
                .build();
    }

    private Map<String, Boolean> buildPreferencesMap(UpdatePreferencesCommand command) {
        Map<String, Boolean> map = new HashMap<>();
        if (command.getEmail() != null) map.put(PREF_EMAIL, command.getEmail());
        if (command.getSms() != null) map.put(PREF_SMS, command.getSms());
        if (command.getPush() != null) map.put(PREF_PUSH, command.getPush());
        if (command.getInApp() != null) map.put(PREF_IN_APP, command.getInApp());
        if (command.getMarketingConsent() != null) map.put(PREF_MARKETING, command.getMarketingConsent());
        return map;
    }
}
