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

import io.fireflysoftware.domain.common.notifications.sdk.api.NotificationsApi;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDTO;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDetailDTO;
import io.fireflysoftware.experience.notifications.core.queries.UnreadCountDTO;
import io.fireflysoftware.experience.notifications.core.services.impl.NotificationCenterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
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

    // --- helpers ---

    private io.fireflysoftware.domain.common.notifications.sdk.model.NotificationDTO buildSdkNotification(
            UUID id, String subject, String body, String channel, boolean read) {
        return new io.fireflysoftware.domain.common.notifications.sdk.model.NotificationDTO()
                .notificationId(id)
                .partyId(PARTY_ID)
                .subject(subject)
                .body(body)
                .channel(channel)
                .read(read)
                .createdAt(LocalDateTime.now());
    }
}
