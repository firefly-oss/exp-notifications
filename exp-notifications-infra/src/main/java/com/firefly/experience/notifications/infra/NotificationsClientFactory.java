package com.firefly.experience.notifications.infra;

import com.firefly.domain.common.notifications.sdk.api.NotificationPreferencesApi;
import com.firefly.domain.common.notifications.sdk.api.NotificationsApi;
import com.firefly.domain.common.notifications.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the domain-common-notifications SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class NotificationsClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Common Notifications service
     */
    public NotificationsClientFactory(NotificationsProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link NotificationsApi} bean for notification CRUD operations
     * (list, detail, read, delete).
     *
     * @return a ready-to-use NotificationsApi instance
     */
    @Bean
    public NotificationsApi notificationsApi() {
        return new NotificationsApi(apiClient);
    }

    /**
     * Provides the {@link NotificationPreferencesApi} bean for managing party
     * notification preferences (get and update).
     *
     * @return a ready-to-use NotificationPreferencesApi instance
     */
    @Bean
    public NotificationPreferencesApi notificationPreferencesApi() {
        return new NotificationPreferencesApi(apiClient);
    }
}
