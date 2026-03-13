package com.firefly.experience.notifications.core.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * Full detail representation of a single notification, extending {@link NotificationDTO}
 * with metadata and related entity information.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotificationDetailDTO extends NotificationDTO {

    private Map<String, String> metadata;
    private UUID relatedEntityId;
    private String relatedEntityType;
}
