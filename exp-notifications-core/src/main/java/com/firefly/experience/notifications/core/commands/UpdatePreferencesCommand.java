package com.firefly.experience.notifications.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command payload for updating a party's notification channel preferences.
 * Only non-null fields are applied; omitted fields remain unchanged.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesCommand {

    private Boolean email;
    private Boolean sms;
    private Boolean push;
    private Boolean inApp;
    private Boolean marketingConsent;
}
