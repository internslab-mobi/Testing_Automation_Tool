# Standalone Email Notification Microservice

A Spring Boot standalone email notification microservice designed to be reusable across multiple client applications. It dispatches emails via Microsoft Outlook SMTP (Office 365 or any compatible SMTP relay) and persists tracking metadata into a dedicated MySQL database.

---

## 1. Architecture & Design Principles

```
Any Client Application ──(REST / JSON / X-API-KEY)──▶ Notification Microservice ──▶ Outlook SMTP (Office 365)
                                                               │
                                                               ▼
                                                  Notification Tracking Database (MySQL)
```

- **Application Independence**: Decoupled from any specific calling system. The service accepts generic recipient lists, subjects, HTML or plain-text bodies, Base64 attachments, and arbitrary tracing metadata (`metadata` map).
- **Outlook SMTP Reliability**: Native integration with Microsoft Outlook SMTP with STARTTLS (TLS 1.2 / 1.3), configurable network timeouts (connection, read, write), and sender display name.
- **Privacy & Security**: Email bodies and attachment contents are **never stored** in the database or logged in application logs.
- **Reliable Audit Trail**: Failed SMTP submissions are recorded with status `FAILED` and concise diagnostic reasons without disappearing due to transaction rollbacks.

---

## 2. Notification Lifecycle Statuses

| Status | Meaning |
| :--- | :--- |
| `PENDING` | Request received and validated; tracking record persisted prior to SMTP handoff. |
| `SUBMITTED` | Email successfully transmitted to and accepted by the Microsoft Outlook SMTP server. *(Note: This indicates submission to the relay; it does not guarantee delivery to the end-recipient's inbox).* |
| `FAILED` | Submission to SMTP failed (e.g., authentication rejection, mailbox send limits, recipient syntax refusal). The failure reason is stored in `failure_reason`. |

---

## 3. Configuration & Environment Variables

The microservice is configured via `src/main/resources/application.properties` using standard environment variables with safe defaults:

### 3.1 Microsoft Outlook SMTP Settings
| Property | Environment Variable | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `notification.mail.outlook.host` | `OUTLOOK_SMTP_HOST` | `smtp.office365.com` | Outlook SMTP server host |
| `notification.mail.outlook.port` | `OUTLOOK_SMTP_PORT` | `587` | SMTP port (STARTTLS default 587) |
| `notification.mail.outlook.username` | `OUTLOOK_SMTP_USERNAME` | *(empty)* | Outlook mailbox username or UPN |
| `notification.mail.outlook.password` | `OUTLOOK_SMTP_PASSWORD` | *(empty)* | Mailbox password or Azure App Password |
| `notification.mail.outlook.default-from` | `OUTLOOK_DEFAULT_FROM` | `notifications@example.com` | Default sender email address |
| `notification.mail.outlook.sender-display-name` | `OUTLOOK_SENDER_NAME` | `Notification Service` | Default sender display name |
| `notification.mail.outlook.connection-timeout` | - | `10000` (10s) | SMTP socket connection timeout (ms) |
| `notification.mail.outlook.read-timeout` | - | `15000` (15s) | SMTP socket read timeout (ms) |
| `notification.mail.outlook.write-timeout` | - | `15000` (15s) | SMTP socket write timeout (ms) |
| `notification.mail.outlook.max-attachment-size-bytes` | - | `10485760` (10 MB) | Max size per individual attachment |
| `notification.mail.outlook.max-total-attachments-size-bytes` | - | `26214400` (25 MB) | Max cumulative attachments size |
| `notification.mail.outlook.blocked-file-extensions` | - | `.exe,.bat,.cmd,.sh,...` | Prohibited executable/script extensions |
| `notification.mail.outlook.allowed-recipient-domains` | `ALLOWED_RECIPIENT_DOMAINS` | *(empty = allow all)* | Comma-separated domain allowlist |

### 3.2 Database Configuration (MySQL)
| Property | Environment Variable | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `spring.datasource.url` | `DB_URL` | `jdbc:mysql://localhost:3306/notification_service_db` | Dedicated MySQL JDBC URL |
| `spring.datasource.username` | `DB_USERNAME` | *(empty)* | Database username |
| `spring.datasource.password` | `DB_PASSWORD` | *(empty)* | Database password |
| `spring.jpa.hibernate.ddl-auto` | - | `none` | DDL auto is disabled to protect databases |

---

## 4. Database Setup & Permissions

The microservice requires a dedicated database (e.g. `notification_service_db`). **Do not point to or modify tables in shared databases like `Interns_db`.**

### Database Administrator Setup Script
To create the dedicated database and permissions on MySQL:
```sql
CREATE DATABASE IF NOT EXISTS notification_service_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'notification_user'@'%' IDENTIFIED BY 'STRONG_PASSWORD_HERE';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, INDEX, ALTER ON notification_service_db.* TO 'notification_user'@'%';
FLUSH PRIVILEGES;
```

### Table DDL
Execute `schema.sql` (located in the project root) or run:
```sql
USE notification_service_db;

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) NOT NULL,
    sender VARCHAR(255) NULL,
    recipients VARCHAR(2000) NOT NULL,
    cc_recipients VARCHAR(2000) NULL,
    bcc_recipients VARCHAR(2000) NULL,
    subject VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    failure_reason VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    submitted_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 5. REST API Reference

### 5.1 Send Email Notification
- **Method**: `POST`
- **Path**: `/api/v1/notifications/email`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "to": ["customer@example.com"],
  "cc": ["billing@example.com"],
  "subject": "Order Confirmation #ORD-10928",
  "isHtml": true,
  "body": "<h1>Order Confirmed</h1><p>Thank you for your order #ORD-10928.</p>",
  "attachments": [
    {
      "fileName": "invoice_1024.pdf",
      "contentType": "application/pdf",
      "contentBase64": "SGVsbG8gV29ybGQ="
    }
  ],
  "metadata": {
    "correlationId": "corr-9988",
    "sourceSystem": "order-management"
  }
}
```
- **Response (`202 Accepted`)**:
```json
{
  "success": true,
  "message": "Email accepted for sending",
  "data": {
    "notificationId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "SUBMITTED",
    "recipientCount": 2,
    "attachmentCount": 1,
    "submittedAt": "2026-10-13T06:00:00Z",
    "metadata": {
      "correlationId": "corr-9988",
      "sourceSystem": "order-management"
    }
  },
  "timestamp": "2026-10-13T06:00:00.000Z"
}
```

### 5.2 Get Notification By ID
- **Method**: `GET`
- **Path**: `/api/v1/notifications/{notificationId}`
- **Response (`200 OK`)**:
```json
{
  "success": true,
  "message": "Notification retrieved successfully",
  "data": {
    "notificationId": "550e8400-e29b-41d4-a716-446655440000",
    "sender": "notifications@example.com",
    "to": ["customer@example.com"],
    "cc": ["billing@example.com"],
    "subject": "Order Confirmation #ORD-10928",
    "status": "SUBMITTED",
    "createdAt": "2026-10-13T05:59:59.000Z",
    "submittedAt": "2026-10-13T06:00:00.000Z"
  },
  "timestamp": "2026-10-13T06:00:05.000Z"
}
```

### 5.3 Get Paginated Notification History
- **Method**: `GET`
- **Path**: `/api/v1/notifications?page=0&size=20&sort=createdAt,desc`
- **Response (`200 OK`)**:
```json
{
  "success": true,
  "message": "Notification history retrieved successfully",
  "data": {
    "items": [
      {
        "notificationId": "550e8400-e29b-41d4-a716-446655440000",
        "sender": "notifications@example.com",
        "to": ["customer@example.com"],
        "subject": "Order Confirmation #ORD-10928",
        "status": "SUBMITTED",
        "createdAt": "2026-10-13T05:59:59.000Z",
        "submittedAt": "2026-10-13T06:00:00.000Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2026-10-13T06:00:10.000Z"
}
```

### 5.4 Health Check (Public)
- **Method**: `GET`
- **Path**: `/actuator/health`

---

## 6. How to Run & Test

### Run Automated Tests
All unit and integration tests run in isolation with mocks and an in-memory test database:
```bash
./mvnw clean test
# or on Windows:
.\mvnw.cmd clean test
```

### Run Locally
```bash
# Set Outlook SMTP credentials
export OUTLOOK_SMTP_USERNAME="your-email@outlook.com"
export OUTLOOK_SMTP_PASSWORD="your-app-password"

# Set dedicated database credentials
export DB_URL="jdbc:mysql://localhost:3306/notification_service_db"
export DB_USERNAME="notification_user"
export DB_PASSWORD="secretpassword"

./mvnw spring-boot:run
```

### Swagger UI
When running, interactive API documentation is available at:
`http://localhost:8091/swagger-ui.html`

### Postman
Import the collection in `postman/notification-service.postman_collection.json` into Postman to test all endpoints, authentication, and error scenarios.
