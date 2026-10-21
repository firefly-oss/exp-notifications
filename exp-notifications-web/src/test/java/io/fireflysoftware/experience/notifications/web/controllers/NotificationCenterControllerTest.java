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

import io.fireflysoftware.experience.notifications.core.queries.NotificationDTO;
import io.fireflysoftware.experience.notifications.core.queries.NotificationDetailDTO;
import io.fireflysoftware.experience.notifications.core.queries.UnreadCountDTO;
import io.fireflysoftware.experience.notifications.core.services.NotificationCenterService;
import org.junit.jupiter.api.Test;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = NotificationCenterController.class,
        excludeAutoConfiguration = {
                ReactiveWebSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class NotificationCenterControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    NotificationCenterService notificationCenterService;

    // Firefly GlobalExceptionHandler is component-scanned into the slice; mock its required deps.
    @MockitoBean
    ExceptionConverterService exceptionConverterService;
    @MockitoBean
    ErrorHandlingProperties errorHandlingProperties;
    @MockitoBean
    ErrorResponseNegotiator errorResponseNegotiator;

    private static final UUID PARTY_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    // --- GET /api/v1/experience/notifications ---

    @Test
    void listNotifications_returns200WithList() {
        var dto = buildNotificationDTO(NOTIFICATION_ID, "Hello", "EMAIL", false);
        when(notificationCenterService.listNotifications(PARTY_ID)).thenReturn(Flux.just(dto));

        webTestClient.get()
                .uri(uri -> uri.path("/api/v1/experience/notifications").queryParam("partyId", PARTY_ID).build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NotificationDTO.class)
                .hasSize(1);
    }

    @Test
    void listNotifications_returns200WithEmptyList() {
        when(notificationCenterService.listNotifications(PARTY_ID)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(uri -> uri.path("/api/v1/experience/notifications").queryParam("partyId", PARTY_ID).build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NotificationDTO.class)
                .hasSize(0);
    }

    // --- GET /api/v1/experience/notifications/{id} ---

    @Test
    void getNotification_returns200WithDetail() {
        var detail = buildNotificationDetailDTO(NOTIFICATION_ID);
        when(notificationCenterService.getNotification(NOTIFICATION_ID)).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri("/api/v1/experience/notifications/{id}", NOTIFICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NotificationDetailDTO.class);
    }

    // --- PATCH /api/v1/experience/notifications/{id}/read ---

    @Test
    void markAsRead_returns204() {
        when(notificationCenterService.markAsRead(PARTY_ID, NOTIFICATION_ID)).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(uri -> uri.path("/api/v1/experience/notifications/{id}/read")
                        .queryParam("partyId", PARTY_ID)
                        .build(NOTIFICATION_ID))
                .exchange()
                .expectStatus().isNoContent();

        verify(notificationCenterService).markAsRead(PARTY_ID, NOTIFICATION_ID);
    }

    // --- POST /api/v1/experience/notifications/read-all ---

    @Test
    void markAllAsRead_returns204() {
        when(notificationCenterService.markAllAsRead(PARTY_ID)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uri -> uri.path("/api/v1/experience/notifications/read-all").queryParam("partyId", PARTY_ID).build())
                .exchange()
                .expectStatus().isNoContent();

        verify(notificationCenterService).markAllAsRead(PARTY_ID);
    }

    // --- DELETE /api/v1/experience/notifications/{id} ---

    @Test
    void deleteNotification_returns204() {
        when(notificationCenterService.deleteNotification(PARTY_ID, NOTIFICATION_ID)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(uri -> uri.path("/api/v1/experience/notifications/{id}")
                        .queryParam("partyId", PARTY_ID)
                        .build(NOTIFICATION_ID))
                .exchange()
                .expectStatus().isNoContent();

        verify(notificationCenterService).deleteNotification(PARTY_ID, NOTIFICATION_ID);
    }

    // --- GET /api/v1/experience/notifications/unread-count ---

    @Test
    void getUnreadCount_returns200WithCount() {
        when(notificationCenterService.getUnreadCount(PARTY_ID))
                .thenReturn(Mono.just(new UnreadCountDTO(5L)));

        webTestClient.get()
                .uri(uri -> uri.path("/api/v1/experience/notifications/unread-count").queryParam("partyId", PARTY_ID).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UnreadCountDTO.class)
                .value(dto -> org.assertj.core.api.Assertions.assertThat(dto.getCount()).isEqualTo(5L));
    }

    // --- helpers ---

    private NotificationDTO buildNotificationDTO(UUID id, String title, String channel, boolean read) {
        return NotificationDTO.builder()
                .notificationId(id)
                .title(title)
                .message("Test message")
                .channel(channel)
                .isRead(read)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private NotificationDetailDTO buildNotificationDetailDTO(UUID id) {
        return NotificationDetailDTO.builder()
                .notificationId(id)
                .title("Detail title")
                .message("Detail message")
                .channel("EMAIL")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .metadata(Map.of("key", "value"))
                .relatedEntityId(null)
                .relatedEntityType(null)
                .build();
    }
}
