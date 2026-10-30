# 🧪 Testing Automation Tool (TAT) — Enterprise QA & Defect Management Platform

> Comprehensive quality assurance, test case lifecycle, defect tracking, and analytics platform built with **Java 21**, **Spring Boot 4.1.1**, **Spring Cloud Gateway**, and **Angular 22**.

---

## 🏛 System Architecture Overview

TAT is structured as an enterprise-grade distributed microservices architecture:

```
                                      ┌─────────────────────────────────────────────────────────┐
                                      │              Client Applications & Consoles             │
                                      │   • Angular 22 SPA (http://localhost:4200)               │
                                      │   • Static Consoles (manager-dashboard, bug-chat)       │
                                      └────────────────────────────┬────────────────────────────┘
                                                                   │ HTTP / JSON / Cookies
                                                                   ▼
                                      ┌─────────────────────────────────────────────────────────┐
                                      │             Edge API Gateway (:8082)                    │
                                      │   • Spring Cloud Gateway WebFlux                        │
                                      │   • Global CORS & Cookie Passthrough                    │
                                      │   • Header Deduplication & Reverse Proxy                │
                                      └───────┬────────────────────┬────────────────────┬───────┘
                                              │ /auth/**           │ /manager/**, etc.  │ /api/v1/notifications/**
                                              ▼                    ▼                    ▼
   ┌──────────────────────────────────────────────┐  ┌───────────────────────────┐  ┌───────────────────────────┐
   │          auth-service (:8081)                │  │    main-service (:8090)   │  │ notification-service(:8091│
   │  • User Registration & Authentication        │  │  • Projects & Features    │  │  • Outlook / O365 SMTP    │
   │  • Dual-Token JWT (15m Access / 24h Refresh) │  │  • Test Cases & Execution │  │  • Delivery Tracking DB   │
   │  • In-Memory 6-digit OTP (10m TTL)           │  │  • Bugs & State Machine   │  │  • HTML Email Alerts      │
   └──────────────────────┬───────────────────────┘  │  • Comment Discussions   │  └───────────────────────────┘
                          │                          │  • Attachment Hub         │
                          │                          │  • Executive Health Score │
                          │                          │  • Admin User Operations  │
                          │                          └─────────────┬─────────────┘
                          │                                        │
                          ▼                                        ▼
   ┌────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
   │                                   summary-export-service (:8092)                                           │
   │                         • Apache POI SXSSF Streaming QA Reports & Bulk Excel Templates                     │
   └────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                           │
                                                           ▼
   ┌────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
   │                                      Core Database (MySQL: Interns_db)                                     │
   │              • Users, Refresh Tokens, Projects, Features, Test Cases, Executions, Bugs, Comments          │
   └────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Repository Modules & Port Allocations

| Component | Path | Tech Stack | Port | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend SPA** | `Testing_Automation_tool_frontend/testing-automation-frontend` | Angular 22, TypeScript | `4200` | Web UI for Admin, Manager, Tester, and Developer workspaces |
| **API Gateway** | `TestingAutomationTool/api-gateway` | Spring Cloud Gateway WebFlux | `8082` | Reverse proxy and edge routing |
| **Auth Service** | `TestingAutomationTool/auth-service` | Spring Boot 4.1.1, JJWT | `8081` | Authentication, dual tokens, and OTP password recovery |
| **Main Service** | `TestingAutomationTool/main-service` | Spring Boot 4.1.1, Spring Data JPA | `8090` | Core business logic, defect management, QA health score |
| **Notification Service** | `notification-service` | Spring Boot, Spring Mail | `8091` | Asynchronous Outlook SMTP delivery and tracking |
| **Summary Export Service** | `summary-export-service` | Spring Boot 4.1.1, Apache POI | `8092` | SXSSF streaming Excel exports and dynamic templates |
| **Common Library** | `TestingAutomationTool/common` | Java 21 | N/A | Shared security, entities, DTOs, and CSRF handlers |

---

## 🔐 Key Security Highlights

- **Pure HttpOnly Cookies**: Sensitive tokens (`accessToken` and `refreshToken`) are stored in HttpOnly cookies to mitigate XSS attacks.
- **Double-Submit CSRF Protection**: Angular sends the `XSRF-TOKEN` cookie value as an `X-XSRF-TOKEN` header validated by custom Spring Security 6 handlers (`SpaCsrfTokenRequestHandler`).
- **Role-Based Access Control (RBAC)**: Fine-grained authorizations across `ADMIN`, `MANAGER`, `TESTER`, and `DEVELOPER`.
- **Manager Approval Pipeline**: Newly registered accounts start inactive until verified and role-assigned by a manager.

---

## 🚀 Quickstart & Startup Sequence

### 1. Start MySQL Databases
- Ensure `Interns_db` (primary database) and `notification_service_db` (notification tracking) are active.

### 2. Start Notification Service
```powershell
cd notification-service
./mvnw clean spring-boot:run
```

### 3. Build & Start Core Services (`TestingAutomationTool`)
```powershell
cd TestingAutomationTool
./mvnw clean package -DskipTests

# Start Auth Service (Port 8081)
cd auth-service
../mvnw spring-boot:run

# Start Main Service (Port 8090)
cd ../main-service
../mvnw spring-boot:run

# Start API Gateway (Port 8082)
cd ../api-gateway
../mvnw spring-boot:run
```

### 4. Start Summary Export Service
```powershell
cd summary-export-service
./mvnw clean spring-boot:run
```

### 5. Start Angular Frontend
```powershell
cd Testing_Automation_tool_frontend/testing-automation-frontend
npm install
npm start
```
Access the application at [http://localhost:4200](http://localhost:4200).

---

## 📖 Detailed Documentation
For the full architectural deep dive, REST API directory, database ERDs, and Quality Health Score formulas, view the generated project documentation artifact or [TestingAutomationTool/README.md](file:///d:/Testing_Automation_Tool/TestingAutomationTool/README.md).

