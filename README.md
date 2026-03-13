# exp-notifications

> Backend-for-Frontend service for the in-app notification centre and notification preferences

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [Functional Verticals](#functional-verticals)
- [API Endpoints](#api-endpoints)
- [Domain SDK Dependencies](#domain-sdk-dependencies)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Testing](#testing)

## Overview

`exp-notifications` is the experience-layer service that powers the in-app notification centre for authenticated users. It provides endpoints for listing and reading notifications, marking them as read (individually or all at once), deleting notifications, checking the unread count badge, and managing per-channel notification preferences.

The service is a **simple stateless composition layer**: every request delegates to the `domain-common-notifications` service via its SDK, maps the result to experience-layer DTOs, and returns. There is no workflow, no Redis, and no persistent state within this service.

> **Note**: In the current MVP implementation, the party identifier is resolved from a placeholder constant. Production deployments must extract the party ID from the authenticated JWT token via the security context.

## Architecture

```
Frontend / Mobile App
         |
         v
exp-notifications  (port 8104)
         |
         +---> domain-common-notifications-sdk  (NotificationsApi)
```

## Module Structure

| Module | Purpose |
|--------|---------|
| `exp-notifications-interfaces` | (Reserved for future shared contracts) |
| `exp-notifications-core` | `NotificationCenterService` interface, `NotificationCenterServiceImpl`, command DTO (`UpdatePreferencesCommand`), query DTOs (`NotificationDTO`, `NotificationDetailDTO`, `NotificationPreferencesDTO`, `UnreadCountDTO`) |
| `exp-notifications-infra` | `NotificationsClientFactory`, `NotificationsProperties` |
| `exp-notifications-web` | `NotificationCenterController`, Spring Boot application class, `application.yaml` |
| `exp-notifications-sdk` | Auto-generated reactive SDK from the OpenAPI spec |

## Functional Verticals

| Vertical | Endpoints | Description |
|----------|-----------|-------------|
| Notifications | 5 | List, get detail, mark as read, mark all as read, delete |
| Preferences | 2 | Get and update per-channel notification preferences |
| Counts | 1 | Unread notification count for badge display |

## API Endpoints

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/notifications` | List all notifications for the authenticated party | `200 OK` |
| `GET` | `/api/v1/experience/notifications/{id}` | Retrieve full details for a single notification | `200 OK` |
| `PATCH` | `/api/v1/experience/notifications/{id}/read` | Mark a specific notification as read | `204 No Content` |
| `POST` | `/api/v1/experience/notifications/read-all` | Mark all notifications as read | `204 No Content` |
| `DELETE` | `/api/v1/experience/notifications/{id}` | Delete a specific notification | `204 No Content` |
| `GET` | `/api/v1/experience/notifications/unread-count` | Get the count of unread notifications | `200 OK` |
| `GET` | `/api/v1/experience/notifications/preferences` | Get notification channel preferences for the authenticated party | `200 OK` |
| `PUT` | `/api/v1/experience/notifications/preferences` | Update notification channel preferences | `200 OK` |

## Domain SDK Dependencies

| SDK | ClientFactory | APIs Used | Purpose |
|-----|--------------|-----------|---------|
| `domain-common-notifications-sdk` | `NotificationsClientFactory` | `NotificationsApi` | Notification CRUD, read/unread state management, preference management |

## Configuration

```yaml
server:
  port: ${SERVER_PORT:8104}

api-configuration:
  domain-platform:
    common-notifications:
      base-path: ${COMMON_NOTIFICATIONS_URL:http://localhost:8095}
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8104` | HTTP server port |
| `SERVER_ADDRESS` | `localhost` | Bind address |
| `COMMON_NOTIFICATIONS_URL` | `http://localhost:8095` | Base URL for `domain-common-notifications` |

## Running Locally

```bash
# Prerequisites — ensure domain-common-notifications is running
cd exp-notifications
mvn spring-boot:run -pl exp-notifications-web
```

Server starts on port `8104`. Swagger UI: [http://localhost:8104/swagger-ui.html](http://localhost:8104/swagger-ui.html)

## Testing

```bash
mvn clean verify
```

Tests cover `NotificationCenterServiceImpl` (unit tests with mocked SDK client) and `NotificationCenterController` (WebTestClient-based controller tests verifying HTTP status codes and response shapes for all endpoints).
