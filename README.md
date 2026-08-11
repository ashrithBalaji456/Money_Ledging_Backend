<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:6DB33F,100:2E86AB&height=200&section=header&text=Money%20Ledging%20Backend&fontSize=42&fontColor=ffffff&animation=fadeIn&desc=Spring%20Boot%20REST%20API%20for%20Loan%20%26%20Ledger%20Management&descAlignY=62&descSize=16" width="100%"/>

<img src="https://readme-typing-svg.demolab.com/?font=Fira+Code&weight=600&size=20&duration=2800&pause=900&color=6DB33F&center=true&vCenter=true&width=620&lines=Spring+Boot+3.3+%7C+Java+17+%7C+PostgreSQL;JWT+Authentication+%2B+Spring+Security;Swagger%2FOpenAPI+Documented+Endpoints;Dockerized+for+One-Command+Deploys" alt="Typing SVG" />

<p>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
</p>

<p>
  <img alt="stars" src="https://img.shields.io/github/stars/ashrithBalaji456/Money_Ledging_Backend?style=flat-square&color=6DB33F" />
  <img alt="forks" src="https://img.shields.io/github/forks/ashrithBalaji456/Money_Ledging_Backend?style=flat-square&color=6DB33F" />
  <img alt="issues" src="https://img.shields.io/github/issues/ashrithBalaji456/Money_Ledging_Backend?style=flat-square&color=6DB33F" />
  <img alt="last commit" src="https://img.shields.io/github/last-commit/ashrithBalaji456/Money_Ledging_Backend?style=flat-square&color=6DB33F" />
</p>

<img src="https://skillicons.dev/icons?i=java,spring,postgres,docker,maven,git" />

</div>

---

## 📖 Overview

The **Money Ledging Backend** is a secure, layered REST API built with **Spring Boot 3.3.2** on **Java 17**. It powers borrower management, loan issuance, repayment tracking, and ledger reporting for the Money Ledging platform — protected end-to-end with **Spring Security + JWT**, documented via **Swagger/OpenAPI**, and shippable as a **Docker** image.

🔗 Consumed by: [Money_Ledging_Frontend](https://github.com/ashrithBalaji456/Money_Ledging_Frontend) → live at [money-ledging-frontend.vercel.app](https://money-ledging-frontend.vercel.app/)

---

## 🧱 Layered Architecture

```mermaid
flowchart TB
    Client[📱 Frontend / API Client] -->|HTTPS + JWT| Filter[🔐 JWT Auth Filter]
    Filter --> SecurityConfig[🛡️ Spring Security Config]
    SecurityConfig --> Controller[🎮 REST Controllers]
    Controller --> Service[⚙️ Service Layer — Business Logic]
    Service --> Repo[🗃️ Spring Data JPA Repositories]
    Repo --> DB[(🐘 PostgreSQL)]
    Controller -.->|generates docs| Swagger[📑 springdoc-openapi / Swagger UI]

    style Client fill:#61DAFB33,stroke:#2E86AB
    style Filter fill:#00000022,stroke:#333
    style Controller fill:#6DB33F33,stroke:#6DB33F
    style Service fill:#6DB33F33,stroke:#6DB33F
    style Repo fill:#6DB33F33,stroke:#6DB33F
    style DB fill:#4169E133,stroke:#4169E1
```

---

## 🔐 Security & JWT Flow

```mermaid
sequenceDiagram
    actor C as Client
    participant AC as AuthController
    participant AM as AuthenticationManager
    participant UD as UserDetailsService
    participant JW as JwtUtil
    participant SF as JwtAuthFilter
    participant RC as Protected Controller

    C->>AC: POST /api/auth/login {username, password}
    AC->>AM: authenticate()
    AM->>UD: loadUserByUsername()
    UD-->>AM: UserDetails
    AM-->>AC: Authenticated
    AC->>JW: generateToken(user)
    JW-->>AC: signed JWT
    AC-->>C: 200 OK { token }

    C->>RC: GET /api/loans  (Authorization: Bearer JWT)
    RC->>SF: intercept request
    SF->>JW: validateToken()
    JW-->>SF: valid + claims
    SF->>SF: set SecurityContext
    SF->>RC: forward request
    RC-->>C: 200 OK [loan data]
```

---

## 🗂️ Entity Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ LOAN : manages
    BORROWER ||--o{ LOAN : borrows
    LOAN ||--o{ PAYMENT : receives

    USER {
        Long id PK
        String username
        String password
        String role
    }
    BORROWER {
        Long id PK
        String name
        String phone
        String email
        String address
    }
    LOAN {
        Long id PK
        Long borrowerId FK
        BigDecimal principal
        BigDecimal interestRate
        Integer tenureMonths
        String status
        LocalDate disbursedDate
    }
    PAYMENT {
        Long id PK
        Long loanId FK
        BigDecimal amount
        LocalDate paidOn
        String method
    }
```

---

## 📁 Project Structure

```
Money_Ledging_Backend/
├── src/
│   └── main/
│       ├── java/com/example/lending/
│       │   ├── controller/       # REST endpoints
│       │   ├── service/          # Business logic
│       │   ├── repository/       # Spring Data JPA interfaces
│       │   ├── entity/           # JPA entities
│       │   ├── dto/              # Request/response DTOs
│       │   ├── security/         # JWT filter, config, security beans
│       │   ├── config/           # App & Swagger configuration
│       │   └── LendingApplication.java
│       └── resources/
│           └── application.properties
├── Dockerfile
├── pom.xml
└── .dockerignore
```

> Package layout above follows standard Spring Boot conventions for this stack — confirm against `src/` for exact naming.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.2 |
| Security | Spring Security + JJWT (0.11.5) |
| Persistence | Spring Data JPA + PostgreSQL |
| Validation | Spring Boot Starter Validation |
| API Docs | springdoc-openapi (Swagger UI) 2.5.0 |
| Boilerplate | Lombok |
| Testing | Spring Boot Test + Spring Security Test |
| Build | Maven |
| Container | Docker |

---

## 📊 Dependency Breakdown

```mermaid
pie title pom.xml Dependency Groups
    "Spring Boot Starters" : 30
    "Security & JWT" : 25
    "Database (PostgreSQL/JPA)" : 20
    "API Docs (Swagger)" : 10
    "Testing" : 10
    "Lombok" : 5
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 14+
- Docker (optional, for containerized runs)

### Run Locally

```bash
git clone https://github.com/ashrithBalaji456/Money_Ledging_Backend.git
cd Money_Ledging_Backend

# configure your DB credentials in src/main/resources/application.properties

./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Run with Docker

```bash
docker build -t money-ledging-backend .
docker run -p 8080:8080 --env-file .env money-ledging-backend
```

### Environment Variables

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_SECRET` | Secret key used to sign JWTs |
| `JWT_EXPIRATION` | Token expiry (ms) |

---

## 📡 Sample API Endpoints

```mermaid
flowchart LR
    subgraph Auth
        A1[POST /api/auth/login]
        A2[POST /api/auth/register]
    end
    subgraph Borrowers
        B1[GET /api/borrowers]
        B2[POST /api/borrowers]
        B3[PUT /api/borrowers/id]
    end
    subgraph Loans
        C1[GET /api/loans]
        C2[POST /api/loans]
        C3[PUT /api/loans/id/close]
    end
    subgraph Payments
        D1[POST /api/loans/id/payments]
        D2[GET /api/loans/id/payments]
    end
```

> Exact routes are defined in the `controller/` package — the diagram above reflects the expected REST surface for this domain; verify against the live Swagger UI.

---

## 🧪 Testing

```bash
./mvnw test
```

Covers service-layer business logic and security-filter behavior using Spring Boot Test + Spring Security Test.

---

## 🤝 Contributing

```bash
git checkout -b feature/your-feature
git commit -m "feat: describe your change"
git push origin feature/your-feature
```
Then open a Pull Request against `main`.

## 📄 License

Add a `LICENSE` file (MIT recommended) to formalize usage terms.

---

<div align="center">

[![Frontend Repo](https://img.shields.io/badge/🖥️_Frontend_Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ashrithBalaji456/Money_Ledging_Frontend)
[![Live App](https://img.shields.io/badge/🌐_Live_App-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://money-ledging-frontend.vercel.app/)

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:2E86AB,100:6DB33F&height=120&section=footer" width="100%"/>

</div>
