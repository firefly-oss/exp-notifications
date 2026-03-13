package com.firefly.experience.notifications.web.controllers;

import com.firefly.experience.notifications.core.commands.UpdatePreferencesCommand;
import com.firefly.experience.notifications.core.queries.NotificationDTO;
import com.firefly.experience.notifications.core.queries.NotificationDetailDTO;
import com.firefly.experience.notifications.core.queries.NotificationPreferencesDTO;
import com.firefly.experience.notifications.core.queries.UnreadCountDTO;
import com.firefly.experience.notifications.core.services.NotificationCenterService;
import org.junit.jupiter.api.Test;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = NotificationCenterController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class NotificationCenterControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    NotificationCenterService notificationCenterService;

    // Firefly GlobalExceptionHandler is component-scanned into the slice; mock its required deps.
    @MockBean
    ExceptionConverterService exceptionConverterService;
    @MockBean
    ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    ErrorResponseNegotiator errorResponseNegotiator;

    // Matches the placeholder in the controller
    private static final UUID PARTY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    // --- GET /api/v1/experience/notifications ---

    @Test
    void listNotifications_returns200WithList() {
        var dto = buildNotificationDTO(NOTIFICATION_ID, "Hello", "EMAIL", false);
        when(notificationCenterService.listNotifications(PARTY_ID)).thenReturn(Flux.just(dto));

        webTestClient.get()
                .uri("/api/v1/experience/notifications")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(NotificationDTO.class)
                .hasSize(1);
    }

    @Test
    void listNotifications_returns200WithEmptyList() {
        when(notificationCenterService.listNotifications(PARTY_ID)).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/experience/notifications")
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
                .uri("/api/v1/experience/notifications/{id}/read", NOTIFICATION_ID)
                .exchange()
                .expectStatus().isNoContent();

        verify(notificationCenterService).markAsRead(PARTY_ID, NOTIFICATION_ID);
    }

    // --- POST /api/v1/experience/notifications/read-all ---

    @Test
    void markAllAsRead_returns204() {
        when(notificationCenterService.markAllAsRead(PARTY_ID)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/experience/notifications/read-all")
                .exchange()
                .expectStatus().isNoContent();

        verify(notificationCenterService).markAllAsRead(PARTY_ID);
    }

    // --- DELETE /api/v1/experience/notifications/{id} ---

    @Test
    void deleteNotification_returns204() {
        when(notificationCenterService.deleteNotification(PARTY_ID, NOTIFICATION_ID)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/experience/notifications/{id}", NOTIFICATION_ID)
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
                .uri("/api/v1/experience/notifications/unread-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UnreadCountDTO.class)
                .value(dto -> org.assertj.core.api.Assertions.assertThat(dto.getCount()).isEqualTo(5L));
    }

    // --- GET /api/v1/experience/notifications/preferences ---

    @Test
    void getPreferences_returns200() {
        var prefs = NotificationPreferencesDTO.builder()
                .email(true).sms(true).push(false).inApp(true).marketingConsent(false).build();
        when(notificationCenterService.getPreferences(PARTY_ID)).thenReturn(Mono.just(prefs));

        webTestClient.get()
                .uri("/api/v1/experience/notifications/preferences")
                .exchange()
                .expectStatus().isOk()
                .expectBody(NotificationPreferencesDTO.class)
                .value(dto -> {
                    org.assertj.core.api.Assertions.assertThat(dto.isEmail()).isTrue();
                    org.assertj.core.api.Assertions.assertThat(dto.isPush()).isFalse();
                });
    }

    // --- PUT /api/v1/experience/notifications/preferences ---

    @Test
    void updatePreferences_returns200WithUpdatedPrefs() {
        var command = UpdatePreferencesCommand.builder().sms(false).marketingConsent(true).build();
        var updated = NotificationPreferencesDTO.builder()
                .email(true).sms(false).push(true).inApp(true).marketingConsent(true).build();

        when(notificationCenterService.updatePreferences(eq(PARTY_ID), any(UpdatePreferencesCommand.class)))
                .thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/api/v1/experience/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NotificationPreferencesDTO.class)
                .value(dto -> {
                    org.assertj.core.api.Assertions.assertThat(dto.isSms()).isFalse();
                    org.assertj.core.api.Assertions.assertThat(dto.isMarketingConsent()).isTrue();
                });
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
