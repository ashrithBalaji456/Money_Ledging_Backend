<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:2E86AB,100:6A4C93&height=220&section=header&text=Money%20Ledging&fontSize=55&fontColor=ffffff&animation=fadeIn&desc=Full-Stack%20Money%20Lending%20%26%20Ledger%20Management%20System&descAlignY=62&descSize=18" width="100%"/>

<a href="https://money-ledging-frontend.vercel.app/">
  <img src="https://readme-typing-svg.demolab.com/?font=Fira+Code&weight=600&size=22&duration=3000&pause=800&color=2E86AB&center=true&vCenter=true&width=650&lines=Track+Loans%2C+Borrowers+%26+Repayments+in+Real+Time;Spring+Boot+%2B+PostgreSQL+%2B+JWT+Auth+on+the+Backend;React+19+%2B+Vite+%2B+TailwindCSS+on+the+Frontend;One+Ledger.+Two+Repos.+Zero+Spreadsheets." alt="Typing SVG" />
</a>

<br/>

[![Live Demo](https://img.shields.io/badge/🔴_LIVE_DEMO-money--ledging--frontend.vercel.app-2E86AB?style=for-the-badge)](https://money-ledging-frontend.vercel.app/)

<p>
  <img src="https://img.shields.io/badge/Backend-Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Frontend-React_19-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/Database-PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Auth-JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />
</p>

<p>
  <img alt="Backend stars" src="https://img.shields.io/github/stars/ashrithBalaji456/Money_Ledging_Backend?style=flat-square&color=2E86AB&label=backend%20★" />
  <img alt="Frontend stars" src="https://img.shields.io/github/stars/ashrithBalaji456/Money_Ledging_Frontend?style=flat-square&color=6A4C93&label=frontend%20★" />
  <img alt="Backend last commit" src="https://img.shields.io/github/last-commit/ashrithBalaji456/Money_Ledging_Backend?style=flat-square&color=2E86AB" />
  <img alt="Frontend last commit" src="https://img.shields.io/github/last-commit/ashrithBalaji456/Money_Ledging_Frontend?style=flat-square&color=6A4C93" />
</p>

</div>

---

## 📖 About

**Money Ledging** is a full-stack money lending & ledger management platform. It lets an admin/lender register borrowers, issue loans, record repayments, and track outstanding balances — all backed by a secure Spring Boot API and a fast, modern React dashboard.

This repository serves as the **project hub** linking both codebases:

| Repo | Description | Link |
|---|---|---|
| 🖥️ **Frontend** | React 19 + Vite dashboard UI | [Money_Ledging_Frontend](https://github.com/ashrithBalaji456/Money_Ledging_Frontend) |
| ⚙️ **Backend** | Spring Boot REST API | [Money_Ledging_Backend](https://github.com/ashrithBalaji456/Money_Ledging_Backend) |
| 🌐 **Live App** | Deployed production build | [money-ledging-frontend.vercel.app](https://money-ledging-frontend.vercel.app/) |

> Each repo also ships its own detailed, animated `README.md` — see [`BACKEND_README.md`](#) and [`FRONTEND_README.md`](#) in this hub, or the linked repos directly.

---

## 🧩 System Architecture

```mermaid
flowchart LR
    subgraph Client["🖥️ Client Layer"]
        A[React 19 + Vite SPA] -->|Axios REST calls| B[React Router Pages]
    end

    subgraph Edge["☁️ Hosting"]
        C[Vercel — Frontend Hosting]
        D[Docker Container — Backend Hosting]
    end

    subgraph Server["⚙️ Application Layer"]
        E[Spring Boot REST Controllers]
        F[Spring Security + JWT Filter]
        G[Service Layer]
        H[JPA Repositories]
    end

    subgraph Data["🗄️ Data Layer"]
        I[(PostgreSQL Database)]
    end

    A -->|HTTPS| C
    C -->|API Requests| D
    D --> E
    E --> F
    F -->|Authorized| G
    G --> H
    H --> I

    style Client fill:#61DAFB33,stroke:#2E86AB,stroke-width:2px
    style Server fill:#6DB33F33,stroke:#2E86AB,stroke-width:2px
    style Data fill:#4169E133,stroke:#2E86AB,stroke-width:2px
    style Edge fill:#6A4C9333,stroke:#2E86AB,stroke-width:2px
```

---

## 🔄 Core Workflow — Loan Lifecycle

```mermaid
flowchart TD
    Start([Admin logs in]) --> Auth{JWT Valid?}
    Auth -- No --> Login[Redirect to Login]
    Auth -- Yes --> Dash[Dashboard]

    Dash --> RegBorrower[Register New Borrower]
    Dash --> NewLoan[Create New Loan]
    Dash --> ViewLedger[View Ledger / Transactions]

    RegBorrower --> NewLoan
    NewLoan --> SetTerms[Set Principal, Interest Rate, Tenure]
    SetTerms --> Disburse[Disburse Loan]
    Disburse --> TrackRepay[Track Repayments / EMIs]

    TrackRepay --> Partial{Payment Type}
    Partial -- Full Payment --> Close[Close Loan]
    Partial -- Partial Payment --> UpdateBalance[Update Outstanding Balance]
    UpdateBalance --> TrackRepay

    Close --> Ledger[Update Ledger Record]
    Ledger --> Report[Export PDF Statement]

    style Start fill:#2E86AB,color:#fff
    style Close fill:#6DB33F,color:#fff
    style Report fill:#6A4C93,color:#fff
```

---

## 🔐 Authentication Sequence

```mermaid
sequenceDiagram
    actor U as User
    participant FE as React Frontend
    participant API as Spring Boot API
    participant Sec as Spring Security Filter
    participant DB as PostgreSQL

    U->>FE: Enter credentials
    FE->>API: POST /api/auth/login
    API->>Sec: Validate credentials
    Sec->>DB: Fetch user by username
    DB-->>Sec: User record
    Sec-->>API: Credentials valid
    API-->>FE: JWT Access Token
    FE->>FE: Store token (memory/localStorage)
    FE->>API: GET /api/loans (Authorization: Bearer <token>)
    API->>Sec: Validate JWT
    Sec-->>API: Token valid, user authorized
    API->>DB: Query loans
    DB-->>API: Loan data
    API-->>FE: 200 OK + JSON payload
    FE-->>U: Render Dashboard
```

---

## 🗂️ Data Model (High-Level ER Diagram)

```mermaid
erDiagram
    USER ||--o{ LOAN : creates
    BORROWER ||--o{ LOAN : receives
    LOAN ||--o{ PAYMENT : has

    USER {
        Long id PK
        string username
        string password
        string role
    }
    BORROWER {
        Long id PK
        string name
        string phone
        string email
        string address
    }
    LOAN {
        Long id PK
        Long borrowerId FK
        decimal principal
        decimal interestRate
        int tenureMonths
        string status
        date disbursedDate
    }
    PAYMENT {
        Long id PK
        Long loanId FK
        decimal amount
        date paidOn
        string method
    }
```

> Field names above reflect the typical domain model for a lending/ledger system — see the backend repo for exact entity definitions.

---

## 📊 Tech Stack Composition

```mermaid
pie title Backend Stack Weight
    "Spring Boot Core" : 35
    "Spring Security + JWT" : 25
    "PostgreSQL / JPA" : 20
    "Swagger / OpenAPI" : 10
    "Docker" : 10
```

```mermaid
pie title Frontend Stack Weight
    "React 19 + Router" : 35
    "TailwindCSS 4" : 25
    "Axios (API layer)" : 15
    "jsPDF / autoTable" : 15
    "Lucide Icons" : 10
```

---

## ✨ Features

- 🔐 Secure JWT-based authentication & role-based access
- 👥 Borrower registration & profile management
- 💰 Loan creation with configurable principal, interest & tenure
- 📈 Repayment / EMI tracking with live outstanding balance
- 📒 Full ledger view of all transactions
- 🧾 One-click **PDF statement export** (via `jsPDF` + `jspdf-autotable`)
- 📑 Interactive API docs via Swagger / springdoc-openapi
- 🐳 Dockerized backend for consistent deployment
- ⚡ Instant, HMR-powered frontend dev experience via Vite

---

## 🛠️ Full Stack Setup

```bash
# 1. Clone both repos
git clone https://github.com/ashrithBalaji456/Money_Ledging_Backend.git
git clone https://github.com/ashrithBalaji456/Money_Ledging_Frontend.git

# 2. Run the backend (Spring Boot, Java 17, Maven)
cd Money_Ledging_Backend
./mvnw spring-boot:run
# API available at http://localhost:8080

# 3. Run the frontend (React + Vite)
cd ../Money_Ledging_Frontend
npm install
npm run dev
# App available at http://localhost:5173
```

> Full environment variable references, Docker instructions, and deep-dive docs live in each repo's own README (linked above).

---

## 🧭 Repository Map

```mermaid
flowchart LR
    Hub[📘 Money Ledging — Project Hub] --> BE[⚙️ Money_Ledging_Backend]
    Hub --> FE[🖥️ Money_Ledging_Frontend]
    FE -->|deployed on| Vercel[▲ Vercel]
    BE -->|containerized via| Docker[🐳 Docker]
    Vercel -->|calls| BE
```

---

## 🗺️ Roadmap

- [x] JWT authentication
- [x] Loan & borrower CRUD
- [x] PDF ledger export
- [ ] Email/SMS repayment reminders
- [ ] Multi-currency support
- [ ] Role-based dashboards (Admin / Agent)
- [ ] Analytics dashboard with charts

---

## 🤝 Contributing

Contributions are welcome on either repo! Fork, create a feature branch, and open a PR against `main`.

```bash
git checkout -b feature/your-feature
git commit -m "feat: add your feature"
git push origin feature/your-feature
```

## 📄 License

This project is available for personal and educational use. Add a `LICENSE` file to either repo to formalize terms (MIT recommended).

---

<div align="center">

### 🔗 Quick Links

[![Backend Repo](https://img.shields.io/badge/⚙️_Backend_Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ashrithBalaji456/Money_Ledging_Backend)
[![Frontend Repo](https://img.shields.io/badge/🖥️_Frontend_Repo-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ashrithBalaji456/Money_Ledging_Frontend)
[![Live App](https://img.shields.io/badge/🌐_Live_App-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://money-ledging-frontend.vercel.app/)

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:6A4C93,100:2E86AB&height=120&section=footer" width="100%"/>

</div>
