package com.firefly.experience.notifications.core.services;

import com.firefly.domain.common.notifications.sdk.api.NotificationPreferencesApi;
import com.firefly.domain.common.notifications.sdk.api.NotificationsApi;
import com.firefly.domain.common.notifications.sdk.model.NotificationPreferencesDTO;
import com.firefly.experience.notifications.core.commands.UpdatePreferencesCommand;
import com.firefly.experience.notifications.core.queries.NotificationDTO;
import com.firefly.experience.notifications.core.queries.NotificationDetailDTO;
import com.firefly.experience.notifications.core.queries.UnreadCountDTO;
import com.firefly.experience.notifications.core.services.impl.NotificationCenterServiceImpl;
import static com.firefly.experience.notifications.core.services.impl.NotificationCenterServiceImpl.PREF_EMAIL;
import static com.firefly.experience.notifications.core.services.impl.NotificationCenterServiceImpl.PREF_IN_APP;
import static com.firefly.experience.notifications.core.services.impl.NotificationCenterServiceImpl.PREF_MARKETING;
import static com.firefly.experience.notifications.core.services.impl.NotificationCenterServiceImpl.PREF_PUSH;
import static com.firefly.experience.notifications.core.services.impl.NotificationCenterServiceImpl.PREF_SMS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCenterServiceImplTest {

    @Mock
    NotificationsApi notificationsApi;

    @Mock
    NotificationPreferencesApi notificationPreferencesApi;

    @InjectMocks
    NotificationCenterServiceImpl service;

    private static final UUID PARTY_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    // --- listNotifications ---

    @Test
    void listNotifications_mapsSubjectAndBodyCorrectly() {
        var sdkNotification = buildSdkNotification(NOTIFICATION_ID, "Welcome!", "Your account is ready", "EMAIL", false);
        when(notificationsApi.getNotificationsForParty(eq(PARTY_ID), isNull(), isNull(), any()))
                .thenReturn(Flux.just(sdkNotification));

        StepVerifier.create(service.listNotifications(PARTY_ID))
                .assertNext(dto -> {
                    assertThat(dto.getNotificationId()).isEqualTo(NOTIFICATION_ID);
                    assertThat(dto.getTitle()).isEqualTo("Welcome!");
                    assertThat(dto.getMessage()).isEqualTo("Your account is ready");
                    assertThat(dto.getChannel()).isEqualTo("EMAIL");
                    assertThat(dto.isRead()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    void listNotifications_returnsEmptyWhenNoNotifications() {
        when(notificationsApi.getNotificationsForParty(eq(PARTY_ID), isNull(), isNull(), any()))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.listNotifications(PARTY_ID))
                .verifyComplete();
    }

    // --- getNotification ---

    @Test
    void getNotification_returnsDetailDTO() {
        var sdkNotification = buildSdkNotification(NOTIFICATION_ID, "Transfer alert", "You sent EUR 100", "PUSH", true);
        when(notificationsApi.getNotificationDetail(eq(NOTIFICATION_ID), any()))
                .thenReturn(Mono.just(sdkNotification));

        StepVerifier.create(service.getNotification(NOTIFICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto).isInstanceOf(NotificationDetailDTO.class);
                    assertThat(dto.getNotificationId()).isEqualTo(NOTIFICATION_ID);
                    assertThat(dto.getTitle()).isEqualTo("Transfer alert");
                    assertThat(dto.isRead()).isTrue();
                    assertThat(dto.getMetadata()).isEmpty();
                    assertThat(dto.getRelatedEntityId()).isNull();
                    assertThat(dto.getRelatedEntityType()).isNull();
                })
                .verifyComplete();
    }

    // --- markAsRead ---

    @Test
    void markAsRead_delegatesToSdk() {
        when(notificationsApi.markAsRead(eq(NOTIFICATION_ID), eq(PARTY_ID), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.markAsRead(PARTY_ID, NOTIFICATION_ID))
                .verifyComplete();

        verify(notificationsApi).markAsRead(eq(NOTIFICATION_ID), eq(PARTY_ID), any());
    }

    // --- markAllAsRead ---

    @Test
    void markAllAsRead_delegatesToSdk() {
        when(notificationsApi.markAllAsRead(eq(PARTY_ID), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.markAllAsRead(PARTY_ID))
                .verifyComplete();

        verify(notificationsApi).markAllAsRead(eq(PARTY_ID), any());
    }

    // --- deleteNotification ---

    @Test
    void deleteNotification_delegatesToSdk() {
        when(notificationsApi.deleteNotification(eq(NOTIFICATION_ID), eq(PARTY_ID), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.deleteNotification(PARTY_ID, NOTIFICATION_ID))
                .verifyComplete();

        verify(notificationsApi).deleteNotification(eq(NOTIFICATION_ID), eq(PARTY_ID), any());
    }

    // --- getUnreadCount ---

    @Test
    void getUnreadCount_countsMixedReadAndUnread() {
        // TODO: Implement this test
        // Hint: create a Flux with some read=true and some read=false notifications
        // then verify the count matches the number of unread ones.
        // Example: 2 unread out of 3 total → UnreadCountDTO(count=2)
        var read = buildSdkNotification(UUID.randomUUID(), "Old", "Already seen", "EMAIL", true);
        var unread1 = buildSdkNotification(UUID.randomUUID(), "New1", "Not yet read", "SMS", false);
        var unread2 = buildSdkNotification(UUID.randomUUID(), "New2", "Not yet read either", "PUSH", false);

        when(notificationsApi.getNotificationsForParty(eq(PARTY_ID), isNull(), isNull(), any()))
                .thenReturn(Flux.just(read, unread1, unread2));

        StepVerifier.create(service.getUnreadCount(PARTY_ID))
                .assertNext(dto -> assertThat(dto.getCount()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    void getUnreadCount_returnsZeroWhenAllRead() {
        var read1 = buildSdkNotification(UUID.randomUUID(), "Read1", "Body", "EMAIL", true);
        var read2 = buildSdkNotification(UUID.randomUUID(), "Read2", "Body", "SMS", true);

        when(notificationsApi.getNotificationsForParty(eq(PARTY_ID), isNull(), isNull(), any()))
                .thenReturn(Flux.just(read1, read2));

        StepVerifier.create(service.getUnreadCount(PARTY_ID))
                .assertNext(dto -> assertThat(dto.getCount()).isEqualTo(0L))
                .verifyComplete();
    }

    // --- getPreferences ---

    @Test
    void getPreferences_mapsSdkPreferencesDTO() {
        var sdkPrefs = new NotificationPreferencesDTO()
                .partyId(PARTY_ID)
                .preferences(Map.of(PREF_EMAIL, true, PREF_SMS, false, PREF_PUSH, true, PREF_IN_APP, true, PREF_MARKETING, true));
        when(notificationPreferencesApi.getPreferences(eq(PARTY_ID), any()))
                .thenReturn(Mono.just(sdkPrefs));

        StepVerifier.create(service.getPreferences(PARTY_ID))
                .assertNext(dto -> {
                    assertThat(dto.isEmail()).isTrue();
                    assertThat(dto.isSms()).isFalse();
                    assertThat(dto.isPush()).isTrue();
                    assertThat(dto.isInApp()).isTrue();
                    assertThat(dto.isMarketingConsent()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void getPreferences_usesDefaults_whenPreferencesMapIsNull() {
        var sdkPrefs = new NotificationPreferencesDTO().partyId(PARTY_ID).preferences(null);
        when(notificationPreferencesApi.getPreferences(eq(PARTY_ID), any()))
                .thenReturn(Mono.just(sdkPrefs));

        StepVerifier.create(service.getPreferences(PARTY_ID))
                .assertNext(dto -> {
                    assertThat(dto.isEmail()).isTrue();
                    assertThat(dto.isSms()).isTrue();
                    assertThat(dto.isPush()).isTrue();
                    assertThat(dto.isInApp()).isTrue();
                    assertThat(dto.isMarketingConsent()).isFalse();
                })
                .verifyComplete();
    }

    // --- updatePreferences ---

    @Test
    void updatePreferences_sendsOnlyNonNullFields() {
        var command = UpdatePreferencesCommand.builder()
                .sms(false)
                .marketingConsent(true)
                .build();

        var sdkPrefs = new NotificationPreferencesDTO()
                .partyId(PARTY_ID)
                .preferences(Map.of(PREF_SMS, false, PREF_MARKETING, true));

        when(notificationPreferencesApi.updatePreferences(eq(PARTY_ID), any(), any()))
                .thenReturn(Mono.just(new Object()));
        when(notificationPreferencesApi.getPreferences(eq(PARTY_ID), any()))
                .thenReturn(Mono.just(sdkPrefs));

        StepVerifier.create(service.updatePreferences(PARTY_ID, command))
                .assertNext(dto -> {
                    assertThat(dto.isSms()).isFalse();
                    assertThat(dto.isMarketingConsent()).isTrue();
                })
                .verifyComplete();

        verify(notificationPreferencesApi).updatePreferences(
                eq(PARTY_ID),
                eq(Map.of(PREF_SMS, false, PREF_MARKETING, true)),
                any());
    }

    // --- helpers ---

    private com.firefly.domain.common.notifications.sdk.model.NotificationDTO buildSdkNotification(
            UUID id, String subject, String body, String channel, boolean read) {
        return new com.firefly.domain.common.notifications.sdk.model.NotificationDTO()
                .notificationId(id)
                .partyId(PARTY_ID)
                .subject(subject)
                .body(body)
                .channel(channel)
                .read(read)
                .createdAt(LocalDateTime.now());
    }
}
