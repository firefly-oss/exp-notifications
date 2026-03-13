package com.firefly.experience.notifications.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload carrying the notification channel preferences for a party.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDTO {

    private boolean email;
    private boolean sms;
    private boolean push;
    private boolean inApp;
    private boolean marketingConsent;
}
